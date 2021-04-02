package com.github.cannor147;

import com.github.cannor147.model.GeoMap;
import com.github.cannor147.model.GeoMapDto;
import com.github.cannor147.model.Territory;
import com.github.cannor147.namer.Namer;
import com.github.cannor147.request.Request;
import com.github.cannor147.request.RequestService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class GeoMapper {
    private static final String GEO_MAPS = "geomaps.json";
    public static final String PNG = "png";

    private final RequestService requestService;
    private final ResourceReader resourceReader;
    private final Map<String, GeoMapDto> nameToGeoMapMap;

    public GeoMapper(ResourceReader resourceReader) throws IOException {
        this.requestService = new RequestService();
        this.resourceReader = resourceReader;
        this.nameToGeoMapMap = Namer.createMap(resourceReader.readJson(GEO_MAPS, GeoMapDto[].class));
    }

    public BufferedImage createMap(Request request) {
        return requestService.handleRequest(request);
    }

    public void createMapToFile(Request request, File file) throws IOException {
        ImageIO.write(createMap(request), PNG, file);
    }

    public GeoMap findGeoMap(String geoMapName) throws IOException {
        if (!nameToGeoMapMap.containsKey(geoMapName.toLowerCase())) {
            throw new IllegalArgumentException("No such geo map");
        }

        final GeoMapDto dto = nameToGeoMapMap.get(geoMapName.toLowerCase());
        final Territory[] territories = resourceReader.readJson(dto.getDataFilePath(), Territory[].class);
        final Map<String, Territory> nameToTerritoryMap = Namer.createMap(territories);
        final BufferedImage map = resourceReader.readImage(dto.getMapFilePath());
        return new GeoMap(dto.getName(), nameToTerritoryMap, map);
    }
}
