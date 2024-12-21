package controllers.EncryptDecrypt;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.inject.Inject;

public class EncryptDecrypt {

    private static final String ALGORITHM = "AES";

    private static final String secretKey = "MySecretKey12345";

    //Κρυπτογράφηση
    public String encrypt(String plainPassword) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKey key = new SecretKeySpec(secretKey.getBytes(), ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = cipher.doFinal(plainPassword.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    //Απόκρυπτογράφηση
    public String decrypt(String encryptedPassword) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKey key = new SecretKeySpec(secretKey.getBytes(), ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedPassword));
        return new String(decryptedBytes);
    }

}
