package com.github.cannor147.service;

import com.github.cannor147.configuration.Configuration;
import com.github.cannor147.configuration.Configurer;
import lombok.RequiredArgsConstructor;
import com.github.cannor147.model.ColorizationTask;
import com.github.cannor147.model.Territory;
import com.github.cannor147.model.request.Request;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class RequestService {
    private final Configurer configurer;
    private final ColorizationService colorizationService;

    public RequestService(Configurer configurer) {
        this.configurer = configurer;
        this.colorizationService = new ColorizationService();
    }

    public BufferedImage handleRequest(Request request) throws IOException {
        final Configuration configuration = configurer.findConfiguration(request.getConfiguration());
        final Queue<ColorizationTask> tasks = request.toTasks(configuration);
        tasks.addAll(createDefaultTasks(configuration, request, tasks));

        final BufferedImage image = configuration.copyMap();
        colorizationService.perform(image, tasks);
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
}
