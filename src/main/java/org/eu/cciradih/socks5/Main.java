package org.eu.cciradih.socks5;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        Thread.startVirtualThread(new Client());
        Thread.startVirtualThread(new Server());

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage());
        }
    }
}
