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
        final Map<Color, List<String>> map = Map.of(Color.RED, List.of("Russia", "Ukraine"), Color.BLUE, List.of("Belarus"));
        final StraightRequest request = new StraightRequest("countries", map, Color.GREEN);
        final BufferedImage image = geoMapper.handleRequest(request);
        ImageIO.write(image, "png", new File("straight.png"));
    }

    public static void createScale(GeoMapper geoMapper) throws IOException {
        final Map<String, Double> map = Map.of("Russia", 144.4, "Ukraine", 44.39, "Belarus", 9.467);
        final ScaleRequest request = new ScaleRequest("countries", map, Color.GREEN, Color.RED);
        final BufferedImage image = geoMapper.handleRequest(request);
        ImageIO.write(image, "png", new File("scale.png"));
    }
}
