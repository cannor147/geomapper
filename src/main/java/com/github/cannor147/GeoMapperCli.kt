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

private val OPTIONS: Options = run {
    val it = Options()
    it.addOption(OptionBuilder.withArgName("file").hasArg().create("from"))
    it.addOptionGroup(
        OptionGroup()
            .addOption(OptionBuilder.withArgName("file").hasArg().create("to"))
            .addOption(OptionBuilder.withArgName("file").hasArg().create("save"))
    )
    it.addOption(OptionBuilder.hasArgs(2).create("list"))
    it.addOption(OptionBuilder.hasArgs(3).create("fromList"))
    it.addOption(OptionBuilder.hasArgs(2).create("values"))
    it.addOption(OptionBuilder.hasArgs(3).create("fromValues"))
    it.addOptionGroup(
        OptionGroup()
            .addOption(OptionBuilder.create("includeAllUnrecognized"))
            .addOption(OptionBuilder.create("includeUnmentionedUnrecognized"))
            .addOption(OptionBuilder.create("excludeAllUnrecognized"))
    )
    it.addOption(OptionBuilder.withArgName("schemeName").hasArg().create("use"))
    it
}

private val SCHEME_OPTION_NAMES: Set<String> = run {
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
    schemeOptions.addOption(
        OptionBuilder.withArgName("values").hasArgs().withValueSeparator(',').create("separators")
    )
    castIterable(schemeOptions.options, Option::class.java).asSequence()
        .onEach { OPTIONS.addOption(it) }
        .map(Option::getOpt)
        .toSet()
}

fun main(args: Array<String>) {
    if (args.isEmpty()) throw IllegalArgumentException("Can't run geo mapper without geo map name.")
    val geoMapper = GeoMapper()
    val geoMap = geoMapper.findGeoMap(args[0])

    val orderedOptions: MutableList<Option> = ArrayList()
    try {
        val parser: CommandLineParser = object : BasicParser() {
            @Throws(ParseException::class)
            override fun processArgs(opt: Option?, iter: MutableListIterator<Any?>?) {
                super.processArgs(opt, iter)
                opt?.let(orderedOptions::add)
            }
        }
        val commandLine: CommandLine = parser.parse(OPTIONS, args.copyOfRange(1, args.size))
        commandLine.options
            .filterNot(orderedOptions::contains)
            .forEach(orderedOptions::add)

    } catch (e: ParseException) {
        println(e.message)
        HelpFormatter().printHelp("utility-name", OPTIONS)
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
            "list" -> handleList(option, from, requestBuilder, 0)
            "fromList" -> {
                from = File(option.getValue(0))
                handleList(option, from, requestBuilder, 1)
            }
            "values" -> handleValues(option, from, requestBuilder, 0)
            "fromValues" -> {
                from = File(option.getValue(0))
                handleValues(option, from, requestBuilder, 1)
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
            "defaultColor" -> handleDefaultColor(option, previousSchemeOption, scheme)
            "color" -> handleColor(option, previousSchemeOption, scheme)
            "colors" -> handleColors(option, previousSchemeOption, scheme)
            "minColor" -> handleMinColor(option, previousSchemeOption, scheme)
            "maxColor" -> handleMaxColor(option, previousSchemeOption, scheme)
            "minValue", "min" -> handleMinValue(option, previousSchemeOption, scheme)
            "maxValue", "max" -> handleMaxValue(option, previousSchemeOption, scheme)
            "logarithmization" -> handleLogarithmization(option, previousSchemeOption, scheme)
            "separator" -> handleSeparator(option, previousSchemeOption, scheme)
            "separators" -> handleSeparators(option, previousSchemeOption, scheme)
            "includeAllUnrecognized" -> requestBuilder.withState(UnofficialStateBehavior.INCLUDE_ALL)
            "includeUnmentionedUnrecognized" -> requestBuilder.withState(UnofficialStateBehavior.INCLUDE_UNMENTIONED)
            "excludeAllUnrecognized" -> requestBuilder.withState(UnofficialStateBehavior.EXCLUDE_ALL)
        }
        previousSchemeOption = SCHEME_OPTION_NAMES.contains(option.opt) || "use" == option.opt
    }

    val request: Request = requestBuilder.build()
    geoMapper.createMapToFile(request, to)
}

@Throws(IOException::class)
private fun handleList(option: Option, from: File?, builder: RequestBuilder, offset: Int) {
    validateFrom("list", from)
    val names = readCsv(from!!, extractInt(option, offset))
    builder.withColor(names, extractColor(option, offset + 1))
}

