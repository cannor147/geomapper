package com.github.cannor147.request;

import com.github.cannor147.model.Color;
import com.github.cannor147.painter.Painter;
import com.github.cannor147.request.colorization.ColorizationTask;
import lombok.RequiredArgsConstructor;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Optional;
import java.util.Queue;

@RequiredArgsConstructor
public class RequestService {
    public BufferedImage handleRequest(Request request) {
        final BufferedImage image = request.getGeoMap().copyMap();
        perform(image, request.getTasks());

        Optional.ofNullable(request.getGeoMap().getBackground())
                .map(background -> Painter.findArea(background, Color.WHITE.getRgbColor()))
                .orElseGet(Collections::emptySet)
                .forEach(point -> Painter.fillPoint(image, point, Color.WHITE.getRgbColor()));
        return image;
    }

    private void perform(BufferedImage image, Queue<ColorizationTask> tasks) {
        tasks.forEach(task -> perform(image, task));
    }

    private void perform(BufferedImage image, ColorizationTask task) {
        if (task.isOnlyPixel()) {
            task.getPoints().forEach(point -> Painter.fillPoint(image, point, task.getRgbColor()));
        } else {
            task.getPoints().forEach(point -> Painter.fillArea(image, point, task.getRgbColor()));
        }
    }
}
