package com.github.cannor147.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.github.cannor147.model.Territory;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
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

    public Optional<Territory> find(String territoryName) {
        return Arrays.stream(territoryName.trim().toLowerCase().split("\\s+"))
                .filter(Predicate.not("the"::equals))
                .collect(collectingAndThen(joining(" "), name -> Optional.ofNullable(nameToTerritoryMap.get(name))));
    }

    public BufferedImage copyMap() {
        final BufferedImage result = new BufferedImage(map.getWidth(), map.getHeight(), TYPE_3BYTE_BGR);
        result.getGraphics().drawImage(map, 0, 0, null);
        return result;
    }
}
