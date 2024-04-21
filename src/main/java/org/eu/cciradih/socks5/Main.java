package org.eu.cciradih.socks5;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.stream.Collectors;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("configuration.json");
        Objects.requireNonNull(inputStream);
        Configuration configuration = null;
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            String configurationJsonString = bufferedReader.lines().collect(Collectors.joining());
            configuration = new ObjectMapper().readValue(configurationJsonString, Configuration.class);
        } catch (IOException ioException) {
        }

        if (configuration == null) {
            LOGGER.error("Error reading configuration file.");
            return;
        }

        Thread.startVirtualThread(new Client(configuration));
        Thread.startVirtualThread(new Server(configuration));

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage());
        }
    }
}
