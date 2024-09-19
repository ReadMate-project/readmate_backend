package com.readmate.ReadMate.common;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Converter(autoApply = true)
public class ListToJsonConverter implements AttributeConverter<List<String>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert list to JSON", e);
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String content) {
        if (content == null) {
            return new ArrayList<>(); // null 대신 빈 리스트 반환
        }

        try {
            return objectMapper.readValue(content, new TypeReference<List<String>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error converting JSON to List", e);
        }
    }




}

