package org.eu.cciradih.socks5;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.ByteBuffer;

public class Client implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(25700)) {
            while (true) {
                Socket client = serverSocket.accept();
                Thread.startVirtualThread(() -> {
                    try {
                        InputStream clientInputStream = client.getInputStream();
                        OutputStream clientOutputStream = client.getOutputStream();
                        byte[] buffer = new byte[8192];
                        clientInputStream.read(buffer);
                        clientOutputStream.write(new byte[]{5, 0});

                        buffer = new byte[8192];
                        int read = clientInputStream.read(buffer);
                        clientOutputStream.write(new byte[]{5, 0, 0, 1, 0, 0, 0, 0, 0, 0});

                        byte addressType = buffer[3];
                        String address = switch (addressType) {
                            case 1 -> {
                                byte[] ipv4 = new byte[4];
                                System.arraycopy(buffer, 4, ipv4, 0, ipv4.length);
                                yield Inet4Address.getByAddress(ipv4).getHostAddress();
                            }
                            case 3 -> {
                                int domainLength = buffer[4];
                                byte[] domain = new byte[domainLength];
                                System.arraycopy(buffer, 5, domain, 0, domain.length);
                                yield new String(domain);
                            }
                            case 4 -> {
                                byte[] ipv6 = new byte[16];
                                System.arraycopy(buffer, 4, ipv6, 0, ipv6.length);
                                yield Inet6Address.getByAddress(ipv6).getHostAddress();
                            }
                            default -> "";
                        };
                        int port = ByteBuffer.wrap(new byte[]{buffer[read - 2], buffer[read - 1]}).asCharBuffer().get();
                        Socket server = new Socket();
                        server.connect(new InetSocketAddress("127.0.0.1", 25701));

                        String target = address + ":" + port + System.lineSeparator();
                        byte[] targetBytes = target.getBytes();
                        OutputStream serverOutputStream = server.getOutputStream();
                        IOUtils.write(targetBytes, serverOutputStream);
                        Thread.startVirtualThread(() -> {
                            try {
                                IOUtils.copy(clientInputStream, serverOutputStream);
                            } catch (IOException e) {
                            }
                        });
                        InputStream serverInputStream = server.getInputStream();
                        Thread.startVirtualThread(() -> {
                            try {
                                IOUtils.copy(serverInputStream, clientOutputStream);
                            } catch (IOException ignored) {
                            }
                        });
                    } catch (Exception ignored) {
                    }
                });
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }
}
