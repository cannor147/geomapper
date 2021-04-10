package com.github.cannor147;

import com.github.cannor147.model.Color;
import com.github.cannor147.model.GeoMap;
import com.github.cannor147.request.Request;
import com.github.cannor147.request.RequestBuilder;
import com.github.cannor147.request.colorization.ColorizationScheme;
import com.github.cannor147.request.colorization.ScaleColorizationScheme;
import com.github.cannor147.request.colorization.StepColorizationScheme;
import com.github.cannor147.request.colorization.StraightColorizationScheme;
import com.github.cannor147.util.CsvUtils;
import one.util.streamex.StreamEx;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GeoMapperCli {
    @SuppressWarnings("AccessStaticViaInstance")
    public static void main(String[] args) throws IOException {
        final GeoMapper geoMapper = new GeoMapper();
        if (args.length == 0) {
            throw new IllegalArgumentException("Can't run geo mapper without geo map name.");
        }
        final GeoMap geoMap = geoMapper.findGeoMap(args[0]);

        final OptionGroup saveOptions = new OptionGroup();
        saveOptions.addOption(OptionBuilder.withArgName("file").hasArg().create("to"));
        saveOptions.addOption(OptionBuilder.withArgName("file").hasArg().create("save"));

        final Options options = new Options();
        options.addOption(OptionBuilder.withArgName("file").hasArg().create("from"));
        options.addOptionGroup(saveOptions);
        options.addOption(OptionBuilder.hasArgs(2).create("list"));
        options.addOption(OptionBuilder.hasArgs(3).create("fromList"));
        options.addOption(OptionBuilder.hasArgs(2).create("values"));
        options.addOption(OptionBuilder.hasArgs(3).create("fromValues"));
        options.addOption(OptionBuilder.withArgName("schemeName").hasArg().create("use"));

        final Options schemeOptions = new Options();
        schemeOptions.addOption(OptionBuilder.withArgName("color").hasArg().create("defaultColor"));
        schemeOptions.addOption(OptionBuilder.withArgName("color").hasArg().create("color"));
        schemeOptions.addOption(OptionBuilder.withArgName("colors").hasArgs(2).create("colors"));
        schemeOptions.addOption(OptionBuilder.withArgName("color").hasArg().create("maxColor"));
        schemeOptions.addOption(OptionBuilder.withArgName("color").hasArg().create("minColor"));
        schemeOptions.addOption(OptionBuilder.withArgName("value").hasArg().create("min"));
        schemeOptions.addOption(OptionBuilder.withArgName("value").hasArg().create("minValue"));
        schemeOptions.addOption(OptionBuilder.withArgName("value").hasArg().create("max"));
        schemeOptions.addOption(OptionBuilder.withArgName("value").hasArg().create("maxValue"));
        schemeOptions.addOption(OptionBuilder.withArgName("value").hasArg().create("logarithmization"));
        schemeOptions.addOption(OptionBuilder.withArgName("value").hasArg().create("separator"));
        schemeOptions.addOption(OptionBuilder.withArgName("values").hasArgs().withValueSeparator(',').create("separators"));
        final Set<String> schemeOptionNames = castIterable(schemeOptions.getOptions(), Option.class).stream()
                .peek(options::addOption)
                .map(Option::getOpt)
                .collect(Collectors.toSet());

        final List<Option> orderedOptions = new ArrayList<>();
        try {
            CommandLineParser parser = new BasicParser() {
                @Override
                public void processArgs(Option opt, ListIterator iter) throws ParseException {
                    super.processArgs(opt, iter);
                    orderedOptions.add(opt);
                }
            };
            parser.parse(options, Arrays.copyOfRange(args, 1, args.length));
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            new HelpFormatter().printHelp("utility-name", options);
            return;
        }

        File from = null;
        File to = new File("map.png");
        ColorizationScheme scheme = null;
        boolean previousSchemeOption = false;
        final RequestBuilder requestBuilder = new RequestBuilder(geoMap);
        for (Option option : orderedOptions) {
            switch (option.getOpt()) {
                case "from" -> from = new File(option.getValue());
                case "to", "save" -> to = new File(option.getValue());
                case "list" -> handleList(option, from, requestBuilder, 0);
                case "fromList" -> {
                    from = new File(option.getValue(0));
                    handleList(option, from, requestBuilder, 1);
                }
                case "values" -> handleValues(option, from, requestBuilder, 0);
                case "fromValues" -> {
                    from = new File(option.getValue(0));
                    handleValues(option, from, requestBuilder, 1);
                }
                case "use" -> {
                    scheme = switch (option.getValue()) {
                        case "straight" -> new StraightColorizationScheme();
                        case "scale" -> new ScaleColorizationScheme();
                        case "step" -> new StepColorizationScheme();
                        default -> throw new IllegalArgumentException("Unknown colorization scheme '" + option.getValue() + "'.");
                    };
                    requestBuilder.changeScheme(scheme);
                }
                case "defaultColor" -> handleDefaultColor(option, previousSchemeOption, scheme);
                case "color" -> handleColor(option, previousSchemeOption, scheme);
                case "colors" -> handleColors(option, previousSchemeOption, scheme);
                case "minColor" -> handleMinColor(option, previousSchemeOption, scheme);
                case "maxColor" -> handleMaxColor(option, previousSchemeOption, scheme);
                case "minValue", "min" -> handleMinValue(option, previousSchemeOption, scheme);
                case "maxValue", "max" -> handleMaxValue(option, previousSchemeOption, scheme);
                case "logarithmization" -> handleLogarithmization(option, previousSchemeOption, scheme);
                case "separator" -> handleSeparator(option, previousSchemeOption, scheme);
                case "separators" -> handleSeparators(option, previousSchemeOption, scheme);
            }
            previousSchemeOption = schemeOptionNames.contains(option.getOpt()) || "use".equals(option.getOpt());
        }

        final Request request = requestBuilder.build();
        geoMapper.createMapToFile(request, to);
    }

    private static void handleList(Option option, File from, RequestBuilder builder, int offset) throws IOException {
        validateFrom("list", from);
        final List<String> names = CsvUtils.readCsv(from, extractInt(option, offset));
        builder.withColor(names, extractColor(option, offset + 1));
    }

    private static void handleValues(Option option, File from, RequestBuilder builder, int offset) throws IOException {
        validateFrom("values", from);
        final int nameColumn = extractInt(option, offset);
        final int valueColumn = extractInt(option, offset + 1);
        final List<Pair<String, String>> csvData = CsvUtils.readCsv(from, nameColumn, valueColumn);
        final Map<String, Number> parsedData = StreamEx.of(csvData)
                .mapToEntry(Pair::getKey, Pair::getValue)
                .mapValues(GeoMapper::safeParseNumber)
                .toMap();
        builder.withValues(parsedData);
    }

    private static void handleDefaultColor(Option option, boolean previousSchemeOption, ColorizationScheme scheme) {
        validateScheme("defaultColor", previousSchemeOption, scheme);
        scheme.registerDefaultColor(extractColor(option, 0));
    }

    private static void handleColor(Option option, boolean previousSchemeOption, ColorizationScheme scheme) {
        validateScheme("color", previousSchemeOption, scheme);
        final Color maxColor = extractColor(option, 0);
        if (scheme instanceof ScaleColorizationScheme) {
            ((ScaleColorizationScheme) scheme).registerColors(maxColor, maxColor.getDefaultOpposite());
        } else if (scheme instanceof StepColorizationScheme) {
            ((StepColorizationScheme) scheme).registerColors(maxColor, maxColor.getDefaultOpposite());
        } else {
            throw new IllegalStateException("Can't add color to current colorization scheme.");
        }
    }

    private static void handleColors(Option option, boolean previousSchemeOption, ColorizationScheme scheme) {
        validateScheme("colors", previousSchemeOption, scheme);
        final Color maxColor = extractColor(option, 0);
        final Color minColor = extractColor(option, 1);
        if (scheme instanceof ScaleColorizationScheme) {
            ((ScaleColorizationScheme) scheme).registerColors(maxColor, minColor);
        } else if (scheme instanceof StepColorizationScheme) {
            ((StepColorizationScheme) scheme).registerColors(maxColor, minColor);
        } else {
            throw new IllegalStateException("Can't add color to current colorization scheme.");
        }
    }

    private static void handleMinColor(Option option, boolean previousSchemeOption, ColorizationScheme scheme) {
        validateScheme("minColor", previousSchemeOption, scheme);
        if (scheme instanceof ScaleColorizationScheme) {
            ((ScaleColorizationScheme) scheme).registerMinColor(extractColor(option, 0));
        } else if (scheme instanceof StepColorizationScheme) {
            ((StepColorizationScheme) scheme).registerMinColor(extractColor(option, 0));
        } else {
            throw new IllegalStateException("Can't add min color to current colorization scheme.");
        }
    }

    private static void handleMaxColor(Option option, boolean previousSchemeOption, ColorizationScheme scheme) {
        validateScheme("maxColor", previousSchemeOption, scheme);
        if (scheme instanceof ScaleColorizationScheme) {
            ((ScaleColorizationScheme) scheme).registerMaxColor(extractColor(option, 0));
        } else if (scheme instanceof StepColorizationScheme) {
            ((StepColorizationScheme) scheme).registerMaxColor(extractColor(option, 0));
        } else {
            throw new IllegalStateException("Can't add max color to current colorization scheme.");
        }
    }

    private static void handleMinValue(Option option, boolean previousSchemeOption, ColorizationScheme scheme) {
        validateScheme("minValue", previousSchemeOption, scheme);
        if (scheme instanceof ScaleColorizationScheme) {
            ((ScaleColorizationScheme) scheme).registerMinValue(extractDouble(option, 0));
        } else {
            throw new IllegalStateException("Can't add min value to current colorization scheme.");
        }
    }

    private static void handleMaxValue(Option option, boolean previousSchemeOption, ColorizationScheme scheme) {
        validateScheme("maxValue", previousSchemeOption, scheme);
        if (scheme instanceof ScaleColorizationScheme) {
            ((ScaleColorizationScheme) scheme).registerMaxValue(extractDouble(option, 0));
        } else {
            throw new IllegalStateException("Can't add max value to current colorization scheme.");
        }
    }

    private static void handleLogarithmization(Option option, boolean previousSchemeOption, ColorizationScheme scheme) {
        validateScheme("logarithmization", previousSchemeOption, scheme);
        if (scheme instanceof ScaleColorizationScheme) {
            ((ScaleColorizationScheme) scheme).registerLogarithmization(extractDouble(option, 0));
        } else {
            throw new IllegalStateException("Can't add logarithmization to current colorization scheme.");
        }
    }

    private static void handleSeparator(Option option, boolean previousSchemeOption, ColorizationScheme scheme) {
        validateScheme("separator", previousSchemeOption, scheme);
        if (scheme instanceof StepColorizationScheme) {
            ((StepColorizationScheme) scheme).registerSeparator(extractDouble(option, 0));
        } else {
            throw new IllegalStateException("Can't add separator to current colorization scheme.");
        }
    }

    private static void handleSeparators(Option option, boolean previousSchemeOption, ColorizationScheme scheme) {
        validateScheme("separators", previousSchemeOption, scheme);
        if (scheme instanceof StepColorizationScheme) {
            IntStream.range(0, option.getValues().length)
                    .mapToObj(i -> extractDouble(option, i))
                    .forEach(((StepColorizationScheme) scheme)::registerSeparator);
        } else {
            throw new IllegalStateException("Can't add separators to current colorization scheme.");
        }
    }

    private static void validateFrom(String state, File from) {
        if (from == null) {
            throw new IllegalArgumentException("Data source is not chosen yet for '" + state + "'.");
        }
    }

    private static void validateScheme(String state, boolean previousSchemeOption, ColorizationScheme scheme) {
        if (!previousSchemeOption || scheme == null) {
            throw new IllegalArgumentException("Unexpected option '" + state + "'.");
        }
    }

    private static Color extractColor(Option option, int index) {
        try {
            return Color.parseColor(option.getValue(index));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Expected color option, but found: '" + option.getValue(index) + "'.");
        }
    }

    private static int extractInt(Option option, int index) {
        try {
            return Integer.parseInt(option.getValue(index));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Expected integer option, but found: '" + option.getValue(index) + "'.");
        }
    }

    private static double extractDouble(Option option, int index) {
        try {
            return Double.parseDouble(option.getValue(index));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Expected decimal option, but found: '" + option.getValue(index) + "'.");
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static <T> List<T> castIterable(Iterable<?> kek, Class<T> clazz) {
        return StreamEx.of(kek.iterator())
                .flatMap(x -> clazz.isInstance(x) ? StreamEx.of(clazz.cast(x)) : StreamEx.empty())
                .toList();
    }
}
