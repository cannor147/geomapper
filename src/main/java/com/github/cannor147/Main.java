package com.github.cannor147;

import com.github.cannor147.model.GeoMap;
import com.github.cannor147.model.Color;
import com.github.cannor147.request.Request;
import com.github.cannor147.request.ScaleRequestBuilder;
import com.github.cannor147.request.StepRequestBuilder;
import com.github.cannor147.request.StraightRequestBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class Main {
    public static final String COUNTRIES = "countries";

    public static void main(String[] args) throws IOException, URISyntaxException {
        final ResourceReader resourceReader = new ResourceReader();
        final GeoMapper geoMapper = new GeoMapper(resourceReader);
        final GeoMap geoMap = geoMapper.findGeoMap(COUNTRIES);

        createStraight(geoMapper, geoMap);
        createScale(resourceReader, geoMapper, geoMap);
        createStep(resourceReader, geoMapper, geoMap);
    }

    public static void createStraight(GeoMapper geoMapper, GeoMap geoMap) throws IOException {
        final List<String> eu = List.of("Sweden", "Finland", "Denmark", "Ireland", "France", "Germany",
                "Poland", "Czechia", "Slovakia", "Hungary", "Austria", "Romania", "Bulgaria",
                "Latvia", "Lithuania", "Estonia", "Greece", "Slovenia", "Croatia", "Italy",
                "Netherlands", "Belgium", "Luxembourg", "Spain", "Portugal", "Malta", "Cyprus");
        final Request request = new StraightRequestBuilder(geoMap)
                .appendAll(Color.RED, "Russia", "Belarus", "Kazakhstan", "Kyrgyzstan", "Armenia")
                .appendAll(Color.BLUE, eu)
                .build();
        geoMapper.createMapToFile(request, new File("straight.png"));
    }

    public static void createScale(ResourceReader resourceReader, GeoMapper geoMapper, GeoMap geoMap) throws IOException, URISyntaxException {
        final Request request = new ScaleRequestBuilder(geoMap)
                .fromCsv(resourceReader.getResource("example/gdp.csv"), 1, 2)
                .useColor(Color.GREEN)
                .addLogarithmization(10)
                .build();
        geoMapper.createMapToFile(request, new File("scale.png"));
    }

    public static void createStep(ResourceReader resourceReader, GeoMapper geoMapper, GeoMap geoMap) throws IOException, URISyntaxException {
        final Request request = new StepRequestBuilder(geoMap)
                .fromCsv(resourceReader.getResource("example/hdr.csv"), 2, 3)
                .useColor(Color.BLUE, Color.RED)
                .withSeparators(0.8, 0.7, 0.55)
                .build();
        geoMapper.createMapToFile(request, new File("step.png"));
    }
}
