package com.wks.caseengine.jpa.entity.converter;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wks.caseengine.cases.instance.accounts.AccountsEvent;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class AccountsEventListConverter implements AttributeConverter<List<AccountsEvent>, String> {

	private static final Gson gson = new Gson();
	private static final Type LIST_TYPE = new TypeToken<List<AccountsEvent>>() {
	}.getType();

	@Override
	public String convertToDatabaseColumn(List<AccountsEvent> attribute) {
		return attribute != null ? gson.toJson(attribute) : "[]";
	}

	@Override
	public List<AccountsEvent> convertToEntityAttribute(String dbData) {
		return dbData != null && !dbData.isEmpty() ? gson.fromJson(dbData, LIST_TYPE) : List.of();
	}
}
