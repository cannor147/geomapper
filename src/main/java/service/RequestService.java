package service;

import configuration.Configuration;
import configuration.Configurer;
import lombok.RequiredArgsConstructor;
import model.Color;
import model.ColorizationTask;
import model.Territory;
import model.request.Request;
import model.request.ScaleRequest;
import model.request.StraightRequest;
import model.rgb.RGBColor;
import util.RGBUtils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.awt.image.BufferedImage.TYPE_3BYTE_BGR;

@RequiredArgsConstructor
public class RequestService {
    private final Configurer configurer;
    private final ColorizationService colorizationService;

    public RequestService(Configurer configurer) {
        this.configurer = configurer;
        this.colorizationService = new ColorizationService();
    }

    public BufferedImage handleRequest(StraightRequest request) throws IOException {
        final Configuration configuration = configurer.findConfiguration(request.getConfiguration());
        final BufferedImage image = deepCopy(configuration.getMap());

        final Queue<ColorizationTask> tasks = request.getColorToTerritoriesMap().entrySet().stream()
                .flatMap(entry -> entry.getValue().stream()
                        .map(territory -> configuration.getNameToTerritoryMap().get(territory))
                        .filter(Objects::nonNull)
                        .map(territory -> new ColorizationTask(territory, entry.getKey().getRgbColor())))
                .collect(Collectors.toCollection(LinkedList::new));

        tasks.addAll(createDefaultTasks(configuration, request, tasks));
        colorizationService.perform(image, tasks);
        return image;
    }

    public BufferedImage handleRequest(ScaleRequest request) throws IOException {
        final Configuration configuration = configurer.findConfiguration(request.getConfiguration());
        final BufferedImage image = deepCopy(configuration.getMap());

        final double distance = request.getMaxValue() - request.getMinValue();
        final Color minColor = request.getMinColor();
        final Color maxColor = request.getMaxColor();
        final List<RGBColor> scheme = RGBUtils.generateScheme(minColor.getRgbColor(), maxColor.getRgbColor());
        final Queue<ColorizationTask> tasks = request.getTerritoryToValueMap().entrySet().stream()
                .filter(entry -> configuration.getNameToTerritoryMap().containsKey(entry.getKey()))
                .map(entry -> {
                    final int index = (int) Math.round((entry.getValue() - request.getMinValue()) / distance * 100);
                    final Territory territory = configuration.getNameToTerritoryMap().get(entry.getKey());
                    return new ColorizationTask(territory, scheme.get(Math.max(Math.min(index, 100), 0)));
                })
                .collect(Collectors.toCollection(LinkedList::new));

        tasks.addAll(createDefaultTasks(configuration, request, tasks));
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

    private static BufferedImage deepCopy(BufferedImage original) {
        final BufferedImage result = new BufferedImage(original.getWidth(), original.getHeight(), TYPE_3BYTE_BGR);
        result.getGraphics().drawImage(original, 0, 0, null);
        return result;
    }
}
