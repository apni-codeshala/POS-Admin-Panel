package org.example.chronoadmin.service;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.Security;
import java.util.Base64;
import java.util.Map;

public class AdminCryptographicService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final String MASTER_KEY = "CHRONOPOS_MASTER_KEY_2025"; // Same as client
    private static final String ADMIN_PRIVATE_KEY = "CHRONOPOS_ADMIN_PRIVATE_KEY_2025"; // Admin signing key

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static String encryptData(Object data) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String jsonData = mapper.writeValueAsString(data);

        SecretKeySpec secretKey = generateKeyFromMaster();
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] encryptedData = cipher.doFinal(jsonData.getBytes());
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    public static <T> T decryptData(String encryptedData, Class<T> clazz) throws Exception {
        SecretKeySpec secretKey = generateKeyFromMaster();
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        byte[] decodedData = Base64.getDecoder().decode(encryptedData);
        byte[] decryptedData = cipher.doFinal(decodedData);

        String jsonData = new String(decryptedData);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonData, clazz);
    }

    public static String createScratchCardEmbeddedPassword(String scratchCode, String salesPersonId,
                                                          String salesPersonName, String territory) throws Exception {
        Map<String, String> data = Map.of(
            "scratchCode", scratchCode,
            "salesPersonId", salesPersonId,
            "salesPersonName", salesPersonName,
            "territory", territory,
            "timestamp", String.valueOf(System.currentTimeMillis())
        );
        return encryptData(data);
    }

    public static Map<String, String> decryptSalesPersonKey(String encryptedKey) throws Exception {
        return decryptData(encryptedKey, Map.class);
    }

    public static String generateLicenseKey(String scratchCode, String salesPersonId,
                                          String customerDetails, String systemFingerprint) throws Exception {
        Map<String, Object> licenseData = Map.of(
            "licenseKey", generateRandomLicenseKey(),
            "scratchCode", scratchCode,
            "salesPersonId", salesPersonId,
            "customerDetails", customerDetails,
            "systemFingerprint", systemFingerprint,
            "isActive", true,
            "issuedAt", java.time.LocalDateTime.now().toString(),
            "expiresAt", java.time.LocalDateTime.now().plusYears(1).toString(),
            "adminSignature", signData(scratchCode + salesPersonId + systemFingerprint)
        );
        return encryptData(licenseData);
    }

    private static String signData(String data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] dataBytes = (data + ADMIN_PRIVATE_KEY).getBytes();
        byte[] hash = md.digest(dataBytes);
        return Base64.getEncoder().encodeToString(hash);
    }

    private static SecretKeySpec generateKeyFromMaster() throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] key = sha.digest(MASTER_KEY.getBytes());
        byte[] keyBytes = new byte[16];
        System.arraycopy(key, 0, keyBytes, 0, 16);
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    public static String generateScratchCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            code.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return code.toString();
    }

    private static String generateRandomLicenseKey() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < 32; i++) {
            if (i > 0 && i % 4 == 0) {
                key.append("-");
            }
            key.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return key.toString();
    }
}