@Throws(IOException::class)
private fun handleValues(option: Option, from: File?, builder: RequestBuilder, offset: Int) {
    validateFrom("values", from)
    readCsv(from!!, extractInt(option, offset), extractInt(option, offset + 1)).asSequence()
        .map { it.first to it.second }
        .map { (a, b) -> a to (b?.let(GeoMapper::safeParseNumber)) }
        .mapNotNull { (a, b) -> if (a == null || b == null) null else a to b }
        .toMap()
        .let(builder::withValues)
}

private fun handleDefaultColor(option: Option, previousSchemeOption: Boolean, scheme: ColorizationScheme) {
    validateScheme("defaultColor", previousSchemeOption, scheme)
    scheme.registerDefaultColor(extractColor(option, 0))
}

private fun handleColor(option: Option, previousSchemeOption: Boolean, scheme: ColorizationScheme) {
    validateScheme("color", previousSchemeOption, scheme)
    val maxColor = extractColor(option, 0)
    when (scheme) {
        is ScaleColorizationScheme -> scheme.registerColors(maxColor, maxColor.defaultOpposite!!)
        is StepColorizationScheme -> scheme.registerColors(maxColor, maxColor.defaultOpposite!!)
        else -> throw IllegalStateException("Can't add color to current colorization scheme.")
    }
}

private fun handleColors(option: Option, previousSchemeOption: Boolean, scheme: ColorizationScheme) {
    validateScheme("colors", previousSchemeOption, scheme)
    val maxColor = extractColor(option, 0)
    val minColor = extractColor(option, 1)
    when (scheme) {
        is ScaleColorizationScheme -> scheme.registerColors(maxColor, minColor)
        is StepColorizationScheme -> scheme.registerColors(maxColor, minColor)
        else -> throw IllegalStateException("Can't add color to current colorization scheme.")
    }
}

private fun handleMinColor(option: Option, previousSchemeOption: Boolean, scheme: ColorizationScheme) {
    validateScheme("minColor", previousSchemeOption, scheme)
    when (scheme) {
        is ScaleColorizationScheme -> scheme.registerMinColor(extractColor(option, 0))
        is StepColorizationScheme -> scheme.registerMinColor(extractColor(option, 0))
        else -> throw IllegalStateException("Can't add min color to current colorization scheme.")
    }
}

private fun handleMaxColor(option: Option, previousSchemeOption: Boolean, scheme: ColorizationScheme) {
    validateScheme("maxColor", previousSchemeOption, scheme)
    when (scheme) {
        is ScaleColorizationScheme -> scheme.registerMaxColor(extractColor(option, 0))
        is StepColorizationScheme -> scheme.registerMaxColor(extractColor(option, 0))
        else -> throw IllegalStateException("Can't add max color to current colorization scheme.")
    }
}

private fun handleMinValue(option: Option, previousSchemeOption: Boolean, scheme: ColorizationScheme) {
    validateScheme("minValue", previousSchemeOption, scheme)
    when (scheme) {
        is ScaleColorizationScheme -> scheme.registerMinValue(extractDouble(option, 0))
        else -> throw IllegalStateException("Can't add min value to current colorization scheme.")
    }
}

private fun handleMaxValue(option: Option, previousSchemeOption: Boolean, scheme: ColorizationScheme) {
    validateScheme("maxValue", previousSchemeOption, scheme)
    when (scheme) {
        is ScaleColorizationScheme -> scheme.registerMaxValue(extractDouble(option, 0))
        else -> throw IllegalStateException("Can't add max value to current colorization scheme.")
    }
}

private fun handleLogarithmization(option: Option, previousSchemeOption: Boolean, scheme: ColorizationScheme) {
    validateScheme("logarithmization", previousSchemeOption, scheme)
    when (scheme) {
        is ScaleColorizationScheme -> scheme.registerLogarithmization(extractDouble(option, 0))
        else -> throw IllegalStateException("Can't add logarithmization to current colorization scheme.")
    }
}

private fun handleSeparator(option: Option, previousSchemeOption: Boolean, scheme: ColorizationScheme) {
    validateScheme("separator", previousSchemeOption, scheme)
    when (scheme) {
        is StepColorizationScheme -> scheme.registerSeparator(extractDouble(option, 0))
        else -> throw IllegalStateException("Can't add separator to current colorization scheme.")
    }
}

private fun handleSeparators(option: Option, previousSchemeOption: Boolean, scheme: ColorizationScheme) {
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

private fun OptionBuilder.withValueSeparator(c: Char): OptionBuilder = OptionBuilder.withValueSeparator(c)
private fun OptionBuilder.hasArg(): OptionBuilder = OptionBuilder.hasArg()
private fun OptionBuilder.hasArgs(i: Int): OptionBuilder = OptionBuilder.hasArgs(i)
private fun OptionBuilder.hasArgs(): OptionBuilder = OptionBuilder.hasArgs()
private fun OptionBuilder.create(s: String): Option? = OptionBuilder.create(s)
