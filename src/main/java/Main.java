import model.Color;
import model.request.ScaleRequest;
import model.request.ScaleRequestBuilder;
import model.request.StraightRequest;
import model.request.StraightRequestBuilder;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main {
    public static final String COUNTRIES = "countries";

    public static void main(String[] args) throws IOException {
        final GeoMapper geoMapper = new GeoMapper();
        createStraight(geoMapper);
        createScale(geoMapper);
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

    public static void createScale(GeoMapper geoMapper) throws IOException {
        final ScaleRequest request = new ScaleRequestBuilder(COUNTRIES)
                .append("Russia", 144.4)
                .append("Ukraine", 44.39)
                .append("Belarus", 9.467)
                .append("Kazakhstan", 20.0)
                .append("Uzbekistan", 39.0)
                .append("Azerbaijan", 4.3)
                .useColor(Color.GREEN)
                .build();
        geoMapper.createMapToFile(request, new File("scale.png"));
    }
}
