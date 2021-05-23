@file:Suppress("MemberVisibilityCanBePrivate")

package com.github.cannor147.geomapper

import com.github.cannor147.geomapper.namer.createMap
import com.github.cannor147.geomapper.namer.normalize
import com.github.cannor147.geomapper.request.Request
import com.github.cannor147.geomapper.request.RequestService
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.util.*
import javax.imageio.ImageIO

private const val GEO_MAPS = "geomaps.json"
private const val PNG = "png"

class GeoMapper {
    private val nameToGeoMapMap: Map<String, GeoMapDto> =
        ResourceReader.readJson(GEO_MAPS, Array<GeoMapDto>::class.java).let(::createMap)

    fun createMap(request: Request): BufferedImage {
        return RequestService.handleRequest(request)
    }

    @Throws(IOException::class)
    fun createMapToFile(request: Request, file: File) {
        file.mkdirs()
        ImageIO.write(createMap(request), PNG, file)
    }

    @Throws(IOException::class)
    fun findGeoMap(geoMapName: String): GeoMap {
        val normalizedKey = normalize(geoMapName)
        require(nameToGeoMapMap.containsKey(normalizedKey)) { "No such geo map." }
        val dto: GeoMapDto = nameToGeoMapMap[normalizedKey]!!
        val nameToTerritoryMap: Map<String, Territory> = dto.dataFilePaths.asSequence()
            .map { path: String -> ResourceReader.readJsonSafely(path, Array<Territory>::class.java) }
            .filterNotNull()
            .flatMap { it.asSequence() }
            .toList()
            .let(::createMap)
        val map = ResourceReader.readImage(dto.mapFilePath)
        val background = if (dto.backgroundFilePath != null) ResourceReader.readImage(dto.backgroundFilePath) else null
        return GeoMap(dto.name, nameToTerritoryMap, map, background)
    }
}
