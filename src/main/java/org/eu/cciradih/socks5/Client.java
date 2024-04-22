package org.eu.cciradih.socks5;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.net.ServerSocket;
import java.net.Socket;

public class Client extends Proxy implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);

    private final Configuration configuration;

    public Client(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void run() {
        try {
            //  Creating a proxy client.
            ServerSocket proxyClient = this.getProxy(this.configuration.proxyClient());
            while (true) {
                //  Receive local client requests.
                Socket localClient = proxyClient.accept();
                //  Connecting to a proxy server.
                Socket proxyServer = this.getServer(configuration.proxyServer(), configuration.proxyClient());
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
                        byte[] bytes = this.read(localClient);
                        this.write(proxyServer, bytes, Cipher.ENCRYPT_MODE);
                        /*
                            +----+--------+
                            |VER | METHOD |
                            +----+--------+
                            | 1  |   1    |
                            +----+--------+
                         */
                        bytes = this.read(proxyServer, Cipher.DECRYPT_MODE);
                        this.write(localClient, bytes);
                        /*
                            +----+-----+-------+------+----------+----------+
                            |VER | CMD |  RSV  | ATYP | DST.ADDR | DST.PORT |
                            +----+-----+-------+------+----------+----------+
                            | 1  |  1  | X'00' |  1   | Variable |    2     |
                            +----+-----+-------+------+----------+----------+
                         */
                        bytes = this.read(localClient);
                        this.write(proxyServer, bytes, Cipher.ENCRYPT_MODE);
                        /*
                            +----+-----+-------+------+----------+----------+
                            |VER | REP |  RSV  | ATYP | BND.ADDR | BND.PORT |
                            +----+-----+-------+------+----------+----------+
                            | 1  |  1  | X'00' |  1   | Variable |    2     |
                            +----+-----+-------+------+----------+----------+
                         */
                        bytes = this.read(proxyServer, Cipher.DECRYPT_MODE);
                        this.write(localClient, bytes);
                        //  Local client to proxy server.
                        this.copy(localClient, proxyServer, Cipher.ENCRYPT_MODE);
                        //  Proxy server to local client.
                        this.copy(proxyServer, localClient, Cipher.DECRYPT_MODE);
                    } catch (Exception e) {
                        LOGGER.error("{} <-> {} - {}", localClient, proxyServer, e.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }
}
