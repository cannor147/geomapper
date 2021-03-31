package model.rgb;

import lombok.*;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class RGBDistance implements RGB {
    public static RGBDistance of(int red, int green, int blue) {
        final int realRed = Math.max(Math.min(red, 255), -255);
        final int realGreen = Math.max(Math.min(green, 255), -255);
        final int realBlue = Math.max(Math.min(blue, 255), -255);
        return new RGBDistance(realRed, realGreen, realBlue);
    }
    public static RGBDistance between(RGB a, RGB b) {
        return of(a.getRed() - b.getRed(), a.getGreen() - b.getGreen(), a.getBlue() - b.getBlue());
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
        return RGBColor.of(red, green, blue);
    }
}
