package com.readmate.ReadMate.login.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public class FavoriteGenreConverter implements AttributeConverter<List<FavoriteGenre>, String> {
    private static final String SPLIT_CHAR = ",";

    @Override
    public String convertToDatabaseColumn(List<FavoriteGenre> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        return attribute.stream()
                .map(FavoriteGenre::name)
                .collect(Collectors.joining(SPLIT_CHAR));
    }

    @Override
    public List<FavoriteGenre> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        return Arrays.stream(dbData.split(SPLIT_CHAR))
                .map(FavoriteGenre::valueOf)
                .collect(Collectors.toList());
    }
}
