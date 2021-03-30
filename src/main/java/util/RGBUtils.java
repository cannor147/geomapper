package util;

import lombok.experimental.UtilityClass;
import model.rgb.RGB;
import model.rgb.RGBColor;
import model.rgb.RGBDistance;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@UtilityClass
public class RGBUtils {
    public static List<RGBColor> generateScheme(RGB from, RGB to) {
        final RGBDistance distance = RGBDistance.between(to, from);
        return IntStream.range(0, 101)
                .mapToDouble(x -> (double) x / 100)
                .mapToObj(distance::multiply)
                .map(from::add)
                .map(RGB::asColor)
                .collect(Collectors.toList());
    }
}
