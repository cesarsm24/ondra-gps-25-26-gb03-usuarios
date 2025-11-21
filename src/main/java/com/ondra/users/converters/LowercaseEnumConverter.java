package com.ondra.users.converters;

import jakarta.persistence.AttributeConverter;

/**
 * Converter base para enumeraciones con almacenamiento en minúsculas.
 *
 * <p>Transforma valores de enums entre Java y base de datos:</p>
 * <ul>
 *     <li>Java a BD: MAYÚSCULAS → minúsculas</li>
 *     <li>BD a Java: minúsculas → MAYÚSCULAS</li>
 * </ul>
 *
 * @param <E> Tipo de enumeración
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
     * Convierte el enum de Java a su representación en base de datos.
     *
     * @param attribute Valor del enum en mayúsculas
     * @return Valor en minúsculas, o null si el atributo es null
     */
    @Override
    public String convertToDatabaseColumn(E attribute) {
        return attribute == null ? null : attribute.name().toLowerCase();
    }

    /**
     * Convierte el valor de base de datos al enum de Java.
     *
     * @param dbData Valor en minúsculas desde la base de datos
     * @return Enum en mayúsculas, o null si dbData es null o vacío
     */
    @Override
    public E convertToEntityAttribute(String dbData) {
        return (dbData == null || dbData.isEmpty())
                ? null
                : Enum.valueOf(enumClass, dbData.toUpperCase());
    }
}