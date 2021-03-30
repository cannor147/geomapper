package util;

import lombok.experimental.UtilityClass;
import model.Coordinates;
import model.rgb.RGBColor;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

@UtilityClass
public class PaintUtils {
    private static final Coordinates[] SHIFTS = new Coordinates[]{
            new Coordinates(0, +1),
            new Coordinates(0, -1),
            new Coordinates(+1, 0),
            new Coordinates(-1, 0),
    };

    public static void fillArea(BufferedImage image, Coordinates coordinates, RGBColor rgbColor) {
        final RGBColor originalColor = getRGBColor(image, coordinates);
        final Set<Coordinates> area = new HashSet<>();
        final Queue<Coordinates> queue = new LinkedList<>();
        queue.add(coordinates);
        area.add(coordinates);

        while (!queue.isEmpty()) {
            final Coordinates currentCoordinates = queue.poll();
            Arrays.stream(SHIFTS)
                    .map(shift -> CoordinatesUtils.sum(currentCoordinates, shift))
                    .filter(c -> c.getX() >= 0 && c.getX() < image.getWidth())
                    .filter(c -> c.getY() >= 0 && c.getY() < image.getHeight())
                    .filter(Predicate.not(area::contains))
                    .filter(c -> originalColor.equals(getRGBColor(image, c)))
                    .forEach(Stream.<Consumer<Coordinates>>of(queue::add, area::add).reduce(Consumer::andThen).get());
        }

        area.forEach(c -> image.setRGB(c.getX(), c.getY(), rgbColor.toInt()));
    }

    private static RGBColor getRGBColor(BufferedImage image, Coordinates coordinates) {
        return RGBColor.fromInt(image.getRGB(coordinates.getX(), coordinates.getY()));
    }
}
