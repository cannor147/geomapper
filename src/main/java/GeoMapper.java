import configuration.Configuration;
import configuration.Configurer;
import model.Color;
import model.ColorizationTask;
import model.Territory;
import model.request.ScaleRequest;
import model.request.StraightRequest;
import model.rgb.RGBColor;
import resources.ResourceReader;
import service.ColorizationService;
import util.RGBUtils;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.awt.image.BufferedImage.TYPE_3BYTE_BGR;

public class GeoMapper {
    private final Configurer configurer;
    private final ColorizationService colorizationService;

    public GeoMapper() throws IOException {
        configurer = new Configurer(new ResourceReader());
        colorizationService = new ColorizationService();
    }

    public BufferedImage handleRequest(StraightRequest request) throws IOException {
        final Configuration configuration = configurer.findConfiguration(request.getConfiguration());
        final BufferedImage image = deepCopy(configuration.getMap());

        final Queue<ColorizationTask> tasks = request.getColorToTerritoriesMap().entrySet().stream()
                .flatMap(entry -> entry.getValue().stream()
                        .map(territory -> configuration.getNameToTerritoryMap().get(territory))
                        .filter(Objects::nonNull)
                        .map(territory -> new ColorizationTask(territory, entry.getKey().getNormal())))
                .collect(Collectors.toCollection(LinkedList::new));
        colorizationService.perform(image, tasks);
        return image;
    }

    public BufferedImage handleRequest(ScaleRequest request) throws IOException {
        final Configuration configuration = configurer.findConfiguration(request.getConfiguration());
        final BufferedImage image = deepCopy(configuration.getMap());

        final double distance = request.getMaxValue() - request.getMinValue();
        final Color color = request.getColor();
        final List<RGBColor> scheme = RGBUtils.generateScheme(color.getPale(), color.getNormal());
        final Queue<ColorizationTask> tasks = request.getTerritoryToValueMap().entrySet().stream()
                .filter(entry -> configuration.getNameToTerritoryMap().containsKey(entry.getKey()))
                .map(entry -> {
                    final int index = (int) Math.round((entry.getValue() - request.getMinValue()) / distance * 100);
                    final Territory territory = configuration.getNameToTerritoryMap().get(entry.getKey());
                    return new ColorizationTask(territory, scheme.get(Math.max(Math.min(index, 100), 0)));
                })
                .collect(Collectors.toCollection(LinkedList::new));
        colorizationService.perform(image, tasks);
        return image;
    }

    private static BufferedImage deepCopy(BufferedImage original) {
        final BufferedImage result = new BufferedImage(original.getWidth(), original.getHeight(), TYPE_3BYTE_BGR);
        result.getGraphics().drawImage(original, 0, 0, null);
        return result;
    }
}
