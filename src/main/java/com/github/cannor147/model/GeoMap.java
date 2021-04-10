package com.github.cannor147.model;

import com.github.cannor147.namer.Namer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.function.Predicate;

import static java.awt.image.BufferedImage.TYPE_3BYTE_BGR;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.joining;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeoMap {
    private String name;
    private Map<String, Territory> nameToTerritoryMap;
    private BufferedImage map;
    private BufferedImage background;

    public Optional<Territory> find(String territoryName) {
        return Optional.ofNullable(territoryName)
                .map(Namer::normalize)
                .stream()
                .flatMap(x -> Arrays.stream(x.split("\\s+")))
                .filter(Predicate.not("the"::equals))
                .collect(collectingAndThen(joining(" "), name -> Optional.ofNullable(nameToTerritoryMap.get(name))));
    }

    public Optional<Territory> findOwner(Territory territory) {
        return Optional.ofNullable(territory.getOfficialOwner()).flatMap(this::find);
    }

    public Set<Territory> territories() {
        return new HashSet<>(nameToTerritoryMap.values());
    }

    public BufferedImage copyMap() {
        final BufferedImage result = new BufferedImage(map.getWidth(), map.getHeight(), TYPE_3BYTE_BGR);
        result.getGraphics().drawImage(map, 0, 0, null);
        return result;
    }
}
