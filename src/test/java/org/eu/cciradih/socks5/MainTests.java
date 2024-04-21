package org.eu.cciradih.socks5;

import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECParameterSpec;
import java.util.Base64;

class MainTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainTests.class);

    @Test
    void ecc() throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Security.addProvider(new BouncyCastleProvider());

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC", "BC");

        X9ECParameters curve25519 = CustomNamedCurves.getByName("Curve25519");
        ECParameterSpec ecParameterSpec = EC5Util.convertToSpec(curve25519);
        keyPairGenerator.initialize(ecParameterSpec, new SecureRandom());
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        ECPublicKey ecPublicKey = (ECPublicKey) keyPair.getPublic();
        System.out.println(Base64.getEncoder().encodeToString(ecPublicKey.getEncoded()));
        ECPrivateKey ecPrivateKey = (ECPrivateKey) keyPair.getPrivate();
        System.out.println(Base64.getEncoder().encodeToString(ecPrivateKey.getEncoded()));

        byte[] bytes = new byte[8192];
        new SecureRandom().nextBytes(bytes);
        System.out.println(Base64.getEncoder().encodeToString(bytes));

        Cipher cipher = Cipher.getInstance("ECIES", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, ecPublicKey);
        bytes = cipher.doFinal(bytes);
        System.out.println(Base64.getEncoder().encodeToString(bytes));

        cipher.init(Cipher.DECRYPT_MODE, ecPrivateKey);
        bytes = cipher.doFinal(bytes);
        System.out.println(Base64.getEncoder().encodeToString(bytes));
    }

    @Test
    void aes() {
        byte[] bytes = new byte[8192];
        new SecureRandom().nextBytes(bytes);
        System.out.println(Base64.getEncoder().encodeToString(bytes));

        CipherUtil cipherUtil = CipherUtil.getInstance();
        bytes = cipherUtil.doFinal(bytes, Cipher.ENCRYPT_MODE);
        System.out.println(Base64.getEncoder().encodeToString(bytes));

        bytes = cipherUtil.doFinal(bytes, Cipher.DECRYPT_MODE);
        System.out.println(Base64.getEncoder().encodeToString(bytes));
    }
}
