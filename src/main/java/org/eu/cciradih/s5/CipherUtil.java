package org.eu.cciradih.s5;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public record CipherUtil(CacheUtil cacheUtil) {
    private static final String TRANSFORMATION = "AES/CTR/NoPadding";

    private static class CipherUtilHolder {
        private static final CacheUtil CACHE_UTIL = CacheUtil.getInstance();
        private static final CipherUtil INSTANCE = new CipherUtil(CACHE_UTIL);
    }

    public static CipherUtil getInstance() {
        return CipherUtilHolder.INSTANCE;
    }

    public byte[] doFinal(byte[] bytes, int mode) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            SecretKeySpec secretKeySpec = (SecretKeySpec) this.cacheUtil.get(Socks5Verticle.KEY);
            IvParameterSpec ivParameterSpec = (IvParameterSpec) this.cacheUtil.get(Socks5Verticle.IV);
            cipher.init(mode, secretKeySpec, ivParameterSpec);
            return cipher.doFinal(bytes);
        } catch (Exception e) {
            return new byte[0];
        }
    }
}
