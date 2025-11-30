package com.example.demo.utils;

import com.example.demo.commons.enums.AccountStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AccountStatusConverter implements AttributeConverter<AccountStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(AccountStatus status) {
        return status != null ? status.getCode() : null;
    }

    @Override
    public AccountStatus convertToEntityAttribute(Integer dbData) {
        return dbData != null ? AccountStatus.fromCode(dbData) : null;
    }
}
