package com.github.cannor147.model.request;

import com.github.cannor147.configuration.Configuration;
import lombok.*;
import com.github.cannor147.model.Color;
import com.github.cannor147.model.ColorizationTask;

import java.util.*;
import java.util.stream.Collectors;

@Data
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class StraightRequest implements Request {
    private final String configuration;
    private final Map<Color, List<String>> colorToTerritoriesMap;
    private final Color defaultColor;

    @Override
    public Queue<ColorizationTask> toTasks(Configuration configuration) {
        return colorToTerritoriesMap.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream()
                        .map(territory -> configuration.find(territory).orElse(null))
                        .filter(Objects::nonNull)
                        .map(territory -> new ColorizationTask(territory, entry.getKey().getRgbColor())))
                .collect(Collectors.toCollection(LinkedList::new));
    }
}
