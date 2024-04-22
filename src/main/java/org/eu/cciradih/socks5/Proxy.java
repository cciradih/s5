package org.eu.cciradih.socks5;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class Proxy {
    private static final Logger LOGGER = LoggerFactory.getLogger(Proxy.class);

    public ServerSocket getProxy(Configuration.Proxy proxy) throws IOException {
        InetSocketAddress inetSocketAddress = new InetSocketAddress(proxy.address(), proxy.port());
        ServerSocket serverSocket = new ServerSocket();
        serverSocket.bind(inetSocketAddress);
        return serverSocket;
    }

    public Socket getServer(Configuration.Proxy proxyServer, Configuration.Proxy proxyClient) throws IOException {
        String address = proxyServer.address();
        int port = proxyServer.port();
        return this.getServer(address, port, proxyClient);
    }

    public Socket getServer(String address, int port, Configuration.Proxy proxy) throws IOException {
        return this.getServer(address, port, proxy.timeout());
    }

    public Socket getServer(String address, int port, int timeout) throws IOException {
        Socket server = new Socket();
        server.setKeepAlive(true);
        server.setSoTimeout(timeout);
        server.connect(new InetSocketAddress(address, port), timeout);
        return server;
    }

    public byte[] read(Socket source) throws IOException {
        byte[] buffer = new byte[8192];
        int read = source.getInputStream().read(buffer);
        return Arrays.copyOfRange(buffer, 0, read);
    }

    public byte[] read(Socket source, int mode) throws Exception {
        byte[] read = this.read(source);
        return CipherUtil.getInstance().doFinal(read, mode);
    }

    public void write(Socket target, byte[] buffer) throws IOException {
        target.getOutputStream().write(buffer);
    }

    public void write(Socket target, byte[] buffer, int mode) throws Exception {
        buffer = CipherUtil.getInstance().doFinal(buffer, mode);
        this.write(target, buffer);
    }

    public void copy(Socket source, Socket target, int mode) {
        Thread.startVirtualThread(() -> {
            try {
                InputStream inputStream = source.getInputStream();
                OutputStream outputStream = target.getOutputStream();
                int read;
                byte[] buffer = new byte[8192];
                while (-1 != (read = inputStream.read(buffer))) {
                    buffer = Arrays.copyOfRange(buffer, 0, read);
                    byte[] bytes = CipherUtil.getInstance().doFinal(buffer, mode);
                    outputStream.write(bytes);
                }
            } catch (Exception e) {
                LOGGER.error("{} -> {} - {}", source, target, e.getMessage());
            }
        });
    }
}
