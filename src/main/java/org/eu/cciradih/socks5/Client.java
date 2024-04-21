package org.eu.cciradih.socks5;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
            ServerSocket proxyClient = this.getProxy(this.configuration.proxyClient());
            while (true) {
                Socket localClient = proxyClient.accept();
                Socket proxyServer = this.getServer(configuration.proxyServer(), configuration.proxyClient());
                Thread.startVirtualThread(() -> {
                    try {
                        //  step 1
                        this.readAndWrite(localClient, proxyServer);
                        this.readAndWrite(proxyServer, localClient);
                        //  step 2
                        this.readAndWrite(localClient, proxyServer);
                        this.readAndWrite(proxyServer, localClient);
                        //  copy
                        this.copy(localClient, proxyServer);
                        this.copy(proxyServer, localClient);
                    } catch (IOException e) {
                        LOGGER.error("{} <-> {} - {}", localClient, proxyServer, e.getMessage());
                    }
                });
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }
}
