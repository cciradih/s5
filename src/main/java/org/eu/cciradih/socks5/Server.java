package org.eu.cciradih.socks5;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class Server implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(25701)) {
            while (true) {
                Socket client = serverSocket.accept();
                Thread.startVirtualThread(() -> {
                    try {
                        InputStream clientInputStream = client.getInputStream();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientInputStream));
                        String targetString = bufferedReader.readLine();
                        String[] split = targetString.split(":");
                        Socket target = new Socket();
                        target.setSoTimeout(30 * 1000);
                        target.connect(new InetSocketAddress(split[0], Integer.parseInt(split[1])), 2 * 1000);
                        OutputStream targetOutputStream = target.getOutputStream();
                        InputStream targetInputStream = target.getInputStream();
                        OutputStream clientOutputStream = client.getOutputStream();
                        Thread.startVirtualThread(() -> {
                            try {
                                IOUtils.copy(targetInputStream, clientOutputStream);
                            } catch (IOException ignored) {
                            }
                        });
                        IOUtils.copy(clientInputStream, targetOutputStream);
                    } catch (IOException ignored) {
                    }
                });
            }
        } catch (IOException ignored) {
        }
    }
}
