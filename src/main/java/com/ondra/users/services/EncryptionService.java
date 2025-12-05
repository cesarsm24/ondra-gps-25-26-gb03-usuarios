package com.ondra.users.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
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

    private static final String ALGORITHM = "AES";
    private final SecretKeySpec secretKey;

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
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }

        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
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
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }

        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("❌ Error al descifrar datos: {}", e.getMessage());
            throw new RuntimeException("Error al descifrar los datos", e);
        }
    }
}