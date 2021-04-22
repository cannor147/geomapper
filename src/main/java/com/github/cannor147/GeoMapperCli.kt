@file:Suppress("unused")

package com.github.cannor147

import com.github.cannor147.model.Color
import com.github.cannor147.model.Color.Companion.parseColor
import com.github.cannor147.request.Request
import com.github.cannor147.request.RequestBuilder
import com.github.cannor147.request.UnofficialStateBehavior
import com.github.cannor147.request.colorization.ColorizationScheme
import com.github.cannor147.request.colorization.ScaleColorizationScheme
import com.github.cannor147.request.colorization.StepColorizationScheme
import com.github.cannor147.request.colorization.StraightColorizationScheme
import com.github.cannor147.util.readCsv
import org.apache.commons.cli.*
import java.io.File
import java.io.IOException

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        throw IllegalArgumentException("Can't run geo mapper without geo map name.")
    }
    val geoMapper = GeoMapper()
    val geoMap = geoMapper.findGeoMap(args[0])

    val orderedOptions: MutableList<Option> = ArrayList()
    try {
        class FixedCommandLineParser : BasicParser() {
            @Throws(ParseException::class)
            override fun processArgs(opt: Option?, iter: MutableListIterator<Any?>?) {
                super.processArgs(opt, iter)
                opt?.let(orderedOptions::add)
            }
        }
        val parser: CommandLineParser = FixedCommandLineParser()
        val commandLine: CommandLine = parser.parse(GeoMapperCli.OPTIONS, args.copyOfRange(1, args.size))
        commandLine.options
                .filterNot(orderedOptions::contains)
                .forEach(orderedOptions::add)

    } catch (e: ParseException) {
        println(e.message)
        HelpFormatter().printHelp("utility-name", GeoMapperCli.OPTIONS)
        return
    }

    var from: File? = null
    var to = File("map.png")
    var scheme: ColorizationScheme = StraightColorizationScheme()
    var previousSchemeOption = false
    val requestBuilder = RequestBuilder(geoMap)
    for (option in orderedOptions) {
        when (option.opt) {
            "from" -> from = File(option.value)
            "to", "save" -> to = File(option.value)
            "list" -> GeoMapperCli.handleList(option, from, requestBuilder, 0)
            "fromList" -> {
                from = File(option.getValue(0))
                GeoMapperCli.handleList(option, from, requestBuilder, 1)
            }
            "values" -> GeoMapperCli.handleValues(option, from, requestBuilder, 0)
            "fromValues" -> {
                from = File(option.getValue(0))
                GeoMapperCli.handleValues(option, from, requestBuilder, 1)
            }
            "use" -> {
                scheme = when (option.value) {
                    "straight" -> StraightColorizationScheme()
                    "scale" -> ScaleColorizationScheme()
                    "step" -> StepColorizationScheme()
                    else -> throw IllegalArgumentException("Unknown colorization scheme '" + option.value + "'.")
                }
                requestBuilder.changeScheme(scheme)
            }
            "defaultColor" -> GeoMapperCli.handleDefaultColor(option, previousSchemeOption, scheme)
            "color" -> GeoMapperCli.handleColor(option, previousSchemeOption, scheme)
            "colors" -> GeoMapperCli.handleColors(option, previousSchemeOption, scheme)
            "minColor" -> GeoMapperCli.handleMinColor(option, previousSchemeOption, scheme)
            "maxColor" -> GeoMapperCli.handleMaxColor(option, previousSchemeOption, scheme)
            "minValue", "min" -> GeoMapperCli.handleMinValue(option, previousSchemeOption, scheme)
            "maxValue", "max" -> GeoMapperCli.handleMaxValue(option, previousSchemeOption, scheme)
            "logarithmization" -> GeoMapperCli.handleLogarithmization(option, previousSchemeOption, scheme)
            "separator" -> GeoMapperCli.handleSeparator(option, previousSchemeOption, scheme)
            "separators" -> GeoMapperCli.handleSeparators(option, previousSchemeOption, scheme)
            "includeAllUnrecognized" -> requestBuilder.withState(UnofficialStateBehavior.INCLUDE_ALL)
            "includeUnmentionedUnrecognized" -> requestBuilder.withState(UnofficialStateBehavior.INCLUDE_UNMENTIONED)
            "excludeAllUnrecognized" -> requestBuilder.withState(UnofficialStateBehavior.EXCLUDE_ALL)
        }
        previousSchemeOption = GeoMapperCli.SCHEME_OPTION_NAMES.contains(option.opt) || "use" == option.opt
    }

    val request: Request = requestBuilder.build()
    geoMapper.createMapToFile(request, to)
}

object GeoMapperCli {
    internal val OPTIONS: Options = Options()
    internal val SCHEME_OPTION_NAMES: Set<String>

