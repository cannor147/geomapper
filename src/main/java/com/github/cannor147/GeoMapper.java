package com.github.cannor147;

import com.github.cannor147.model.Color;
import com.github.cannor147.model.GeoMap;
import com.github.cannor147.model.GeoMapDto;
import com.github.cannor147.model.Territory;
import com.github.cannor147.namer.Namer;
import com.github.cannor147.request.Request;
import com.github.cannor147.request.RequestBuilder;
import com.github.cannor147.request.RequestService;
import com.github.cannor147.request.colorization.ScaleColorizationScheme;
import com.github.cannor147.request.colorization.StepColorizationScheme;
import com.github.cannor147.request.colorization.StraightColorizationScheme;
import com.github.cannor147.util.CsvUtils;
import one.util.streamex.StreamEx;
import org.apache.commons.lang3.tuple.Pair;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class GeoMapper {
    private static final String GEO_MAPS = "geomaps.json";
    public static final String PNG = "png";

    private final RequestService requestService;
    private final ResourceReader resourceReader;
    private final Map<String, GeoMapDto> nameToGeoMapMap;

    public static void main(String[] args) throws IOException {
        final GeoMapper geoMapper = new GeoMapper();
        final GeoMap geoMap = geoMapper.findGeoMap(extractArg(args, 0, true));
        final RequestBuilder requestBuilder = new RequestBuilder(geoMap);

        int index = 1;
        File csvFile = null;
        String fileName = "map.png";
        while (index < args.length) {
            final String command = extractArg(args, index, false);
            switch (command) {
                case "-from" -> {
                    csvFile = new File(extractArg(args, index + 1, true));
                    index += 2;
                }
                case "-fromValues" -> {
                    csvFile = new File(extractArg(args, index + 1, true));
                    final int nameColumn = Integer.parseInt(extractArg(args, index + 2, true));
                    final int valueColumn = Integer.parseInt(extractArg(args, index + 3, true));
                    final List<Pair<String, String>> csvData = CsvUtils.readCsv(csvFile, nameColumn, valueColumn);
                    final Map<String, Number> parsedData = StreamEx.of(csvData.iterator())
                            .mapToEntry(Pair::getKey, Pair::getValue)
                            .mapValues(GeoMapper::safeParseNumber)
                            .toMap();
                    requestBuilder.withValues(parsedData);
                    index += 4;
                }
                case "-fromList" -> {
                    csvFile = new File(extractArg(args, index + 1, true));
                    final int nameColumn = Integer.parseInt(extractArg(args, index + 2, true));
                    final Color color = Color.parseColor(extractArg(args, index + 3, true));
                    requestBuilder.withColor(CsvUtils.readCsv(csvFile, nameColumn), color);
                    index += 4;
                }
                case "-values" -> {
                    Objects.requireNonNull(csvFile);
                    final int nameColumn = Integer.parseInt(extractArg(args, index + 1, true));
                    final int valueColumn = Integer.parseInt(extractArg(args, index + 2, true));
                    final List<Pair<String, String>> csvData = CsvUtils.readCsv(csvFile, nameColumn, valueColumn);
                    final Map<String, Number> parsedData = StreamEx.of(csvData.iterator())
                            .mapToEntry(Pair::getKey, Pair::getValue)
                            .mapValues(GeoMapper::safeParseNumber)
                            .toMap();
                    requestBuilder.withValues(parsedData);
                    index += 3;
                }
                case "-list" -> {
                    Objects.requireNonNull(csvFile);
                    final int nameColumn = Integer.parseInt(extractArg(args, index + 1, true));
                    final Color color = Color.parseColor(extractArg(args, index + 2, true));
                    requestBuilder.withColor(CsvUtils.readCsv(csvFile, nameColumn), color);
                    index += 3;
                }
                case "-use" -> {
                    final String schemeName = extractArg(args, index + 1, true);
                    index += 2;

                    switch (schemeName) {
                        case "straight" -> {
                            final StraightColorizationScheme colorizationScheme = new StraightColorizationScheme();
                            boolean end = false;
                            while (!end && index < args.length) {
                                final String schemeCommand = extractArg(args, index, false);
                                if ("-defaultColor".equals(schemeCommand)) {
                                    final Color defaultColor = Color.parseColor(extractArg(args, index + 1, true));
                                    colorizationScheme.registerDefaultColor(defaultColor);
                                    index += 2;
                                } else {
                                    end = true;
                                }
                            }
                            requestBuilder.changeScheme(colorizationScheme);
                        }
                        case "scale" -> {
                            final ScaleColorizationScheme colorizationScheme = new ScaleColorizationScheme();
                            boolean end = false;
                            while (!end && index < args.length) {
                                final String schemeCommand = extractArg(args, index, false);
                                switch (schemeCommand) {
                                    case "-defaultColor" -> {
                                        final Color defaultColor = Color.parseColor(extractArg(args, index + 1, true));
                                        colorizationScheme.registerDefaultColor(defaultColor);
                                        index += 2;
                                    }
                                    case "-color" -> {
                                        final Color color = Color.parseColor(extractArg(args, index + 1, true));
                                        colorizationScheme.registerColors(color, color.getDefaultOpposite());
                                        index += 2;
                                    }
                                    case "-colors" -> {
                                        final Color minColor = Color.parseColor(extractArg(args, index + 1, true));
                                        final Color maxColor = Color.parseColor(extractArg(args, index + 2, true));
                                        colorizationScheme.registerColors(minColor, maxColor);
                                        index += 3;
                                    }
                                    case "-min" -> {
                                        final double min = Double.parseDouble(extractArg(args, index + 1, true));
                                        colorizationScheme.registerMinValue(min);
                                        index += 2;
                                    }
                                    case "-max" -> {
                                        final double max = Double.parseDouble(extractArg(args, index + 1, true));
                                        colorizationScheme.registerMaxValue(max);
                                        index += 2;
                                    }
                                    case "-logarithmization" -> {
                                        final double base = Double.parseDouble(extractArg(args, index + 1, true));
                                        colorizationScheme.registerLogarithmization(base);
                                        index += 2;
                                    }
                                    default -> end = true;
                                }
                            }
                            requestBuilder.changeScheme(colorizationScheme);
                        }
                        case "step" -> {
                            final StepColorizationScheme colorizationScheme = new StepColorizationScheme();
                            boolean end = false;
                            while (!end && index < args.length) {
                                final String schemeCommand = extractArg(args, index, false);
                                switch (schemeCommand) {
                                    case "-defaultColor" -> {
                                        final Color defaultColor = Color.parseColor(extractArg(args, index + 1, true));
                                        colorizationScheme.registerDefaultColor(defaultColor);
                                        index += 2;
                                    }
                                    case "-color" -> {
                                        final Color color = Color.parseColor(extractArg(args, index + 1, true));
                                        colorizationScheme.registerColors(color, color.getDefaultOpposite());
                                        index += 2;
                                    }
                                    case "-colors" -> {
                                        final Color maxColor = Color.parseColor(extractArg(args, index + 1, true));
                                        final Color minColor = Color.parseColor(extractArg(args, index + 2, true));
                                        colorizationScheme.registerColors(maxColor, minColor);
                                        index += 3;
                                    }
                                    case "-separator" -> {
                                        colorizationScheme.registerSeparator(Double.parseDouble(extractArg(args, index + 1, true)));
                                        index += 2;
                                    }
                                    case "-separators" -> {
                                        final int count = Integer.parseInt(extractArg(args, index + 1, true));
                                        IntStream.range(index + 2, index + 2 + count)
                                                .mapToObj(i -> extractArg(args, i, true))
                                                .map(Double::parseDouble)
                                                .collect(Collectors.toList())
                                                .forEach(colorizationScheme::registerSeparator);
                                        index += count + 2;
                                    }
                                    default -> end = true;
                                }
                            }
                            requestBuilder.changeScheme(colorizationScheme);
                        }
                        default -> throw new IllegalArgumentException("Unknown colorization scheme '" + schemeName + "'.");
                    }
                }
                case "-save" -> {
                    fileName = extractArg(args, index + 1, true);
                    index += 2;
                }
                default -> throw new IllegalArgumentException("Unknown command '" + command + "'.");
            }
        }
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
