package com.github.cannor147.request.colorization

import com.github.cannor147.model.Color
import com.github.cannor147.model.Territory
import com.github.cannor147.painter.RGBColor
import com.github.cannor147.painter.generateScheme
import java.util.*
import java.util.function.Predicate
import kotlin.streams.asSequence

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
        valueSeparators = Optional.of(valueSeparatorSet)
            .filter(Predicate.not { obj: SortedSet<Double> -> obj.isEmpty() })
            .map<List<Double>> { c: SortedSet<Double>? -> ArrayList(c) }
            .orElseGet {
                territoryToParameterMap.values.asSequence()
                    .map { obj: ColorizationParameter -> obj.getValue() }
                    .flatMap { obj: Optional<Double> -> obj.stream().asSequence() }
                    .summarizingDouble { it }
                    .takeIf { it.count > 0 }
                    ?.let { sequenceOf(it.average) }
                    .orEmpty()
                    .toList()
            }
        val separatorCount = valueSeparators.size
        colorScheme = generateScheme(minColor.rgbColor, maxColor.rgbColor, separatorCount)
    }

    override fun calculateColor(colorizationParameter: ColorizationParameter): RGBColor = colorizationParameter
        .getValue()
        .map { findIndex(it) }
        .map { colorScheme[it] }
        .orElseGet(defaultColor::rgbColor)

    private fun findIndex(value: Double): Int = when {
        value.compareTo(valueSeparators[0]) < 0 -> 0
        value.compareTo(valueSeparators[valueSeparators.size - 1]) > 0 -> valueSeparators.size
        else -> {
            val index = Collections.binarySearch(valueSeparators, value)
            (index + 1) * if (index > 0) 1 else -1
        }
    }
}