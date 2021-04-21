package com.github.cannor147.painter;

public interface RGB {
    int getRed();
    int getGreen();
    int getBlue();

    RGB add(RGB other);
    RGB subtract(RGB other);
    RGB multiply(double proportion);

    RGBColor asColor();
}
