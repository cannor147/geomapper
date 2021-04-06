package com.github.cannor147.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.github.cannor147.namer.Synonymized;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeoMapDto implements Synonymized {
    private String name;
    private String[] synonyms;
    @JsonProperty("data")
    private String[] dataFilePaths;
    @JsonProperty("map")
    private String mapFilePath;
    @JsonProperty("background")
    private String backgroundFilePath;
}
