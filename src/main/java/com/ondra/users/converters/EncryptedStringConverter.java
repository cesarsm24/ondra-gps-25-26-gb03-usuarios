package com.ondra.users.converters;

import com.ondra.users.services.EncryptionService;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Convertidor JPA para cifrado automático de campos String.
 *
 * <p>Cifra datos antes de guardar en base de datos y descifra al leer.
 * Soporta valores null.</p>
 *
 * <p>Ejemplo de uso:</p>
 * <pre>
 * {@literal @}Column(length = 255)
 * {@literal @}Convert(converter = EncryptedStringConverter.class)
 * private String numeroTarjeta;
 * </pre>
 */
@Component
@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private static EncryptionService encryptionService;

    /**
     * Inyecta el servicio de cifrado mediante setter.
     *
     * <p>Utiliza inyección estática para compatibilidad con JPA.</p>
     *
     * @param service Servicio de cifrado
     */
    @Autowired
    public void setEncryptionService(EncryptionService service) {
        EncryptedStringConverter.encryptionService = service;
    }

    /**
     * Cifra el valor antes de almacenarlo en base de datos.
     *
     * @param attribute Valor plano del atributo
     * @return Valor cifrado en Base64, o null si el atributo es null
     */
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        return encryptionService.encrypt(attribute);
    }

    /**
     * Descifra el valor al leerlo desde base de datos.
     *
     * @param dbData Valor cifrado almacenado
     * @return Valor plano descifrado, o null si el dato es null
     */
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return encryptionService.decrypt(dbData);
    }
}