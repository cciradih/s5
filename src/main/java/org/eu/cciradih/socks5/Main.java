package org.eu.cciradih.socks5;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(25700)) {
            while (true) {
                Socket client = serverSocket.accept();
                Start start = new Start(client);
                Thread.startVirtualThread(start);
            }
        } catch (IOException e) {
            System.out.println("Main: " + e.getMessage());
        }
    }
}
