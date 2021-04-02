package com.github.cannor147;

import com.github.cannor147.model.Color;
import com.github.cannor147.model.GeoMap;
import com.github.cannor147.model.GeoMapDto;
import com.github.cannor147.model.Territory;
import com.github.cannor147.namer.Namer;
import com.github.cannor147.request.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.collectingAndThen;

public class GeoMapper {
    private static final String GEO_MAPS = "geomaps.json";
    public static final String PNG = "png";

    private final RequestService requestService;
    private final ResourceReader resourceReader;
    private final Map<String, GeoMapDto> nameToGeoMapMap;

    public static void main(String[] args) throws IOException {
        final GeoMapper geoMapper = new GeoMapper();
        final String mode = extractArg(args, 0, true);
        final GeoMap geoMap = geoMapper.findGeoMap(extractArg(args, 1, true));
        String fileName = "map.png";
        final RequestBuilder requestBuilder = switch (mode) {
            case "straight" -> {
                StraightRequestBuilder straightRequestBuilder = new StraightRequestBuilder(geoMap);
                int index = 2;
                while (index < args.length) {
                    final String command = extractArg(args, index, false);
                    switch (command) {
                        case "-import" -> {
                            final String pathname = extractArg(args, index + 1, true);
                            final int nameColumn = Integer.parseInt(extractArg(args, index + 2, true));
                            straightRequestBuilder = straightRequestBuilder.fromCsv(new File(pathname), nameColumn);
                            index += 3;
                        }
                        case "-color" -> {
                            final Color color = Color.parseColor(extractArg(args, index + 1, true));
                            straightRequestBuilder = straightRequestBuilder.useColor(color);
                            index += 2;
                        }
                        case "-defaultColor" -> {
                            final Color defaultColor = Color.parseColor(extractArg(args, index + 1, true));
                            straightRequestBuilder = straightRequestBuilder.useDefaultColor(defaultColor);
                            index += 2;
                        }
                        case "-save" -> {
                            fileName = extractArg(args, index + 1, true);
                            index += 2;
                        }
                        default -> throw new IllegalArgumentException("Unknown command + '" + command + "'.");
                    }
                }
                yield straightRequestBuilder;
            }
            case "scale" -> {
                ScaleRequestBuilder scaleRequestBuilder = new ScaleRequestBuilder(geoMap);
                int index = 2;
                while (index < args.length) {
                    final String command = extractArg(args, index, false);
                    switch (command) {
                        case "-import" -> {
                            final String pathname = extractArg(args, index + 1, true);
                            final int nameColumn = Integer.parseInt(extractArg(args, index + 2, true));
                            final int valueColumn = Integer.parseInt(extractArg(args, index + 3, true));
                            scaleRequestBuilder = scaleRequestBuilder.fromCsv(new File(pathname), nameColumn, valueColumn);
                            index += 4;
                        }
                        case "-color" -> {
                            final Color color = Color.parseColor(extractArg(args, index + 1, true));
                            scaleRequestBuilder = scaleRequestBuilder.useColor(color);
                            index += 2;
                        }
                        case "-colors" -> {
                            final Color minColor = Color.parseColor(extractArg(args, index + 1, true));
                            final Color maxColor = Color.parseColor(extractArg(args, index + 2, true));
                            scaleRequestBuilder = scaleRequestBuilder.useColor(minColor, maxColor);
                            index += 3;
                        }
                        case "-defaultColor" -> {
                            final Color defaultColor = Color.parseColor(extractArg(args, index + 1, true));
                            scaleRequestBuilder = scaleRequestBuilder.useDefaultColor(defaultColor);
                            index += 2;
                        }
                        case "-min" -> {
                            final double min = Double.parseDouble(extractArg(args, index + 1, true));
                            scaleRequestBuilder = scaleRequestBuilder.customizeMinimum(min);
                            index += 2;
                        }
                        case "-max" -> {
                            final double max = Double.parseDouble(extractArg(args, index + 1, true));
                            scaleRequestBuilder = scaleRequestBuilder.customizeMaximum(max);
                            index += 2;
                        }
                        case "-logarithmization" -> {
                            final double base = Double.parseDouble(extractArg(args, index + 1, true));
                            scaleRequestBuilder = scaleRequestBuilder.addLogarithmization(base);
                            index += 2;
                        }
                        case "-save" -> {
                            fileName = extractArg(args, index + 1, true);
                            index += 2;
                        }
                        default -> throw new IllegalArgumentException("Unknown command + '" + command + "'.");
                    }
                }
                yield scaleRequestBuilder;
            }
            case "step" -> {
                StepRequestBuilder stepRequestBuilder = new StepRequestBuilder(geoMap);
                int index = 2;
                while (index < args.length) {
                    final String command = extractArg(args, index, false);
                    switch (command) {
                        case "-import" -> {
                            final String pathname = extractArg(args, index + 1, true);
                            final int nameColumn = Integer.parseInt(extractArg(args, index + 2, true));
                            final int valueColumn = Integer.parseInt(extractArg(args, index + 3, true));
                            stepRequestBuilder = stepRequestBuilder.fromCsv(new File(pathname), nameColumn, valueColumn);
                            index += 4;
                        }
                        case "-color" -> {
                            final Color color = Color.parseColor(extractArg(args, index + 1, true));
                            stepRequestBuilder = stepRequestBuilder.useColor(color);
                            index += 2;
                        }
                        case "-colors" -> {
                            final Color minColor = Color.parseColor(extractArg(args, index + 1, true));
                            final Color maxColor = Color.parseColor(extractArg(args, index + 2, true));
                            stepRequestBuilder = stepRequestBuilder.useColor(minColor, maxColor);
                            index += 3;
                        }
                        case "-defaultColor" -> {
                            final Color defaultColor = Color.parseColor(extractArg(args, index + 1, true));
                            stepRequestBuilder = stepRequestBuilder.useDefaultColor(defaultColor);
                            index += 2;
                        }
                        case "-separator" -> {
                            stepRequestBuilder.withSeparator(Double.parseDouble(extractArg(args, index + 1, true)));
                            index += 2;
                        }
                        case "-separators" -> {
                            stepRequestBuilder = Arrays.asList(args).subList(index + 1, args.length - 1).stream()
                                    .map(Double::parseDouble)
                                    .collect(collectingAndThen(Collectors.toList(), stepRequestBuilder::withSeparators));
                            index = args.length;
                        }
                        case "-save" -> {
                            fileName = extractArg(args, index + 1, true);
                            index += 2;
                        }
                        default -> throw new IllegalArgumentException("Unknown command + '" + command + "'.");
                    }
                }
                yield stepRequestBuilder;
            }
            default -> throw new IllegalArgumentException("Unknown mode '" + mode + "'");
        };
        geoMapper.createMapToFile(requestBuilder.build(), new File(fileName));
    }

    private static String extractArg(String[] args, int index, boolean canThrow) {
        final String result = args.length > index ? args[index] : null;
        if (canThrow && result == null) {
            throw new IllegalArgumentException("Required argument with index " + index + ".");
        }
        return result;
    }

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
        if (!nameToGeoMapMap.containsKey(geoMapName.toLowerCase())) {
            throw new IllegalArgumentException("No such geo map.");
        }

        final GeoMapDto dto = nameToGeoMapMap.get(geoMapName.toLowerCase());
        final Territory[] territories = resourceReader.readJson(dto.getDataFilePath(), Territory[].class);
        final Map<String, Territory> nameToTerritoryMap = Namer.createMap(territories);
        final BufferedImage map = resourceReader.readImage(dto.getMapFilePath());
        return new GeoMap(dto.getName(), nameToTerritoryMap, map);
    }
}
