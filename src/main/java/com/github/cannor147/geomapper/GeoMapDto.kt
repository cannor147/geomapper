@file:Suppress("ArrayInDataClass")

package com.github.cannor147.geomapper

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.cannor147.geomapper.namer.Synonymized

data class GeoMapDto(
    @JsonProperty("name") override val name: String,
    @JsonProperty("synonyms") override val synonyms: Array<String>,
    @JsonProperty("data") val dataFilePaths: Array<String>,
    @JsonProperty("map") val mapFilePath: String,
    @JsonProperty("background") val backgroundFilePath: String?
) : Synonymized
