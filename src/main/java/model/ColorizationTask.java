package model;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import model.rgb.RGBColor;

@Data
@RequiredArgsConstructor
public class ColorizationTask {
    private final Territory territory;
    private final RGBColor rgbColor;
}
