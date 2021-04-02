package com.github.cannor147.service;

import com.github.cannor147.model.ColorizationTask;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Queue;

import static com.github.cannor147.util.PaintUtils.fillArea;

public class ColorizationService {
    public void perform(BufferedImage image, Queue<ColorizationTask> tasks) {
        tasks.forEach(task -> perform(image, task));
    }

    public void perform(BufferedImage image, ColorizationTask task) {
        Arrays.stream(task.getTerritory().getPoints())
                .forEach(coordinate -> fillArea(image, coordinate, task.getRgbColor()));
    }
}
