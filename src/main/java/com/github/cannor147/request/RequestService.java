package com.github.cannor147.request;

import com.github.cannor147.model.Color;
import com.github.cannor147.model.Territory;
import com.github.cannor147.painter.Painter;
import com.github.cannor147.painter.RGBColor;
import com.github.cannor147.request.colorization.ColorizationTask;
import lombok.RequiredArgsConstructor;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.function.Predicate;

import static com.github.cannor147.painter.Painter.fillArea;
import static java.util.stream.Collectors.*;

@RequiredArgsConstructor
public class RequestService {
    public static final RGBColor WHITE_COLOR = RGBColor.of(255, 255, 255);

    public BufferedImage handleRequest(Request request) {
        final BufferedImage image = request.getGeoMap().copyMap();
        final Queue<ColorizationTask> initialTasks = request.getTasks();

        final Map<Territory, RGBColor> mentionedTerritoryToColorMap = initialTasks.stream()
                .collect(toMap(ColorizationTask::getTerritory, ColorizationTask::getRgbColor, (a, b) -> a));
        request.getGeoMap().getNameToTerritoryMap().values().stream()
                .distinct()
                .filter(territory -> territory.getOfficialOwner() != null)
                .filter(territory -> {
                    if (mentionedTerritoryToColorMap.containsKey(territory)) {
                        return request.getUnofficialStateBehavior().isIncludeMentioned();
                    } else {
                        return request.getUnofficialStateBehavior().isIncludeUnmentioned();
                    }
                })
                .map(Territory::getOfficialOwnerBorder)
                .filter(Objects::nonNull)
                .flatMap(Arrays::stream)
                .forEach(borderPoint -> Painter.fillPoint(image, borderPoint, Color.SILVER.getRgbColor()));

        final LinkedList<ColorizationTask> tasks = initialTasks.stream()
                .map(task -> Optional.ofNullable(task.getTerritory().getOfficialOwner())
                        .flatMap(request.getGeoMap()::find)
                        .filter(owner -> request.getUnofficialStateBehavior().isIncludeMentioned())
                        .filter(mentionedTerritoryToColorMap::containsKey)
                        .map(owner -> new ColorizationTask(task.getTerritory(), mentionedTerritoryToColorMap.get(owner)))
                        .orElse(task))
                .collect(toCollection(LinkedList::new));
        request.getGeoMap().getNameToTerritoryMap().values().stream()
                .distinct()
                .filter(Predicate.not(mentionedTerritoryToColorMap::containsKey))
                .map(territory -> Optional.ofNullable(territory.getOfficialOwner())
                        .flatMap(request.getGeoMap()::find)
                        .filter(owner -> request.getUnofficialStateBehavior().isIncludeUnmentioned())
                        .filter(mentionedTerritoryToColorMap::containsKey)
                        .map(owner -> new ColorizationTask(territory, mentionedTerritoryToColorMap.get(owner))))
                .flatMap(Optional::stream)
                .forEach(tasks::add);
        perform(image, tasks);

        final Queue<ColorizationTask> defaultTasks = request.getGeoMap().getNameToTerritoryMap().values().stream()
                .distinct()
                .filter(territory -> {
                    if (territory.getOfficialOwner() == null) {
                        return !mentionedTerritoryToColorMap.containsKey(territory);
                    } else if (mentionedTerritoryToColorMap.containsKey(territory)) {
                        return false;
                    }
                    return !request.getUnofficialStateBehavior().isIncludeUnmentioned();
                })
                .map(territory -> new ColorizationTask(territory, request.getDefaultColor().getRgbColor()))
                .collect(toCollection(LinkedList::new));
        perform(image, defaultTasks);

        Optional.ofNullable(request.getGeoMap().getBackground())
                .map(background -> Painter.findArea(background, WHITE_COLOR))
                .orElseGet(Collections::emptySet)
                .forEach(point -> Painter.fillPoint(image, point, WHITE_COLOR));
        return image;
    }

    private void perform(BufferedImage image, Queue<ColorizationTask> tasks) {
        tasks.forEach(task -> perform(image, task));
    }

    private void perform(BufferedImage image, ColorizationTask task) {
        Arrays.stream(task.getTerritory().getPoints())
                .forEach(coordinate -> fillArea(image, coordinate, task.getRgbColor()));
    }
}
