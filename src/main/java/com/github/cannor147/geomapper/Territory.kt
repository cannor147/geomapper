@file:Suppress("ArrayInDataClass")

package com.github.cannor147.geomapper

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.cannor147.geomapper.namer.Synonymized
import java.awt.Point

data class Territory(
    @JsonProperty("name") override val name: String,
    @JsonProperty("synonyms") override val synonyms: Array<String>,
    @JsonProperty("points") val points: Array<Point>,
    @JsonProperty("officialOwner") val officialOwner: String?,
    @JsonProperty("officialOwnerBorder") val officialOwnerBorder: Array<Point>?
) : Synonymized
