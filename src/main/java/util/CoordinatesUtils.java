package util;

import lombok.experimental.UtilityClass;
import model.Coordinates;

@UtilityClass
public class CoordinatesUtils {
    public static Coordinates sum(Coordinates first, Coordinates second) {
        return new Coordinates(first.getX() + second.getX(), first.getY() + second.getY());
    }
}
