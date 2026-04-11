package com.wks.caseengine.jpa.entity.converter;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wks.caseengine.cases.instance.admin.AdminEvent;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class AdminEventListConverter implements AttributeConverter<List<AdminEvent>, String> {

	private static final Gson gson = new Gson();
	private static final Type LIST_TYPE = new TypeToken<List<AdminEvent>>() {
	}.getType();

	@Override
	public String convertToDatabaseColumn(List<AdminEvent> attribute) {
		return attribute != null ? gson.toJson(attribute) : "[]";
	}

	@Override
	public List<AdminEvent> convertToEntityAttribute(String dbData) {
		return dbData != null && !dbData.isEmpty() ? gson.fromJson(dbData, LIST_TYPE) : List.of();
	}
}
