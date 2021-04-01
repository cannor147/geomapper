package com.github.cannor147.model.request;

import com.github.cannor147.configuration.Configuration;
import lombok.*;
import com.github.cannor147.model.Color;
import com.github.cannor147.model.ColorizationTask;
import com.github.cannor147.model.rgb.RGBColor;
import com.github.cannor147.util.RGBUtils;

import java.util.*;
import java.util.stream.Collectors;

@Data
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class StepRequest implements Request {
    private final String configuration;
    private final Map<String, Double> territoryToValueMap;
    private final Color maxColor;
    private final Color minColor;
    private final Color defaultColor;
    private final SortedSet<Double> valueSeparators;

    @Override
    public Queue<ColorizationTask> toTasks(Configuration configuration) {
        final List<Double> separators = new ArrayList<>(valueSeparators);
        final List<RGBColor> scheme = RGBUtils.generateScheme(minColor.getRgbColor(),
                maxColor.getRgbColor(), valueSeparators.size());
        return territoryToValueMap.entrySet().stream()
                .map(e -> configuration.find(e.getKey())
                        .map(t -> new ColorizationTask(t, scheme.get(findIndex(separators, e.getValue()))))
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    public static <T extends Comparable<T>> int findIndex(List<T> elements, T element) {
        if (element.compareTo(elements.get(0)) < 0) {
            return 0;
        } else if (element.compareTo(elements.get(elements.size() - 1)) > 0) {
            return elements.size();
        }

        final int index = Collections.binarySearch(elements, element);
        return (index + 1) * (index > 0 ? 1 : -1);
    }
}
