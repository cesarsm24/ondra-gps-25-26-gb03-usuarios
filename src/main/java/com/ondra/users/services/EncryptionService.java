package com.ondra.users.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * Servicio para cifrado y descifrado de datos sensibles usando AES.
 *
 * <p>Implementa cifrado simétrico AES-128 para proteger información confidencial como:
 * números de tarjeta, CVV, IBAN, correos de PayPal y teléfonos de Bizum.</p>
 *
 * <p>La clave de cifrado debe configurarse en application.properties y ser única por entorno.
 * En producción, debe almacenarse de forma segura (Kubernetes Secrets, AWS Secrets Manager, etc.).</p>
 */
@Slf4j
@Service
public class EncryptionService {

    private final SecretKeySpec secretKey;
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12; // 12 bytes IV recomendados para GCM
    private static final int GCM_TAG_LENGTH = 128; // bits

    /**
     * Constructor que inicializa la clave de cifrado desde configuración.
     *
     * <p>Deriva una clave AES-128 aplicando SHA-256 sobre el string configurado.</p>
     *
     * @param secretKeyString clave secreta desde application.properties
     * @throws RuntimeException si falla la inicialización del servicio
     */
    public EncryptionService(@Value("${encryption.secret-key}") String secretKeyString) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] key = sha.digest(secretKeyString.getBytes(StandardCharsets.UTF_8));
            key = Arrays.copyOf(key, 16);
            this.secretKey = new SecretKeySpec(key, ALGORITHM);
            log.info("✅ Servicio de cifrado inicializado");
        } catch (Exception e) {
            log.error("❌ Error al inicializar servicio de cifrado: {}", e.getMessage());
            throw new RuntimeException("Error al inicializar el servicio de cifrado", e);
        }
    }

    /**
     * Cifra un texto plano usando AES.
     *
     * @param plainText texto a cifrar
     * @return texto cifrado en Base64, o el input original si es null o vacío
     * @throws RuntimeException si falla el cifrado
     */
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) return plainText;
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv); // IV aleatorio para cada cifrado
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Concatenar IV + ciphertext para almacenar
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encrypted.length);
            byteBuffer.put(iv);
            byteBuffer.put(encrypted);
            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            log.error("❌ Error al cifrar datos: {}", e.getMessage());
            throw new RuntimeException("Error al cifrar los datos", e);
        }
    }

    /**
     * Descifra un texto cifrado en Base64 usando AES.
     *
     * @param encryptedText texto cifrado en Base64
     * @return texto plano descifrado, o el input original si es null o vacío
     * @throws RuntimeException si falla el descifrado
     */
    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) return encryptedText;
        try {
            byte[] decoded = Base64.getDecoder().decode(encryptedText);
            ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);

            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);

            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);

            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
            byte[] decrypted = cipher.doFinal(cipherText);

            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("❌ Error al descifrar datos: {}", e.getMessage());
            throw new RuntimeException("Error al descifrar los datos", e);
        }
    }
}