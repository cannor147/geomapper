package com.github.cannor147.configuration;

import com.github.cannor147.model.Territory;
import com.github.cannor147.resources.ResourceReader;
import com.github.cannor147.namer.Namer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

public class Configurer {
    private static final String CONFIGURATIONS = "configurations.json";

    private final Map<String, ConfigurationDto> nameToConfigurationMap;
    private final ResourceReader resourceReader;

    public Configurer(ResourceReader resourceReader) throws IOException {
        this.resourceReader = resourceReader;
        this.nameToConfigurationMap = Namer.createMap(resourceReader.readJson(CONFIGURATIONS, ConfigurationDto[].class));
    }

    public Configuration findConfiguration(String configurationName) throws IOException {
        if (!nameToConfigurationMap.containsKey(configurationName.toLowerCase())) {
            throw new IllegalArgumentException("No such com.github.cannor147.configuration");
        }

        final ConfigurationDto configuration = nameToConfigurationMap.get(configurationName.toLowerCase());
        final Territory[] territories = resourceReader.readJson(configuration.getDataFilePath(), Territory[].class);
        final Map<String, Territory> nameToTerritoryMap = Namer.createMap(territories);
        final BufferedImage map = resourceReader.readImage(configuration.getMapFilePath());
        return new Configuration(configuration.getName(), nameToTerritoryMap, map);
    }
}
