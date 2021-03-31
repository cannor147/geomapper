package model.request;

import lombok.*;
import model.Color;

import java.util.List;
import java.util.Map;

@Data
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class StraightRequest implements Request {
    private final String configuration;
    private final Map<Color, List<String>> colorToTerritoriesMap;
    private final Color defaultColor;
}
