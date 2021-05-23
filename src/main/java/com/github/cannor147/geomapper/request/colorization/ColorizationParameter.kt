@file:Suppress("unused")

package com.github.cannor147.geomapper.request.colorization

import com.github.cannor147.geomapper.Color

class ColorizationParameter private constructor(val color: Color?, val value: Double?) {
    constructor(color: Color) : this(color, null)
    constructor(value: Number) : this(null, value.toDouble())

    companion object {
        private val EMPTY_PARAMETER = ColorizationParameter(null, null)
        @JvmStatic
        fun empty(): ColorizationParameter {
            return EMPTY_PARAMETER
        }
    }
}