package com.github.cannor147.request.colorization;

import com.github.cannor147.model.Color;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ColorizationParameter {
    private static final ColorizationParameter EMPTY_PARAMETER = new ColorizationParameter(null, null);

    private final Color color;
    private final Double value;

    public static ColorizationParameter empty() {
        return EMPTY_PARAMETER;
    }

    public ColorizationParameter(Color color) {
        this(color, null);
    }

    public <N extends Number> ColorizationParameter(N value) {
        this(null, value.doubleValue());
    }

    public Optional<Color> getColor() {
        return Optional.ofNullable(color);
    }

    public Optional<Double> getValue() {
        return Optional.ofNullable(value);
    }
}
