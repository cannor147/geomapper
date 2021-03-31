package configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.named.Synonymized;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigurationDto implements Synonymized {
    private String name;
    private String[] synonyms;
    @JsonProperty("data")
    private String dataFilePath;
    @JsonProperty("map")
    private String mapFilePath;
}
