package com.github.cannor147.request

import com.github.cannor147.model.GeoMap
import com.github.cannor147.request.colorization.ColorizationTask
import java.util.*

class Request internal constructor(
    internal val tasks: Queue<ColorizationTask>,
    internal val geoMap: GeoMap
)