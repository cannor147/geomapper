package model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import model.rgb.RGBColor;

@Getter
@RequiredArgsConstructor
public enum Color {
    GREY(RGBColor.of(160, 160, 160), null),
    SILVER(RGBColor.of(192, 192, 192), null),
    PALE_RED(RGBColor.of(255, 127, 127), SILVER),
    PALE_BLUE(RGBColor.of(127, 201, 255), SILVER),
    PALE_GREEN(RGBColor.of(127, 255, 142), SILVER),

    BLACK(RGBColor.of(0, 0, 0), SILVER),
    BLUE(RGBColor.of(0, 96, 255), PALE_BLUE),
    GREEN(RGBColor.of(0, 192, 24), PALE_GREEN),
    RED(RGBColor.of(255, 0, 0), PALE_RED);


    private final RGBColor rgbColor;
    private final Color defaultOpposite;
}
