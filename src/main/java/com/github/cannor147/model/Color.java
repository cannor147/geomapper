package com.github.cannor147.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import com.github.cannor147.painter.RGBColor;

@Getter
@RequiredArgsConstructor
public enum Color {
    SILVER(RGBColor.of(192, 192, 192), null),
    GREY(RGBColor.of(121, 121, 121), SILVER),
    BLACK(RGBColor.of(0, 0, 0), GREY),

    PALE_RED(RGBColor.of(254, 198, 197), GREY),
    PALE_ORANGE(RGBColor.of(255, 188, 120), GREY),
    PALE_YELLOW(RGBColor.of(252, 219, 80), GREY),
    PALE_GREEN(RGBColor.of(190, 217, 186), GREY),
    PALE_BLUE(RGBColor.of(183, 183, 255), GREY),
    PALE_PURPLE(RGBColor.of(214, 170, 255), GREY),
    PALE_FUCHSIA(RGBColor.of(255, 179, 254), GREY),
    PALE_LIME(RGBColor.of(197, 255, 197), GREY),
    PALE_TEAL(RGBColor.of(175, 255, 255), GREY),
    PALE_BROWN(RGBColor.of(226, 197, 199), GREY),
    PALE_PINK(RGBColor.of(255, 186, 222), GREY),
    PALE_OLIVE(RGBColor.of(227, 228, 197), GREY),

    RED(RGBColor.of(244, 0, 2), PALE_RED),
    ORANGE(RGBColor.of(232, 115, 0), PALE_ORANGE),
    YELLOW(RGBColor.of(182, 145, 2), PALE_YELLOW),
    GREEN(RGBColor.of(73, 117, 66), PALE_GREEN),
    BLUE(RGBColor.of(50, 51, 255), PALE_BLUE),
    PURPLE(RGBColor.of(112, 0, 224), PALE_PURPLE),
    FUCHSIA(RGBColor.of(222, 0, 223), PALE_FUCHSIA),
    LIME(RGBColor.of(0, 198, 0), PALE_LIME),
    TEAL(RGBColor.of(0, 185, 187), PALE_TEAL),
    BROWN(RGBColor.of(154, 78, 78), PALE_BROWN),
    PINK(RGBColor.of(255, 36, 146), PALE_PINK),
    OLIVE(RGBColor.of(129, 129, 67), PALE_OLIVE),
    ;

    private final RGBColor rgbColor;
    private final Color defaultOpposite;
}
