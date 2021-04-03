package com.github.cannor147.request;

import com.github.cannor147.model.GeoMap;
import com.github.cannor147.model.Territory;
import com.github.cannor147.painter.Painter;
import com.github.cannor147.painter.RGBColor;
import lombok.RequiredArgsConstructor;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.github.cannor147.painter.Painter.fillArea;

@RequiredArgsConstructor
public class RequestService {
    public static final RGBColor WHITE_COLOR = RGBColor.of(255, 255, 255);

    public BufferedImage handleRequest(Request request) {
        final Queue<ColorizationTask> tasks = request.getTasks();
        tasks.addAll(createDefaultTasks(request.getGeoMap(), request, tasks));

        final BufferedImage image = request.getGeoMap().copyMap();
        perform(image, tasks);
        Optional.ofNullable(request.getGeoMap().getBackground())
                .map(background -> Painter.findArea(background, WHITE_COLOR))
                .orElseGet(Collections::emptySet)
                .forEach(point -> Painter.fillPoint(image, point, WHITE_COLOR));
        return image;
    }

    private Queue<ColorizationTask> createDefaultTasks(GeoMap geoMap, Request request,
                                                       Queue<ColorizationTask> tasks) {
        final Set<Territory> usedTerritories = tasks.stream()
                .map(ColorizationTask::getTerritory)
                .collect(Collectors.toSet());
        return geoMap.getNameToTerritoryMap().values().stream()
                .distinct()
                .filter(Predicate.not(usedTerritories::contains))
                .map(territory -> new ColorizationTask(territory, request.getDefaultColor().getRgbColor()))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private void perform(BufferedImage image, Queue<ColorizationTask> tasks) {
        tasks.forEach(task -> perform(image, task));
    }

    private void perform(BufferedImage image, ColorizationTask task) {
        Arrays.stream(task.getTerritory().getPoints())
                .forEach(coordinate -> fillArea(image, coordinate, task.getRgbColor()));
    }
}
