package com.github.cannor147.geomapper.request.colorization

import com.github.cannor147.geomapper.Color
import com.github.cannor147.geomapper.Territory
import com.github.cannor147.geomapper.painter.RGBColor
import java.util.*
import java.util.function.ToDoubleFunction

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
    return this.fold(DoubleSummaryStatistics()) { statistics: DoubleSummaryStatistics, element: T ->
        statistics.accept(toDoubleFunction.applyAsDouble(element))
        statistics
    }
}