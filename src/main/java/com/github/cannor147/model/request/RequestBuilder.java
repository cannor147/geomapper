package com.github.cannor147.model.request;

import com.github.cannor147.configuration.Configuration;
import com.github.cannor147.model.Color;
import lombok.RequiredArgsConstructor;

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
}
