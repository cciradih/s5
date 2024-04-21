package org.eu.cciradih.socks5;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.concurrent.locks.ReentrantLock;

public class CipherUtil {
    private volatile static CipherUtil instance;
    private static final ReentrantLock REENTRANT_LOCK = new ReentrantLock();
    private static SecretKeySpec secretKeySpec;
    private static IvParameterSpec ivParameterSpec;
    private static Cipher cipher;

    private CipherUtil() {
    }

    public static CipherUtil getInstance() {
        if (instance == null) {
            boolean held = REENTRANT_LOCK.tryLock();
            if (held) {
                try {
                    if (instance == null) {
                        instance = new CipherUtil();

                        SecureRandom secureRandom = new SecureRandom();
                        byte[] key = new byte[32];
                        secureRandom.nextBytes(key);
                        byte[] iv = new byte[16];
                        secureRandom.nextBytes(iv);

                        secretKeySpec = new SecretKeySpec(key, "AES");
                        ivParameterSpec = new IvParameterSpec(iv);
                        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                    }
                } catch (Exception ignored) {
                } finally {
                    REENTRANT_LOCK.unlock();
                }

            }
        }
        return instance;
    }

    public byte[] doFinal(byte[] bytes, int mode) {
        try {
            cipher.init(mode, secretKeySpec, ivParameterSpec);
            return cipher.doFinal(bytes);
        } catch (Exception ignored) {
            return new byte[0];
        }
    }
}