    init {
        OPTIONS.addOption(OptionBuilder.withArgName("file").hasArg().create("from"))
        OPTIONS.addOptionGroup(OptionGroup()
                .addOption(OptionBuilder.withArgName("file").hasArg().create("to"))
                .addOption(OptionBuilder.withArgName("file").hasArg().create("save")))
        OPTIONS.addOption(OptionBuilder.hasArgs(2).create("list"))
        OPTIONS.addOption(OptionBuilder.hasArgs(3).create("fromList"))
        OPTIONS.addOption(OptionBuilder.hasArgs(2).create("values"))
        OPTIONS.addOption(OptionBuilder.hasArgs(3).create("fromValues"))
        OPTIONS.addOptionGroup(OptionGroup()
                .addOption(OptionBuilder.create("includeAllUnrecognized"))
                .addOption(OptionBuilder.create("includeUnmentionedUnrecognized"))
                .addOption(OptionBuilder.create("excludeAllUnrecognized")))
        OPTIONS.addOption(OptionBuilder.withArgName("schemeName").hasArg().create("use"))

        val schemeOptions = Options()
        schemeOptions.addOption(OptionBuilder.withArgName("color").hasArg().create("defaultColor"))
        schemeOptions.addOption(OptionBuilder.withArgName("color").hasArg().create("color"))
        schemeOptions.addOption(OptionBuilder.withArgName("colors").hasArgs(2).create("colors"))
        schemeOptions.addOption(OptionBuilder.withArgName("color").hasArg().create("maxColor"))
        schemeOptions.addOption(OptionBuilder.withArgName("color").hasArg().create("minColor"))
        schemeOptions.addOption(OptionBuilder.withArgName("value").hasArg().create("min"))
        schemeOptions.addOption(OptionBuilder.withArgName("value").hasArg().create("minValue"))
        schemeOptions.addOption(OptionBuilder.withArgName("value").hasArg().create("max"))
        schemeOptions.addOption(OptionBuilder.withArgName("value").hasArg().create("maxValue"))
        schemeOptions.addOption(OptionBuilder.withArgName("value").hasArg().create("logarithmization"))
        schemeOptions.addOption(OptionBuilder.withArgName("value").hasArg().create("separator"))
        schemeOptions.addOption(OptionBuilder.withArgName("values").hasArgs().withValueSeparator(',').create("separators"))
        SCHEME_OPTION_NAMES = castIterable(schemeOptions.options, Option::class.java).asSequence()
            .onEach { OPTIONS.addOption(it) }
            .map(Option::getOpt)
            .toSet()
    }

    @Throws(IOException::class)
    internal fun handleList(option: Option, from: File?, builder: RequestBuilder, offset: Int) {
        validateFrom("list", from)
        val names = readCsv(from!!, extractInt(option, offset))
        builder.withColor(names, extractColor(option, offset + 1))
    }

    @Throws(IOException::class)
    internal fun handleValues(option: Option, from: File?, builder: RequestBuilder, offset: Int) {
        validateFrom("values", from)
        readCsv(from!!, extractInt(option, offset), extractInt(option, offset + 1)).asSequence()
            .map { it.first to it.second }
            .map { (a, b) -> a to (b?.let(GeoMapper::safeParseNumber)) }
            .mapNotNull { (a, b) -> if (a == null || b == null) null else a to b }
            .toMap()
            .let(builder::withValues)
    }

    internal fun handleDefaultColor(option: Option, previousSchemeOption: Boolean, scheme: ColorizationScheme) {
        validateScheme("defaultColor", previousSchemeOption, scheme)
        scheme.registerDefaultColor(extractColor(option, 0))
    }

    internal fun handleColor(option: Option, previousSchemeOption: Boolean, scheme: ColorizationScheme) {
        validateScheme("color", previousSchemeOption, scheme)
        val maxColor = extractColor(option, 0)
        when (scheme) {
            is ScaleColorizationScheme -> scheme.registerColors(maxColor, maxColor.defaultOpposite!!)
            is StepColorizationScheme -> scheme.registerColors(maxColor, maxColor.defaultOpposite!!)
            else -> throw IllegalStateException("Can't add color to current colorization scheme.")
        }
    }

    internal fun handleColors(option: Option, previousSchemeOption: Boolean, scheme: ColorizationScheme) {
        validateScheme("colors", previousSchemeOption, scheme)
        val maxColor = extractColor(option, 0)
        val minColor = extractColor(option, 1)
        when (scheme) {
            is ScaleColorizationScheme -> scheme.registerColors(maxColor, minColor)
            is StepColorizationScheme -> scheme.registerColors(maxColor, minColor)
            else -> throw IllegalStateException("Can't add color to current colorization scheme.")
        }
    }

