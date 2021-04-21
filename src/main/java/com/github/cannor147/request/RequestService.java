package com.github.cannor147.request;

import com.github.cannor147.model.Color;
import com.github.cannor147.painter.PainterKt;
import com.github.cannor147.request.colorization.ColorizationTask;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Optional;
import java.util.Queue;

public class RequestService {
    public BufferedImage handleRequest(Request request) {
        final BufferedImage image = request.getGeoMap().copyMap();
        perform(image, request.getTasks());

        Optional.ofNullable(request.getGeoMap().getBackground())
                .map(background -> PainterKt.findArea(background, Color.WHITE.getRgbColor()))
                .orElseGet(Collections::emptySet)
                .forEach(point -> PainterKt.fillPoint(image, point, Color.WHITE.getRgbColor()));
        return image;
    }

    private void perform(BufferedImage image, Queue<ColorizationTask> tasks) {
        tasks.forEach(task -> perform(image, task));
    }

    private void perform(BufferedImage image, ColorizationTask task) {
        if (task.getOnlyPixel()) {
            task.getPoints().forEach(point -> PainterKt.fillPoint(image, point, task.getRgbColor()));
        } else {
            task.getPoints().forEach(point -> PainterKt.fillArea(image, point, task.getRgbColor()));
        }
    }
}
