package org.eu.cciradih.socks5;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.net.*;
import java.nio.ByteBuffer;

public class Server extends Proxy implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
    private final Configuration configuration;

    public Server(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void run() {
        try {
            //  Creating a proxy server.
            ServerSocket proxyServer = this.getProxy(this.configuration.proxyServer());
            while (true) {
                //  Receive proxy client requests.
                Socket proxyClient = proxyServer.accept();
                //  Using virtual threads to process requests.
                Thread.startVirtualThread(() -> {
                    try {
                        //  Socks 5 https://datatracker.ietf.org/doc/html/rfc1928
                        //  Since it is a local deployment, the protocol verification is skipped.
                        /*
                            +----+----------+----------+
                            |VER | NMETHODS | METHODS  |
                            +----+----------+----------+
                            | 1  |    1     | 1 to 255 |
                            +----+----------+----------+
                         */
                        this.read(proxyClient, Cipher.DECRYPT_MODE);
                        /*
                            +----+--------+
                            |VER | METHOD |
                            +----+--------+
                            | 1  |   1    |
                            +----+--------+
                         */
                        this.write(proxyClient, new byte[]{5, 0}, Cipher.ENCRYPT_MODE);
                        /*
                            +----+-----+-------+------+----------+----------+
                            |VER | CMD |  RSV  | ATYP | DST.ADDR | DST.PORT |
                            +----+-----+-------+------+----------+----------+
                            | 1  |  1  | X'00' |  1   | Variable |    2     |
                            +----+-----+-------+------+----------+----------+
                         */
                        byte[] bytes = this.read(proxyClient, Cipher.DECRYPT_MODE);
                        /*
                            +----+-----+-------+------+----------+----------+
                            |VER | REP |  RSV  | ATYP | BND.ADDR | BND.PORT |
                            +----+-----+-------+------+----------+----------+
                            | 1  |  1  | X'00' |  1   | Variable |    2     |
                            +----+-----+-------+------+----------+----------+
                         */
                        this.write(proxyClient, new byte[]{5, 0, 0, 1, 0, 0, 0, 0, 0, 0}, Cipher.ENCRYPT_MODE);
                        //  Get remote server address and port.
                        String remoteServerAddress = this.getRemoteServerAddress(bytes);
                        int remoteServerPort = this.getRemoteServerPort(bytes);
                        //  Connecting to a remote server.
                        Socket remoteServer = this.getServer(remoteServerAddress, remoteServerPort, this.configuration.proxyServer());
                        //  Proxy client to remote server.
                        this.copy(proxyClient, remoteServer, Cipher.DECRYPT_MODE);
                        //  Remote server to proxy client.
                        this.copy(remoteServer, proxyClient, Cipher.ENCRYPT_MODE);
                    } catch (Exception e) {
                        LOGGER.error("{} <-> {} - {}", proxyClient, proxyServer, e.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    private String getRemoteServerAddress(byte[] bytes) throws UnknownHostException {
        byte addressType = bytes[3];
        return switch (addressType) {
            case 1 -> {
                byte[] ipv4 = new byte[4];
                System.arraycopy(bytes, 4, ipv4, 0, ipv4.length);
                yield Inet4Address.getByAddress(ipv4).getHostAddress();
            }
            case 3 -> {
                int domainLength = bytes[4];
                byte[] domain = new byte[domainLength];
                System.arraycopy(bytes, 5, domain, 0, domain.length);
                yield new String(domain);
            }
            case 4 -> {
                byte[] ipv6 = new byte[16];
                System.arraycopy(bytes, 4, ipv6, 0, ipv6.length);
                yield Inet6Address.getByAddress(ipv6).getHostAddress();
            }
            default -> "";
        };
    }

    private int getRemoteServerPort(byte[] bytes) {
        bytes = new byte[]{bytes[bytes.length - 2], bytes[bytes.length - 1]};
        return ByteBuffer.wrap(bytes).asCharBuffer().get();
    }
}
