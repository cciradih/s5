package org.eu.cciradih.socks5;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
            ServerSocket proxyServer = this.getProxy(this.configuration.proxyServer());
            while (true) {
                Socket proxyClient = proxyServer.accept();
                Thread.startVirtualThread(() -> {
                    try {
                        //  step 1
                        this.readAndWrite(proxyClient, proxyClient, new byte[]{5, 0});
                        byte[] bytes = this.readAndWrite(proxyClient, proxyClient, new byte[]{5, 0, 0, 1, 0, 0, 0, 0, 0, 0});
                        //  get remote server
                        String remoteServerAddress = this.getRemoteServerAddress(bytes);
                        int remoteServerPort = this.getRemoteServerPort(bytes);
                        Socket remoteServer = this.getServer(remoteServerAddress, remoteServerPort, this.configuration.proxyServer());
                        //  copy
                        this.copy(proxyClient, remoteServer);
                        this.copy(remoteServer, proxyClient);
                    } catch (IOException e) {
                        LOGGER.error("{} - {}", proxyClient, e.getMessage());
                    }
                });
            }
        } catch (IOException e) {
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

    private int getRemoteServerPort(byte[] bytes) throws UnknownHostException {
        bytes = new byte[]{bytes[bytes.length - 2], bytes[bytes.length - 1]};
        return ByteBuffer.wrap(bytes).asCharBuffer().get();
    }
}
