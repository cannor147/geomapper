@file:Suppress("MemberVisibilityCanBePrivate")

package com.github.cannor147

import com.github.cannor147.model.GeoMap
import com.github.cannor147.model.GeoMapDto
import com.github.cannor147.model.Territory
import com.github.cannor147.namer.createMap
import com.github.cannor147.namer.normalize
import com.github.cannor147.request.Request
import com.github.cannor147.request.RequestService
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.text.NumberFormat
import java.text.ParseException
import java.util.*
import javax.imageio.ImageIO

class GeoMapper {
    private val requestService: RequestService = RequestService()
    private val resourceReader: ResourceReader = ResourceReader()
    private val nameToGeoMapMap: Map<String, GeoMapDto> =
        resourceReader.readJson(GEO_MAPS, Array<GeoMapDto>::class.java).let(::createMap)

    fun createMap(request: Request): BufferedImage {
        return requestService.handleRequest(request)
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
            .map { path: String -> resourceReader.safeReadJson(path, Array<Territory>::class.java) }
            .filterNotNull()
            .flatMap { it.asSequence() }
            .toList()
            .let(::createMap)
        val map = resourceReader.readImage(dto.mapFilePath)
        val background = if (dto.backgroundFilePath != null) resourceReader.readImage(dto.backgroundFilePath) else null
        return GeoMap(dto.name, nameToTerritoryMap, map, background)
    }

    companion object {
        private const val GEO_MAPS = "geomaps.json"
        private const val PNG = "png"

        @JvmStatic
        fun safeParseNumber(number: String): Number? {
            return try {
                val text = number.replace(",", "").replace("%", "").trim { it <= ' ' }
                NumberFormat.getNumberInstance().parse(text)
            } catch (e: ParseException) {
                null
            }
        }
    }

}