package com.example.demo.utils;

import com.example.demo.commons.enums.InventoryLogType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class InventoryLogTypeConverter implements AttributeConverter<InventoryLogType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(InventoryLogType attribute) {
        if (attribute == null) return null;
        return attribute.getCode();
    }

    @Override
    public InventoryLogType convertToEntityAttribute(Integer dbData) {
        if (dbData == null) return null;
        return InventoryLogType.fromCode(dbData);
    }
}
