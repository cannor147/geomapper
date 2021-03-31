package model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import model.rgb.RGBColor;

@Getter
@RequiredArgsConstructor
public enum Color {
    RED(RGBColor.of(255, 0, 0), RGBColor.of(255, 127, 127)),
    BLUE(RGBColor.of(0, 96, 255), RGBColor.of(127, 201, 255)),
    GREEN(RGBColor.of(0, 192, 24), RGBColor.of(127, 255, 142));

    private final RGBColor normal;
    private final RGBColor pale;
}
