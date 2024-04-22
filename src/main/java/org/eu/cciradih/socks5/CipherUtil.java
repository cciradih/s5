package org.eu.cciradih.socks5;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CipherUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(CipherUtil.class);
    private static final CacheUtil CACHE_UTIL = CacheUtil.getCacheUtil();

    private CipherUtil() {
    }

    private static class CipherUtilHolder {
        private static final CipherUtil INSTANCE = new CipherUtil();
    }

    public static CipherUtil getInstance() {
        return CipherUtilHolder.INSTANCE;
    }

    public byte[] doFinal(byte[] bytes, int mode) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
            SecretKeySpec secretKeySpec = (SecretKeySpec) CACHE_UTIL.get("key");
            IvParameterSpec ivParameterSpec = (IvParameterSpec) CACHE_UTIL.get("iv");
            cipher.init(mode, secretKeySpec, ivParameterSpec);
            return cipher.doFinal(bytes);
        } catch (Exception e) {
            LOGGER.error("{} {}", e, bytes);
            return new byte[0];
        }
    }
}
