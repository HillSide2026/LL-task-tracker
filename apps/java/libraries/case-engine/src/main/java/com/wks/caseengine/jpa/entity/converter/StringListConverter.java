package com.wks.caseengine.jpa.entity.converter;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class StringListConverter implements AttributeConverter<List<String>, String> {

	private static final Gson gson = new Gson();
	private static final Type LIST_TYPE = new TypeToken<List<String>>() {
	}.getType();

	@Override
	public String convertToDatabaseColumn(List<String> attribute) {
		return attribute != null ? gson.toJson(attribute) : "[]";
	}

	@Override
	public List<String> convertToEntityAttribute(String dbData) {
		return dbData != null && !dbData.isEmpty() ? gson.fromJson(dbData, LIST_TYPE) : List.of();
	}
}
