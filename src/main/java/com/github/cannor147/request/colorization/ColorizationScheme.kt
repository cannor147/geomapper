package com.github.cannor147.request.colorization

import com.github.cannor147.model.Color
import com.github.cannor147.model.Territory
import com.github.cannor147.painter.RGBColor
import java.util.*
import java.util.function.ToDoubleFunction
import java.util.stream.Collectors
import kotlin.streams.asStream

abstract class ColorizationScheme {
    protected var defaultColor = Color.SILVER

    fun registerDefaultColor(defaultColor: Color) {
        this.defaultColor = defaultColor
    }

    open fun prepareForCalculation(territoryToParameterMap: Map<Territory, ColorizationParameter>) {
        // No operations.
    }

    abstract fun calculateColor(colorizationParameter: ColorizationParameter): RGBColor
}

fun <T> Sequence<T>.summarizingDouble(toDoubleFunction: ToDoubleFunction<T>): DoubleSummaryStatistics {
    return this.asStream().collect(Collectors.summarizingDouble(toDoubleFunction))
}