@file:Suppress("unused")

package com.github.cannor147.geomapper.request

import com.github.cannor147.geomapper.Color
import com.github.cannor147.geomapper.GeoMap
import com.github.cannor147.geomapper.Territory
import com.github.cannor147.geomapper.painter.RGBColor
import com.github.cannor147.geomapper.request.colorization.ColorizationParameter
import com.github.cannor147.geomapper.request.colorization.ColorizationParameter.Companion.empty
import com.github.cannor147.geomapper.request.colorization.ColorizationScheme
import com.github.cannor147.geomapper.request.colorization.ColorizationTask
import com.github.cannor147.geomapper.request.colorization.StraightColorizationScheme
import java.awt.Point
import java.util.*
import java.util.function.Function

class RequestBuilder(private val geoMap: GeoMap) {
    private var unofficialStateBehavior: UnofficialStateBehavior = UnofficialStateBehavior.INCLUDE_UNMENTIONED
    private val territoryToParameterMap: MutableMap<Territory, ColorizationParameter> = HashMap()
    private val territoryToSchemeMap: MutableMap<Territory, ColorizationScheme> = HashMap()
    private var currentScheme: ColorizationScheme = StraightColorizationScheme()

    fun build(): Request {
        territoryToSchemeMap.values.asSequence()
            .distinct()
            .forEach { it.prepareForCalculation(territoryToParameterMap) }
        return geoMap.territories()
            .groupBy(territoryToParameterMap::containsKey)
            .flatMap { (mentioned, territories) -> territories.map { mentioned to it } }
            .flatMap { (mentioned: Boolean, territory: Territory) ->
                val owner = geoMap.findOwner(territory)
                val inclusionOwner = owner
                    ?.takeIf { territoryToParameterMap.containsKey(it) }
                    ?.takeIf { mentioned && unofficialStateBehavior.includeMentioned }
                    ?: owner?.takeIf { !mentioned && unofficialStateBehavior.includeUnmentioned }
                if (inclusionOwner != null) {
                    val rgbColor = calculateColor(inclusionOwner)
                    val borderTask = toTask(territory.officialOwnerBorder, rgbColor, true)
                    val task = toTask(territory.points, rgbColor, false)
                    sequenceOf(borderTask, task)
                } else {
                    sequenceOf(toTask(territory.points, calculateColor(territory), false))
                }
            }
            .toCollection(LinkedList())
            .let { Request(it, geoMap) }
    }

    fun changeScheme(colorizationScheme: ColorizationScheme): RequestBuilder {
        currentScheme = colorizationScheme
        return this
    }

    fun withColor(data: Pair<String, Color>): RequestBuilder {
        return withParameter(sequenceOf(data)) { ColorizationParameter(it) }
    }

    fun withColor(name: String, color: Color): RequestBuilder {
        return withParameter(sequenceOf(name to color)) { ColorizationParameter(it) }
    }

    fun withColor(names: Array<String?>, color: Color): RequestBuilder {
        return names.asSequence()
            .filterNotNull()
            .map { it to color }
            .let { pairs -> withParameter(pairs) { ColorizationParameter(it) } }
    }

    fun withColor(names: Iterable<String?>, color: Color): RequestBuilder {
        return names.asSequence()
            .filterNotNull()
            .map { it to color }
            .let { pairs -> withParameter(pairs) { ColorizationParameter(it) } }
    }

    fun withColors(data: Iterable<Pair<String, Color>>): RequestBuilder {
        return withParameter(data.asSequence()) { ColorizationParameter(it) }
    }

    fun withColors(data: Map<String, Color>): RequestBuilder {
        return withParameter(data.toList().asSequence()) { ColorizationParameter(it) }
    }

    fun <N : Number> withValue(data: Pair<String, N>): RequestBuilder {
        return withParameter(sequenceOf(data)) { ColorizationParameter(it) }
    }

    fun <N : Number> withValue(name: String, value: N): RequestBuilder {
        return withParameter(sequenceOf(name to value)) { ColorizationParameter(it) }
    }

    fun <N : Number> withValues(data: Map<String, N>): RequestBuilder {
        return withParameter(data.toList().asSequence()) { ColorizationParameter(it) }
    }

    fun <N : Number> withValues(data: Iterable<Pair<String, N>>): RequestBuilder {
        return withParameter(data.asSequence()) { ColorizationParameter(it) }
    }

    fun <N : Number> withValues(names: List<String>, values: List<N>): RequestBuilder {
        return withParameter(names.asSequence().zip(values.asSequence())) { ColorizationParameter(it) }
    }

    private fun <T> withParameter(pairs: Sequence<Pair<String, T>>, parameterizer: Parameterizer<T>): RequestBuilder {
        pairs
            .map { (name, value) -> (geoMap.find(name) to value) }
            .mapNotNull { (territory, value) -> if (territory == null) null else territory to value  }
            .filterNot { territoryToParameterMap.containsKey(it.first) }
            .map { (territory, value) -> territory to parameterizer.apply(value) }
            .forEach { (territory, parameter) ->
                territoryToParameterMap[territory] = parameter
                territoryToSchemeMap[territory] = currentScheme
            }
        return this
    }

    fun withState(unofficialStateBehavior: UnofficialStateBehavior): RequestBuilder {
        this.unofficialStateBehavior = unofficialStateBehavior
        return this
    }

    private fun calculateColor(territory: Territory): RGBColor {
        val scheme = territoryToSchemeMap.getOrDefault(territory, currentScheme)
        return scheme.calculateColor(territoryToParameterMap.getOrDefault(territory, empty()))
    }

    private fun toTask(points: Array<Point>?, color: RGBColor, onlyPixel: Boolean): ColorizationTask {
        return points?.asSequence()
            .orEmpty()
            .toList()
            .let { ColorizationTask(it, color, onlyPixel) }
    }
}

typealias Parameterizer<T> = Function<T, ColorizationParameter>