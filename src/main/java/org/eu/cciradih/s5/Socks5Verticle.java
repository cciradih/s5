package org.eu.cciradih.s5;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Objects;
import java.util.logging.LogManager;

public class Socks5Verticle extends AbstractVerticle {
    private static final String LOGGING_PROPERTIES = "/vertx-default-jul-logging.properties";
    private static final String CONFIGURATION_JSON ="/configuration.json";
    private static final String ALGORITHM = "AES";

    public static final String KEY = "key";
    public static final String IV = "iv";

    static {
        try (InputStream loggingStream = Socks5Verticle.class.getResourceAsStream(LOGGING_PROPERTIES)) {
            LogManager.getLogManager().readConfiguration(loggingStream);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void start() throws IOException {
        Configuration configuration = this.initConfiguration();

        if (configuration == null) {
            return;
        }

        this.vertx.deployVerticle(new ProxyClientVerticle(configuration));
        this.vertx.deployVerticle(new ProxyServerVerticle(configuration));
    }

    private Configuration initConfiguration() throws IOException {
        InputStream configurationStream = Socks5Verticle.class.getResourceAsStream(CONFIGURATION_JSON);
        Objects.requireNonNull(configurationStream);
        byte[] bytes = configurationStream.readAllBytes();
        configurationStream.close();
        Configuration configuration = new JsonObject(new String(bytes))
                .mapTo(Configuration.class);
        this.initAes(configuration);
        return configuration;
    }

    private void initAes(Configuration configuration) {
        CacheUtil cacheUtil = CacheUtil.getInstance();

        String key = configuration.aes().key();
        byte[] bytes = Base64.getDecoder().decode(key);
        SecretKeySpec secretKeySpec = new SecretKeySpec(bytes, ALGORITHM);
        cacheUtil.put(KEY, secretKeySpec);

        String iv = configuration.aes().iv();
        bytes = Base64.getDecoder().decode(iv);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(bytes);
        cacheUtil.put(IV, ivParameterSpec);
    }
}
