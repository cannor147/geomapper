package com.github.cannor147.painter;

import lombok.experimental.UtilityClass;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Queue;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@UtilityClass
public class Painter {
    private static final Point[] SHIFTS = new Point[]{
            new Point(0, +1),
            new Point(0, -1),
            new Point(+1, 0),
            new Point(-1, 0),
    };

    public static void fillArea(BufferedImage image, Point point, RGBColor rgbColor) {
        final RGBColor originalColor = getRGBColor(image, point);
        if (originalColor.equals(rgbColor)) {
            return;
        }

        final Set<Point> area = new HashSet<>();
        final Queue<Point> queue = new LinkedList<>();
        queue.add(point);
        area.add(point);

        while (!queue.isEmpty()) {
            final Point currentPoint = queue.poll();
            Arrays.stream(SHIFTS)
                    .map(Point::new)
                    .peek(p -> p.translate(currentPoint.x, currentPoint.y))
                    .filter(p -> p.x >= 0 && p.x < image.getWidth())
                    .filter(p -> p.y >= 0 && p.y < image.getHeight())
                    .filter(Predicate.not(area::contains))
                    .filter(p -> originalColor.equals(getRGBColor(image, p)))
                    .forEach(Stream.<Consumer<Point>>of(queue::add, area::add).reduce(Consumer::andThen).get());
        }

        area.forEach(c -> fillPoint(image, c, rgbColor));
    }

    public static void fillPoint(BufferedImage image, Point point, RGBColor rgbColor) {
        image.setRGB(point.x, point.y, rgbColor.toInt());
    }

    private static RGBColor getRGBColor(BufferedImage image, Point point) {
        return RGBColor.fromInt(image.getRGB(point.x, point.y));
    }

    public static List<RGBColor> generateScheme(RGB from, RGB to) {
        return generateScheme(from, to, 100);
    }

    public static List<RGBColor> generateScheme(RGB from, RGB to, int count) {
        final RGBDistance distance = RGBDistance.between(to, from);
        return IntStream.range(0, count + 1)
                .mapToDouble(x -> (double) x / count)
                .mapToObj(distance::multiply)
                .map(from::add)
                .map(RGB::asColor)
                .collect(Collectors.toList());
    }
}
