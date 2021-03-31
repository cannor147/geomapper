package configuration;

import model.Territory;
import org.apache.commons.lang3.tuple.Pair;
import resources.ResourceReader;
import util.SynonymizedUtils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class Configurer {
    private static final String CONFIGURATIONS = "configurations.json";

    private final Map<String, ConfigurationDto> nameToConfigurationMap;
    private final ResourceReader resourceReader;

    public Configurer(ResourceReader resourceReader) throws IOException {
        this.resourceReader = resourceReader;
        this.nameToConfigurationMap = Arrays.stream(resourceReader.readJson(CONFIGURATIONS, ConfigurationDto[].class))
                .map(configurationDto -> Pair.of(SynonymizedUtils.findNames(configurationDto), configurationDto))
                .flatMap(pair -> pair.getKey().stream().map(name -> Pair.of(name, pair.getRight())))
                .collect(Collectors.toMap(Pair::getKey, Pair::getRight));
    }

    public Configuration findConfiguration(String configurationName) throws IOException {
        if (!nameToConfigurationMap.containsKey(configurationName)) {
            throw new IllegalArgumentException("No such configuration");
        }

        final ConfigurationDto configuration = nameToConfigurationMap.get(configurationName);
        final Territory[] territories = resourceReader.readJson(configuration.getDataFilePath(), Territory[].class);
        final Map<String, Territory> nameToTerritoryMap = Arrays.stream(territories)
                .map(territory -> Pair.of(SynonymizedUtils.findNames(territory), territory))
                .flatMap(pair -> pair.getKey().stream().map(name -> Pair.of(name, pair.getRight())))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
        final BufferedImage map = resourceReader.readImage(configuration.getMapFilePath());
        return new Configuration(configuration.getName(), nameToTerritoryMap, map);
    }
}
