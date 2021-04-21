@file:Suppress("unused")

package com.github.cannor147.request.colorization

import com.github.cannor147.model.Color
import java.util.*

class ColorizationParameter private constructor(private val color: Color?, private val value: Double?) {
    constructor(color: Color?) : this(color, null)
    constructor(value: Number) : this(null, value.toDouble())

    fun getColor(): Optional<Color> {
        return Optional.ofNullable(color)
    }

    fun getValue(): Optional<Double> {
        return Optional.ofNullable(value)
    }

    companion object {
        private val EMPTY_PARAMETER = ColorizationParameter(null, null)
        @JvmStatic
        fun empty(): ColorizationParameter {
            return EMPTY_PARAMETER
        }
    }
}