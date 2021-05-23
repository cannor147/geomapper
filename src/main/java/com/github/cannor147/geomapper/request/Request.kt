package com.github.cannor147.geomapper.request

import com.github.cannor147.geomapper.GeoMap
import com.github.cannor147.geomapper.request.colorization.ColorizationTask
import java.util.*

class Request internal constructor(
    internal val tasks: Queue<ColorizationTask>,
    internal val geoMap: GeoMap
)