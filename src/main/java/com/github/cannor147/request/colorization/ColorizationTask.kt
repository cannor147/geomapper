package com.github.cannor147.request.colorization;

import com.github.cannor147.painter.RGBColor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.awt.*;
import java.util.List;

@Data
@RequiredArgsConstructor
public class ColorizationTask {
    private final List<Point> points;
    private final RGBColor rgbColor;
    private final boolean onlyPixel;
}
