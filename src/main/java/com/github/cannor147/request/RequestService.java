package com.github.cannor147.request;

import com.github.cannor147.configuration.Configuration;
import com.github.cannor147.model.Territory;
import lombok.RequiredArgsConstructor;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.github.cannor147.painter.Painter.fillArea;

@RequiredArgsConstructor
public class RequestService {

    public BufferedImage handleRequest(Request request) {
        final Queue<ColorizationTask> tasks = request.getTasks();
        tasks.addAll(createDefaultTasks(request.getConfiguration(), request, tasks));

        final BufferedImage image = request.getConfiguration().copyMap();
        perform(image, tasks);
        return image;
    }

    private Queue<ColorizationTask> createDefaultTasks(Configuration configuration, Request request,
                                                       Queue<ColorizationTask> tasks) {
        final Set<Territory> usedTerritories = tasks.stream()
                .map(ColorizationTask::getTerritory)
                .collect(Collectors.toSet());
        return configuration.getNameToTerritoryMap().values().stream()
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
