package model.request;

import lombok.RequiredArgsConstructor;
import model.Color;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings({"unused", "UnusedReturnValue"})
@RequiredArgsConstructor
public class ScaleRequestBuilder {
    private final String configuration;
    private final Map<String, Double> territoryToValueMap = new HashMap<>();
    private Color maxColor = Color.GREEN;
    private Color minColor = Color.RED;
    private Color defaultColor = Color.SILVER;
    private Double maxValue;
    private Double minValue;

    public <N extends Number> ScaleRequestBuilder append(String territory, N value) {
        territoryToValueMap.put(territory, value.doubleValue());
        return this;
    }

    public <N extends Number> ScaleRequestBuilder append(Pair<String, N> territoryToValue) {
        return append(territoryToValue.getKey(), territoryToValue.getValue().doubleValue());
    }

    public <N extends Number> ScaleRequestBuilder append(Map.Entry<String, N> territoryToValue) {
        return append(territoryToValue.getKey(), territoryToValue.getValue().doubleValue());
    }

    public <N extends Number> ScaleRequestBuilder appendAll(Map<String, N> territoryToValueMap) {
        territoryToValueMap.forEach(this::append);
        return this;
    }

    public <N extends Number> ScaleRequestBuilder appendAll(Iterable<Pair<String, N>> territoryToValueList) {
        territoryToValueList.forEach(this::append);
        return this;
    }

    public ScaleRequestBuilder useColor(Color maxColor, Color minColor) {
        this.maxColor = maxColor;
        this.minColor = minColor;
        return this;
    }

    public ScaleRequestBuilder useColor(Color maxColor) {
        return useColor(maxColor, maxColor.getDefaultOpposite());
    }

    public ScaleRequestBuilder reverseColors() {
        return useColor(minColor, maxColor);
    }

    public ScaleRequestBuilder useDefaultColor(Color defaultColor) {
        this.defaultColor = defaultColor;
        return this;
    }

    public <N extends Number> ScaleRequestBuilder customizeMaximum(N maxValue) {
        this.maxValue = maxValue.doubleValue();
        return this;
    }

    public <N extends Number> ScaleRequestBuilder customizeMinimum(N minValue) {
        this.minValue = minValue.doubleValue();
        return this;
    }

    public <N1 extends Number, N2 extends Number> ScaleRequestBuilder customizeMinMax(N1 minValue, N2 maxValue) {
        this.minValue = minValue.doubleValue();
        this.maxValue = maxValue.doubleValue();
        return this;
    }

    public ScaleRequest build() {
        maxValue = Optional.ofNullable(maxValue).orElseGet(() -> territoryToValueMap.values().stream().mapToDouble(x -> x).max().orElse(100.0));
        minValue = Optional.ofNullable(minValue).orElseGet(() -> territoryToValueMap.values().stream().mapToDouble(x -> x).min().orElse(0.0));
        return new ScaleRequest(configuration, territoryToValueMap, maxColor, minColor, defaultColor, maxValue, minValue);
    }
}
