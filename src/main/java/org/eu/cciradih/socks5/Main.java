package org.eu.cciradih.socks5;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.Objects;
import java.util.stream.Collectors;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static final CacheUtil CACHE_UTIL = CacheUtil.getCacheUtil();


    public static void main(String[] args) throws Exception {
        //  Init configuration.
        Configuration configuration = init();
        //  Start a proxy client using a virtual thread.
        Thread.startVirtualThread(new Client(configuration));
        //  Start a proxy server using a virtual thread.
        Thread.startVirtualThread(new Server(configuration));

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage());
        }
    }

    private static Configuration init() throws Exception {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("configuration.json");
        Objects.requireNonNull(inputStream);
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            String configurationJsonString = bufferedReader.lines().collect(Collectors.joining());
            Configuration configuration = new ObjectMapper().readValue(configurationJsonString, Configuration.class);
            //  Init AES.
            init(configuration);
            return configuration;
        }
    }

    private static void init(Configuration configuration) {
        //  Init key.
        String key = configuration.aes().key();
        byte[] bytes = Base64.getDecoder().decode(key);
        SecretKeySpec secretKeySpec = new SecretKeySpec(bytes, "AES");
        CACHE_UTIL.put("key", secretKeySpec);
        //  Init iv.
        String iv = configuration.aes().iv();
        bytes = Base64.getDecoder().decode(iv);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(bytes);
        CACHE_UTIL.put("iv", ivParameterSpec);
    }
}
