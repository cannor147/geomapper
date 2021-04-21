@file:Suppress("unused")

package com.github.cannor147.request.colorization

import com.github.cannor147.model.Color
import com.github.cannor147.model.Territory
import com.github.cannor147.painter.RGBColor
import com.github.cannor147.painter.generateScheme
import java.util.*
import java.util.function.Function
import java.util.function.ToDoubleFunction
import java.util.stream.Collectors
import kotlin.math.log
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong
import kotlin.streams.asSequence
import kotlin.streams.asStream

class ScaleColorizationScheme : ColorizationScheme() {
    private var maxColor: Color = Color.GREEN
    private var minColor: Color = Color.RED
    private var customMaxValue: Double? = null
    private var customMinValue: Double? = null
    private var transformer: Function<Double, Double> = Function { it }
    private var colorScheme: List<RGBColor> = emptyList()
    private var maxValue: Double = 0.0
    private var minValue: Double = 0.0

    fun registerMaxColor(maxColor: Color) {
        this.maxColor = maxColor
    }

    fun registerMinColor(minColor: Color) {
        this.minColor = minColor
    }

    fun registerColors(maxColor: Color, minColor: Color) {
        registerMaxColor(maxColor)
        registerMinColor(minColor)
    }

    fun <N : Number> registerMaxValue(maxValue: N) {
        customMaxValue = maxValue.toDouble()
    }

    fun <N : Number> registerMinValue(minValue: N) {
        customMinValue = minValue.toDouble()
    }

    fun <N1 : Number, N2 : Number> registerBounds(maxValue: N1, minValue: N2) {
        registerMaxValue(maxValue)
        registerMinValue(minValue)
    }

    fun <N : Number> registerLogarithmization(base: N) {
        transformer = Function { log(it, base.toDouble()) }
    }

    override fun prepareForCalculation(territoryToParameterMap: Map<Territory, ColorizationParameter>) {
        val valueStatistics = territoryToParameterMap.values.asSequence()
            .map { obj: ColorizationParameter -> obj.getValue() }
            .flatMap { obj: Optional<Double> -> obj.stream().asSequence() }
            .summarizingDouble { it }
        maxValue = transformer.apply(customMaxValue ?: valueStatistics.max)
        minValue = transformer.apply(customMinValue ?: valueStatistics.min)
        colorScheme = generateScheme(minColor.rgbColor, maxColor.rgbColor)
    }

    override fun calculateColor(colorizationParameter: ColorizationParameter): RGBColor {
        return colorizationParameter.getValue()
            .map(transformer)
            .map { ((it - minValue) / (maxValue - minValue) * 100).roundToLong() }
            .map(Long::toInt)
            .map { value: Int -> max(min(value, 100), 0) }
            .map { index: Int -> colorScheme[index] }
            .orElseGet(defaultColor::rgbColor)
    }
}
