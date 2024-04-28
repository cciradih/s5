package org.eu.cciradih.s5;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import io.vertx.core.parsetools.RecordParser;

import javax.crypto.Cipher;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * +-------------+--------+----------+
 * | MAGIC ASCII | LENGTH | DATA     |
 * +-------------+--------+----------+
 * | 6           | 4      | variable |
 * +-------------+--------+----------+
 * MAGIC ASCII: 6 random ascii between 0x20 and 0x7E
 * LENGTH: data length
 * DATA: data to be transferred
 */
public record ProtocolUtil(CipherUtil cipherUtil) {
    private static final byte RANDOM_START = 0x20;
    private static final byte RANDOM_END = 0x7E;

    public static final String ALGORITHM = "SHA1PRNG";

    private static class ProtocolUtilHolder {
        private static final CipherUtil CIPHER_UTIL = CipherUtil.getInstance();
        private static final ProtocolUtil INSTANCE = new ProtocolUtil(CIPHER_UTIL);
    }

    public static ProtocolUtil getInstance() {
        return ProtocolUtil.ProtocolUtilHolder.INSTANCE;
    }

    public Buffer generate(Buffer buffer) {
        byte[] magicAsciiBytes = this.generateMagicAscii();
        byte[] dataBytes = buffer.getBytes();
        dataBytes = this.cipherUtil.doFinal(dataBytes, Cipher.ENCRYPT_MODE);
        return Buffer.buffer().appendBytes(magicAsciiBytes)
                .appendInt(buffer.length())
                .appendBytes(dataBytes);
    }

    public int getLength(Buffer buffer) {
        byte[] lengthBytes = Arrays.copyOfRange(buffer.getBytes(), 6, 10);
        return Buffer.buffer()
                .appendBytes(lengthBytes)
                .getInt(0);
    }

    public Buffer getData(Buffer buffer) {
        byte[] dataBytes = this.cipherUtil.doFinal(buffer.getBytes(), Cipher.DECRYPT_MODE);
        return Buffer.buffer(dataBytes);
    }

    public RecordParser getRecordParser(NetSocket netSocket) {
        RecordParser recordParser = RecordParser.newFixed(10);
        recordParser.setOutput(new Handler<>() {
            int size = -1;

            @Override
            public void handle(Buffer event) {
                if (-1 == size) {
                    size = getLength(event);
                    recordParser.fixedSizeMode(size);
                } else {
                    event = getData(event);
                    netSocket.write(event);
                    recordParser.fixedSizeMode(10);
                    size = -1;
                }
            }
        });
        return recordParser;
    }

    private byte[] generateMagicAscii() {
        Buffer buffer = Buffer.buffer();
        try {
            SecureRandom secureRandom = SecureRandom.getInstance(ALGORITHM);
            byte[] bytes = new byte[6];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = (byte) secureRandom.nextInt(RANDOM_START, RANDOM_END + 1);
            }
            buffer.appendBytes(bytes);
            return buffer.getBytes();
        } catch (Exception ignored) {
            return null;
        }
    }
}
