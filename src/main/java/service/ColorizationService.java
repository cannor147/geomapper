package service;

import model.ColorizationTask;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Queue;

import static util.PaintUtils.fillArea;

public class ColorizationService {
    public void perform(BufferedImage image, Queue<ColorizationTask> tasks) {
        tasks.forEach(task -> perform(image, task));
    }

    public void perform(BufferedImage image, ColorizationTask task) {
        Arrays.stream(task.getTerritory().getCoordinates())
                .forEach(coordinate -> fillArea(image, coordinate, task.getRgbColor()));
    }
}
