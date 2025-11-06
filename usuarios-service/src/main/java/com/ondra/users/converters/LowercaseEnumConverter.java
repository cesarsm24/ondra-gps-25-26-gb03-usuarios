package com.ondra.users.converters;

import jakarta.persistence.AttributeConverter;

/**
 * Converter base genérico para convertir enumeraciones entre el formato Java y base de datos.
 *
 * <p>Este converter transforma automáticamente los valores de enums:
 * <ul>
 *     <li>De Java a BD: Convierte de MAYÚSCULAS a minúsculas</li>
 *     <li>De BD a Java: Convierte de minúsculas a MAYÚSCULAS</li>
 * </ul>
 *
 * <p>Esto permite mantener la convención estándar de Java (enums en MAYÚSCULAS)
 * mientras la base de datos almacena valores más legibles en minúsculas.
 *
 * @param <E> Tipo de enumeración que extiende de {@link Enum}
 */
public abstract class LowercaseEnumConverter<E extends Enum<E>>
        implements AttributeConverter<E, String> {

    private final Class<E> enumClass;

    /**
     * Constructor que inicializa el converter con la clase del enum.
     *
     * @param enumClass Clase del enum a convertir
     */
    protected LowercaseEnumConverter(Class<E> enumClass) {
        this.enumClass = enumClass;
    }

    /**
     * Convierte el valor del enum de Java a su representación en base de datos.
     *
     * @param attribute Valor del enum en Java (MAYÚSCULAS)
     * @return Valor en minúsculas para almacenar en BD, o null si el atributo es null
     */
    @Override
    public String convertToDatabaseColumn(E attribute) {
        return attribute == null ? null : attribute.name().toLowerCase();
    }

    /**
     * Convierte el valor de base de datos al enum de Java.
     *
     * @param dbData Valor en minúsculas desde la BD
     * @return Enum correspondiente en Java (MAYÚSCULAS), o null si dbData es null o vacío
     */
    @Override
    public E convertToEntityAttribute(String dbData) {
        return (dbData == null || dbData.isEmpty())
                ? null
                : Enum.valueOf(enumClass, dbData.toUpperCase());
    }
}