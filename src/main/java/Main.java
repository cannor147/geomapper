import model.Color;
import model.request.*;
import resources.ResourceReader;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class Main {
    public static final String COUNTRIES = "countries";

    public static void main(String[] args) throws IOException, URISyntaxException {
        final ResourceReader resourceReader = new ResourceReader();
        final GeoMapper geoMapper = new GeoMapper();
        createStraight(geoMapper);
        createScale(resourceReader, geoMapper);
        createStep(resourceReader, geoMapper);
    }

    public static void createStraight(GeoMapper geoMapper) throws IOException {
        final List<String> eu = List.of("Sweden", "Finland", "Denmark", "Ireland", "France", "Germany",
                "Poland", "Czechia", "Slovakia", "Hungary", "Austria", "Romania", "Bulgaria",
                "Latvia", "Lithuania", "Estonia", "Greece", "Slovenia", "Croatia", "Italy",
                "Netherlands", "Belgium", "Luxembourg", "Spain", "Portugal", "Malta", "Cyprus");
        final StraightRequest request = new StraightRequestBuilder(COUNTRIES)
                .appendAll(Color.RED, "Russia", "Belarus", "Kazakhstan", "Kyrgyzstan", "Armenia")
                .appendAll(Color.BLUE, eu)
                .build();
        geoMapper.createMapToFile(request, new File("straight.png"));
    }

    public static void createScale(ResourceReader resourceReader, GeoMapper geoMapper) throws IOException, URISyntaxException {
        final ScaleRequest request = new ScaleRequestBuilder(COUNTRIES)
                .fromCsv(resourceReader.getResource("example/gdp.csv"), 1, 2)
                .useColor(Color.GREEN)
                .addLogarithmization(10)
                .build();
        geoMapper.createMapToFile(request, new File("scale.png"));
    }

    public static void createStep(ResourceReader resourceReader, GeoMapper geoMapper) throws IOException, URISyntaxException {
        final StepRequest request = new StepRequestBuilder(COUNTRIES)
                .fromCsv(resourceReader.getResource("example/hdr.csv"), 2, 3)
                .useColor(Color.BLUE, Color.RED)
                .withSeparators(0.8, 0.7, 0.55)
                .build();
        geoMapper.createMapToFile(request, new File("step.png"));
    }
}
