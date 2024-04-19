package org.eu.cciradih.socks5;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class End implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(End.class);

    private final Socket source;
    private final Socket target;

    public End(Socket source, Socket target) {
        this.source = source;
        this.target = target;
    }

    @Override
    public void run() {
        try (InputStream inputStream = this.source.getInputStream();
             OutputStream outputStream = this.target.getOutputStream()) {
            IOUtils.copy(inputStream, outputStream);
        } catch (IOException e) {
            String sourceAddress = this.source.getInetAddress().getHostAddress();
            int sourcePort = this.source.getPort();
            String targetAddress = this.target.getInetAddress().getHostAddress();
            int targetPort = this.target.getPort();
            LOGGER.error("{}:{} -> {}:{} - {}", sourceAddress, sourcePort, targetAddress, targetPort, e.getMessage());
        }
    }
}
