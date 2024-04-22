package org.eu.cciradih.socks5;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Base64;

class MainTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainTests.class);

    @Test
    void aes() throws Exception {
        byte[] aesKey = this.generateBytes(32);
        LOGGER.info(Base64.getEncoder().encodeToString(aesKey));
        byte[] aesIv = this.generateBytes(16);
        LOGGER.info(Base64.getEncoder().encodeToString(aesIv));
    }

    private byte[] generateBytes(int length) throws Exception {
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        byte[] bytes = new byte[length];
        secureRandom.nextBytes(bytes);
        return bytes;
    }
}
