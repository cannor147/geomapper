package com.github.cannor147.model

import com.github.cannor147.namer.normalize
import java.awt.image.BufferedImage
import java.util.*

class GeoMap(
    val name: String,
    private val nameToTerritoryMap: Map<String, Territory>,
    val map: BufferedImage,
    val background: BufferedImage? = null,
) {
    fun find(territoryName: String?): Territory? = territoryName
        ?.let(::normalize)
        ?.split("\\s+".toRegex())
        ?.asSequence()
        .orEmpty()
        .filterNot("the"::equals)
        .joinToString(" ")
        .let { nameToTerritoryMap[it] }

    fun findOwner(territory: Territory): Territory? = territory.officialOwner.let(this::find)

    fun territories(): Set<Territory> = HashSet(nameToTerritoryMap.values)

    fun copyMap(): BufferedImage {
        val result = BufferedImage(map.width, map.height, BufferedImage.TYPE_3BYTE_BGR)
        result.graphics.drawImage(map, 0, 0, null)
        return result
    }
}