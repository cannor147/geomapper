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
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class GeoMapper {
    private static final String GEO_MAPS = "geomaps.json";
    public static final String PNG = "png";

    private final RequestService requestService;
    private final ResourceReader resourceReader;
    private final Map<String, GeoMapDto> nameToGeoMapMap;

    public GeoMapper() throws IOException {
        this.requestService = new RequestService();
        this.resourceReader = new ResourceReader();
        this.nameToGeoMapMap = Namer.createMap(resourceReader.readJson(GEO_MAPS, GeoMapDto[].class));
    }

    public BufferedImage createMap(Request request) {
        return requestService.handleRequest(request);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void createMapToFile(Request request, File file) throws IOException {
        file.mkdirs();
        ImageIO.write(createMap(request), PNG, file);
    }

    public GeoMap findGeoMap(String geoMapName) throws IOException {
        if (!nameToGeoMapMap.containsKey(Namer.normalize(geoMapName))) {
            throw new IllegalArgumentException("No such geo map.");
        }

        final GeoMapDto dto = nameToGeoMapMap.get(geoMapName.toLowerCase());
        final Map<String, Territory> nameToTerritoryMap = Arrays.stream(dto.getDataFilePaths())
                .map(path -> resourceReader.safeReadJson(path, Territory[].class))
                .filter(Objects::nonNull)
                .flatMap(Arrays::stream)
                .collect(Collectors.collectingAndThen(toList(), Namer::createMap));
        final BufferedImage map = resourceReader.readImage(dto.getMapFilePath());
        final BufferedImage background = dto.getBackgroundFilePath() != null
                ? resourceReader.readImage(dto.getBackgroundFilePath()) : null;
        return new GeoMap(dto.getName(), nameToTerritoryMap, map, background);
    }

    public static Number safeParseNumber(String number) {
        try {
            final String text = number.replace(",", "").replace("%", "").trim();
            return NumberFormat.getNumberInstance().parse(text);
        } catch (ParseException e) {
            return null;
        }
    }
}
