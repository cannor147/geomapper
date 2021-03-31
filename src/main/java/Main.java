import model.Color;
import model.request.ScaleRequest;
import model.request.StraightRequest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Main {
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
        final List<String> eaue = List.of("Russia", "Belarus", "Kazakhstan", "Kyrgyzstan", "Armenia");
        final Map<Color, List<String>> map = Map.of(Color.RED, eaue, Color.BLUE, eu);
        final StraightRequest request = new StraightRequest("countries", map, Color.GREEN);
        final BufferedImage image = geoMapper.handleRequest(request);
        ImageIO.write(image, "png", new File("straight.png"));
    }

    public static void createScale(GeoMapper geoMapper) throws IOException {
        final Map<String, Double> map = Map.of("Russia", 144.4, "Ukraine", 44.39, "Belarus", 9.467, "Kazakhstan", 20.0, "Uzbekistan", 39.0, "Azerbaijan", 4.3);
        final ScaleRequest request = new ScaleRequest("countries", map, Color.BLUE, Color.RED);
        final BufferedImage image = geoMapper.handleRequest(request);
        ImageIO.write(image, "png", new File("scale.png"));
    }
}
