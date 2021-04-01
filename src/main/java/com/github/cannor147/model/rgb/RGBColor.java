package com.github.cannor147.model.rgb;

import lombok.*;

import java.awt.*;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class RGBColor implements RGB {
    public static RGBColor of(int red, int green, int blue) {
        return new RGBColor(red & 0xff, green & 0xff, blue & 0xff);
    }

    public static RGBColor fromInt(int rgbInt) {
        return of(rgbInt >> 16, rgbInt >> 8, rgbInt);
    }

    private final int red;
    private final int green;
    private final int blue;

    @Override
    public RGB add(RGB other) {
        return of(this.red + other.getRed(), this.green + other.getGreen(), this.blue + other.getBlue());
    }

    @Override
    public RGB subtract(RGB other) {
        return of(this.red - other.getRed(), this.green - other.getGreen(), this.blue - other.getBlue());
    }

    @Override
    public RGB multiply(double proportion) {
        return of((int) (this.red * proportion), (int) (this.green * proportion), (int) (this.blue * proportion));
    }

    @Override
    public RGBColor asColor() {
        return this;
    }

    public Color toColor() {
        return new Color(red, green, blue);
    }

    public int toInt() {
        return toColor().getRGB();
    }
}
