@file:Suppress("unused")

package com.github.cannor147.request

import com.github.cannor147.model.Color
import com.github.cannor147.model.GeoMap
import com.github.cannor147.model.Territory
import com.github.cannor147.painter.RGBColor
import com.github.cannor147.request.colorization.ColorizationParameter
import com.github.cannor147.request.colorization.ColorizationParameter.Companion.empty
import com.github.cannor147.request.colorization.ColorizationScheme
import com.github.cannor147.request.colorization.ColorizationTask
import com.github.cannor147.request.colorization.StraightColorizationScheme
import one.util.streamex.EntryStream
import org.apache.commons.lang3.tuple.Pair
import java.awt.Point
import java.util.*
import java.util.function.Function
import java.util.function.Predicate

class RequestBuilder(private val geoMap: GeoMap) {
    private var unofficialStateBehavior: UnofficialStateBehavior = UnofficialStateBehavior.INCLUDE_UNMENTIONED
    private val territoryToParameterMap: MutableMap<Territory, ColorizationParameter> = HashMap()
    private val territoryToSchemeMap: MutableMap<Territory, ColorizationScheme> = HashMap()
    private var currentScheme: ColorizationScheme = StraightColorizationScheme()

    fun build(): Request {
        territoryToSchemeMap.values.stream()
            .distinct()
            .forEach { colorizationScheme: ColorizationScheme ->
                colorizationScheme.prepareForCalculation(territoryToParameterMap)
            }
        return geoMap.territories()
            .groupBy(territoryToParameterMap::containsKey)
            .flatMap { (mentioned, territories) -> territories.map { Pair.of(mentioned, it) } }
            .flatMap { (mentioned: Boolean, territory: Territory) ->
                val owner = geoMap.findOwner(territory)
                val inclusionOwner = owner
                    .filter { territoryToParameterMap.containsKey(it) }
                    .filter { mentioned && unofficialStateBehavior.includeMentioned }
                    .or { owner.filter { !mentioned && unofficialStateBehavior.includeUnmentioned } }
                if (inclusionOwner.isPresent) {
                    val rgbColor = calculateColor(inclusionOwner.get())
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

    fun buildOptional(): Optional<Request> {
        return Optional.of(build())
    }

    fun changeScheme(colorizationScheme: ColorizationScheme): RequestBuilder {
        currentScheme = colorizationScheme
        return this
    }

    fun withColor(data: Pair<String, Color>): RequestBuilder {
        return withParameter(sequenceOf(data)) { ColorizationParameter(it) }
    }

    fun withColor(name: String, color: Color): RequestBuilder {
        return withParameter(EntryStream.of(name, color)) { ColorizationParameter(it) }
    }

    fun withColor(names: Array<String?>, color: Color): RequestBuilder {
        return names.asSequence()
            .filterNotNull()
            .map { Pair.of(it, color) }
            .let { pairs -> withParameter(pairs) { ColorizationParameter(it) } }
    }

    fun withColor(names: Iterable<String?>, color: Color): RequestBuilder {
        return names.asSequence()
            .filterNotNull()
            .map { Pair.of(it, color) }
            .let { pairs -> withParameter(pairs) { ColorizationParameter(it) } }
    }

    fun withColors(data: Iterable<Pair<String, Color>>): RequestBuilder {
        return withParameter(data.asSequence()) { ColorizationParameter(it) }
    }

    fun withColors(data: Map<String, Color>): RequestBuilder {
        return withParameter(EntryStream.of(data)) { ColorizationParameter(it) }
    }

    fun <N : Number> withValue(data: Pair<String, N>): RequestBuilder {
        return withParameter(sequenceOf(data)) { ColorizationParameter(it) }
    }

    fun <N : Number> withValue(name: String, value: N): RequestBuilder {
        return withParameter(EntryStream.of(name, value)) { ColorizationParameter(it) }
    }

    fun <N : Number> withValues(data: Map<String, N>): RequestBuilder {
        return withParameter(EntryStream.of(data)) { ColorizationParameter(it) }
    }

    fun <N : Number> withValues(data: Iterable<Pair<String, N>>): RequestBuilder {
        return withParameter(data.asSequence()) { ColorizationParameter(it) }
    }

    fun <N : Number> withValues(names: List<String?>?, values: List<N>?): RequestBuilder {
        return withParameter(EntryStream.zip(names, values)) { ColorizationParameter(it) }
    }

    private fun <T> withParameter(pairs: Sequence<Pair<String, T>>, parameterizer: Parameterizer<T>): RequestBuilder {
        return withParameter(EntryStream.of(pairs.associateBy({ it.key }, { it.value })), parameterizer)
    }

    private fun <T> withParameter(stream: EntryStream<String, T>, parameterizer: Parameterizer<T>): RequestBuilder {
        stream.mapKeys { territoryName: String? -> geoMap.find(territoryName) }
            .flatMapKeys { obj: Optional<Territory> -> obj.stream() }
            .filterKeys(Predicate.not { key: Territory -> territoryToParameterMap.containsKey(key) })
            .mapValues(parameterizer)
            .forKeyValue { territory: Territory, colorizationParameter: ColorizationParameter ->
                territoryToParameterMap[territory] = colorizationParameter
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