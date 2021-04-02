package com.github.cannor147.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import com.github.cannor147.painter.RGBColor;

@Data
@RequiredArgsConstructor
public class ColorizationTask {
    private final Territory territory;
    private final RGBColor rgbColor;
}
