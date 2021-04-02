package com.github.cannor147.request;

import com.github.cannor147.configuration.Configuration;
import com.github.cannor147.model.Color;
import lombok.RequiredArgsConstructor;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Optional;

@RequiredArgsConstructor
public abstract class RequestBuilder {
    protected final Configuration configuration;
    protected Color defaultColor = Color.SILVER;

    public abstract Request build();

    @SuppressWarnings("unused")
    public Optional<Request> buildOptional() {
        return Optional.of(build());
    }

    public static Number safeParseNumber(String number) {
        try {
            return NumberFormat.getNumberInstance().parse(number.replace(",", "").trim());
        } catch (ParseException e) {
            return null;
        }
    }
}