    internal fun handleMinColor(option: Option, previousSchemeOption: Boolean, scheme: ColorizationScheme) {
        validateScheme("minColor", previousSchemeOption, scheme)
        when (scheme) {
            is ScaleColorizationScheme -> scheme.registerMinColor(extractColor(option, 0))
            is StepColorizationScheme -> scheme.registerMinColor(extractColor(option, 0))
            else -> throw IllegalStateException("Can't add min color to current colorization scheme.")
        }
    }

    internal fun handleMaxColor(option: Option, previousSchemeOption: Boolean, scheme: ColorizationScheme) {
        validateScheme("maxColor", previousSchemeOption, scheme)
        when (scheme) {
            is ScaleColorizationScheme -> scheme.registerMaxColor(extractColor(option, 0))
            is StepColorizationScheme -> scheme.registerMaxColor(extractColor(option, 0))
            else -> throw IllegalStateException("Can't add max color to current colorization scheme.")
        }
    }

    internal fun handleMinValue(option: Option, previousSchemeOption: Boolean, scheme: ColorizationScheme) {
        validateScheme("minValue", previousSchemeOption, scheme)
        when (scheme) {
            is ScaleColorizationScheme -> scheme.registerMinValue(extractDouble(option, 0))
            else -> throw IllegalStateException("Can't add min value to current colorization scheme.")
        }
    }

    internal fun handleMaxValue(option: Option, previousSchemeOption: Boolean, scheme: ColorizationScheme) {
        validateScheme("maxValue", previousSchemeOption, scheme)
        when (scheme) {
            is ScaleColorizationScheme -> scheme.registerMaxValue(extractDouble(option, 0))
            else -> throw IllegalStateException("Can't add max value to current colorization scheme.")
        }
    }

    internal fun handleLogarithmization(option: Option, previousSchemeOption: Boolean, scheme: ColorizationScheme) {
        validateScheme("logarithmization", previousSchemeOption, scheme)
        when (scheme) {
            is ScaleColorizationScheme -> scheme.registerLogarithmization(extractDouble(option, 0))
            else -> throw IllegalStateException("Can't add logarithmization to current colorization scheme.")
        }
    }

    internal fun handleSeparator(option: Option, previousSchemeOption: Boolean, scheme: ColorizationScheme) {
        validateScheme("separator", previousSchemeOption, scheme)
        when (scheme) {
            is StepColorizationScheme -> scheme.registerSeparator(extractDouble(option, 0))
            else -> throw IllegalStateException("Can't add separator to current colorization scheme.")
        }
    }

    internal fun handleSeparators(option: Option, previousSchemeOption: Boolean, scheme: ColorizationScheme) {
        validateScheme("separators", previousSchemeOption, scheme)
        when (scheme) {
            is StepColorizationScheme -> (0 until option.values.size).asSequence()
                .map { extractDouble(option, it) }
                .forEach { scheme.registerSeparator(it) }
            else -> throw IllegalStateException("Can't add separators to current colorization scheme.")
        }
    }

    private fun validateFrom(state: String, from: File?) {
        requireNotNull(from) { "Data source is not chosen yet for '$state'." }
    }

    private fun validateScheme(state: String, previousSchemeOption: Boolean, scheme: ColorizationScheme?) {
        require(!(!previousSchemeOption || scheme == null)) { "Unexpected option '$state'." }
    }

    private fun extractColor(option: Option, index: Int): Color = try {
        parseColor(option.getValue(index))
    } catch (e: IllegalArgumentException) {
        throw IllegalArgumentException("Expected color option, but found: '" + option.getValue(index) + "'.")
    }

    private fun extractInt(option: Option, index: Int): Int = try {
        option.getValue(index).toInt()
    } catch (e: NumberFormatException) {
        throw IllegalArgumentException("Expected integer option, but found: '" + option.getValue(index) + "'.")
    }

    private fun extractDouble(option: Option, index: Int): Double = try {
        option.getValue(index).toDouble()
    } catch (e: NumberFormatException) {
        throw IllegalArgumentException("Expected decimal option, but found: '" + option.getValue(index) + "'.")
    }

    private fun <T> castIterable(kek: Iterable<*>, clazz: Class<T>): List<T> = kek.asSequence()
        .flatMap { x: Any? -> if (clazz.isInstance(x)) sequenceOf(clazz.cast(x)) else emptySequence() }
        .toList()
}

private fun OptionBuilder.withValueSeparator(c: Char): OptionBuilder {
    return OptionBuilder.withValueSeparator(c)
}

private fun OptionBuilder.hasArg(): OptionBuilder {
    return OptionBuilder.hasArg()
}

private fun OptionBuilder.hasArgs(i: Int): OptionBuilder {
    return OptionBuilder.hasArgs(i)
}

private fun OptionBuilder.hasArgs(): OptionBuilder {
    return OptionBuilder.hasArgs()
}

private fun OptionBuilder.create(s: String): Option? {
    return OptionBuilder.create(s)
}
