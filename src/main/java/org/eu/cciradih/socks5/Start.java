package org.eu.cciradih.socks5;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

public class Start implements Runnable {
    private final Socket client;
    private String address;
    private int port;

    public Start(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try {
            InputStream clientInputStream = this.client.getInputStream();
            OutputStream clientOutputStream = this.client.getOutputStream();
            byte[] buffer = new byte[1024];
            clientInputStream.read(buffer);
            clientOutputStream.write(new byte[]{5, 0});

            buffer = new byte[1024];
            int read = clientInputStream.read(buffer);
            clientOutputStream.write(new byte[]{5, 0, 0, 1, 0, 0, 0, 0, 0, 0});

            byte addressType = buffer[3];
            this.address = switch (addressType) {
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
            this.port = ByteBuffer.wrap(new byte[]{buffer[read - 2], buffer[read - 1]}).asCharBuffer().get();
            Socket server = new Socket();
            server.setKeepAlive(true);
            server.setSoTimeout(30 * 1000);
            server.connect(new InetSocketAddress(this.address, this.port), 2 * 1000);
            Thread.startVirtualThread(new End(this.client, server));
            Thread.startVirtualThread(new End(server, this.client));
        } catch (Exception e) {
            String clientAddress = this.client.getInetAddress().getHostAddress();
            int clientPort = this.client.getPort();
            System.out.println("End error: " + clientAddress + ":" + clientPort + " -> " + this.address + ":" +
                    this.port + " - " + e.getMessage());
        }
    }
}
