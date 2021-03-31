package model.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import model.Color;

import java.util.Map;

@Data
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class ScaleRequest implements Request {
    private final String configuration;
    private final Map<String, Double> territoryToValueMap;
    private final Color maxColor;
    private final Color minColor;
    private final Color defaultColor;
    private final double maxValue;
    private final double minValue;
}
