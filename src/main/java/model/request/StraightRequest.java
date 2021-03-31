package model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.Color;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StraightRequest {
    private String configuration;
    private Map<Color, List<String>> colorToTerritoriesMap;
    private Color defaultColor;
}
