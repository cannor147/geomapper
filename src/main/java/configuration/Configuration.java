package configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.Territory;

import java.awt.image.BufferedImage;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Configuration {
    private String name;
    private Map<String, Territory> nameToTerritoryMap;
    private BufferedImage map;
}
