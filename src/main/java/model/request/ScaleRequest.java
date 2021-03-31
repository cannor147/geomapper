package model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.Color;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScaleRequest {
    private String configuration;
    private Map<String, Double> territoryToValueMap;
    private Color color;
    private Color defaultColor;
    private double minValue;
    private double maxValue;

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public ScaleRequest(String configuration, Map<String, Double> territoryToValueMap, Color color, Color defaultColor) {
        this(configuration, territoryToValueMap, color, defaultColor,
                territoryToValueMap.values().stream().mapToDouble(x -> x).min().getAsDouble(),
                territoryToValueMap.values().stream().mapToDouble(x -> x).max().getAsDouble());
    }
}
