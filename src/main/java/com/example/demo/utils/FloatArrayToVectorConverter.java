package com.example.demo.utils;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class FloatArrayToVectorConverter implements AttributeConverter<float[], String> {

    @Override
    public String convertToDatabaseColumn(float[] attribute) {
        if (attribute == null) return null;

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < attribute.length; i++) {
            sb.append(attribute[i]);
            if (i < attribute.length - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public float[] convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) return null;

        // Remove brackets and whitespace
        String cleaned = dbData.trim();
        if (cleaned.startsWith("[")) {
            cleaned = cleaned.substring(1);
        }
        if (cleaned.endsWith("]")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }

        if (cleaned.isEmpty()) return new float[0];

        String[] parts = cleaned.split(",");
        float[] result = new float[parts.length];

        for (int i = 0; i < parts.length; i++) {
            result[i] = Float.parseFloat(parts[i].trim());
        }

        return result;
    }
}