package org.eu.cciradih.socks5;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(25700)) {
            while (true) {
                Socket client = serverSocket.accept();
                Start start = new Start(client);
                Thread.startVirtualThread(start);
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }
}
