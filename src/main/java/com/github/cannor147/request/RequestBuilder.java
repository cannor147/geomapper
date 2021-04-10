package com.github.cannor147.request;

import com.github.cannor147.model.Color;
import com.github.cannor147.model.GeoMap;
import com.github.cannor147.model.Territory;
import com.github.cannor147.painter.RGBColor;
import com.github.cannor147.request.colorization.ColorizationParameter;
import com.github.cannor147.request.colorization.ColorizationScheme;
import com.github.cannor147.request.colorization.ColorizationTask;
import com.github.cannor147.request.colorization.StraightColorizationScheme;
import lombok.RequiredArgsConstructor;
import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

@SuppressWarnings({"unused", "UnusedReturnValue"})
@RequiredArgsConstructor
public class RequestBuilder {
    private final GeoMap geoMap;
    private final Color defaultColor = Color.SILVER;
    private final UnofficialStateBehavior unofficialStateBehavior = UnofficialStateBehavior.INCLUDE_UNMENTIONED;
    private final Map<Territory, ColorizationParameter> territoryToParameterMap = new HashMap<>();
    private final Map<Territory, ColorizationScheme> territoryToSchemeMap = new HashMap<>();
    private ColorizationScheme currentScheme = new StraightColorizationScheme();

    public Request build() {
        territoryToSchemeMap.values().stream()
                .distinct()
                .forEach(colorizationScheme -> colorizationScheme.prepareForCalculation(territoryToParameterMap));
        return StreamEx.of(geoMap.territories())
                .mapToEntry(territoryToParameterMap::containsKey)
                .flatMapKeyValue((territory, mentioned) -> {
                    final Optional<Territory> owner = geoMap.findOwner(territory);
                    final Optional<Territory> inclusionOwner = owner
                            .filter(territoryToParameterMap::containsKey)
                            .filter(x -> mentioned && unofficialStateBehavior.isIncludeMentioned())
                            .or(() -> owner.filter(x -> !mentioned && unofficialStateBehavior.isIncludeUnmentioned()));
                    if (inclusionOwner.isPresent()) {
                        final RGBColor rgbColor = calculateColor(inclusionOwner.get());
                        final ColorizationTask borderTask = toTask(territory.getOfficialOwnerBorder(), rgbColor, true);
                        final ColorizationTask task = toTask(territory.getPoints(), rgbColor, false);
                        return Stream.of(borderTask, task);
                    }
                    return Stream.of(toTask(territory.getPoints(), calculateColor(territory), false));
                })
                .collect(collectingAndThen(toCollection(LinkedList::new), tasks -> new Request(tasks, geoMap)));
    }

    public Optional<Request> buildOptional() {
        return Optional.of(build());
    }

    public RequestBuilder changeScheme(ColorizationScheme colorizationScheme) {
        this.currentScheme = colorizationScheme;
        return this;
    }

    public RequestBuilder withColor(Pair<String, Color> data) {
        return with(StreamEx.of(data), ColorizationParameter::new);
    }

    public RequestBuilder withColor(String name, Color color) {
        return with(EntryStream.of(name, color), ColorizationParameter::new);
    }

    public RequestBuilder withColor(String[] names, Color color) {
        return with(StreamEx.of(names).mapToEntry(x -> color), ColorizationParameter::new);
    }

    public RequestBuilder withColor(Iterable<String> names, Color color) {
        return with(StreamEx.of(names.iterator()).mapToEntry(x -> color), ColorizationParameter::new);
    }

    public RequestBuilder withColors(Iterable<Pair<String, Color>> data) {
        return with(StreamEx.of(data.iterator()), ColorizationParameter::new);
    }

    public RequestBuilder withColors(Map<String, Color> data) {
        return with(EntryStream.of(data), ColorizationParameter::new);
    }

    public <N extends Number> RequestBuilder withValue(Pair<String, N> data) {
        return with(StreamEx.of(data), ColorizationParameter::new);
    }

    public <N extends Number> RequestBuilder withValue(String name, N value) {
        return with(EntryStream.of(name, value), ColorizationParameter::new);
    }

    public <N extends Number> RequestBuilder withValues(Map<String, N> data) {
        return with(EntryStream.of(data), ColorizationParameter::new);
    }

    public <N extends Number> RequestBuilder withValues(Iterable<Pair<String, N>> data) {
        return with(StreamEx.of(data.iterator()), ColorizationParameter::new);
    }

    public <N extends Number> RequestBuilder withValues(List<String> names, List<N> values) {
        return with(EntryStream.zip(names, values), ColorizationParameter::new);
    }

    private <T> RequestBuilder with(StreamEx<Pair<String, T>> stream, Function<T, ColorizationParameter> parameterizer) {
        return with(stream.mapToEntry(Pair::getKey, Pair::getValue), parameterizer);
    }

    private <T> RequestBuilder with(EntryStream<String, T> stream, Function<T, ColorizationParameter> parameterizer) {
        stream.mapKeys(geoMap::find)
                .flatMapKeys(Optional::stream)
                .filterKeys(Predicate.not(territoryToParameterMap::containsKey))
                .mapValues(parameterizer)
                .forKeyValue(((territory, colorizationParameter) -> {
                    territoryToParameterMap.put(territory, colorizationParameter);
                    territoryToSchemeMap.put(territory, currentScheme);
                }));
        return this;
    }

    private RGBColor calculateColor(Territory territory) {
        final ColorizationScheme scheme = territoryToSchemeMap.getOrDefault(territory, currentScheme);
        return scheme.calculateColor(territoryToParameterMap.getOrDefault(territory, ColorizationParameter.empty()));
    }

    private ColorizationTask toTask(Point[] points, RGBColor color, boolean onlyPixel) {
        return StreamEx.ofNullable(points)
                .flatMap(Arrays::stream)
                .collect(collectingAndThen(toList(), pointList -> new ColorizationTask(pointList, color, onlyPixel)));
    }
}
