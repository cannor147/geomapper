package com.github.cannor147.request.colorization

import com.github.cannor147.model.Color
import com.github.cannor147.model.Territory
import com.github.cannor147.painter.RGBColor
import com.github.cannor147.painter.generateScheme
import java.util.*

class StepColorizationScheme : ColorizationScheme() {
    private val valueSeparatorSet: SortedSet<Double> = TreeSet()
    private var maxColor: Color = Color.GREEN
    private var minColor: Color = Color.RED
    private var colorScheme: List<RGBColor> = emptyList()
    private var valueSeparators: List<Double> = emptyList()

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

    fun registerSeparator(separator: Double) {
        valueSeparatorSet.add(separator)
    }

    override fun prepareForCalculation(territoryToParameterMap: Map<Territory, ColorizationParameter>) {
        valueSeparators = valueSeparatorSet
            .takeUnless(SortedSet<Double>::isEmpty)
            ?.let { ArrayList(it) }
            ?: territoryToParameterMap.values.asSequence()
            .map { it.value }
            .flatMap { it?.let { sequenceOf(it) }.orEmpty() }
            .summarizingDouble { it }
            .takeIf { it.count > 0 }
            ?.let { sequenceOf(it.average) }
            .orEmpty()
            .toList()
        colorScheme = generateScheme(minColor.rgbColor, maxColor.rgbColor, valueSeparators.count())
    }

    override fun calculateColor(colorizationParameter: ColorizationParameter): RGBColor = colorizationParameter.value
        ?.let { findIndex(it) }
        ?.let { colorScheme[it] }
        ?: defaultColor.rgbColor

    private fun findIndex(value: Double): Int = when {
        value.compareTo(valueSeparators[0]) < 0 -> 0
        value.compareTo(valueSeparators[valueSeparators.size - 1]) > 0 -> valueSeparators.size
        else -> {
            val index = Collections.binarySearch(valueSeparators, value)
            (index + 1) * if (index > 0) 1 else -1
        }
    }
}