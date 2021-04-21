package com.github.cannor147.request

import com.github.cannor147.model.Color
import com.github.cannor147.painter.fillArea
import com.github.cannor147.painter.fillPoint
import com.github.cannor147.painter.findArea
import com.github.cannor147.request.colorization.ColorizationTask
import java.awt.Point
import java.awt.image.BufferedImage
import java.util.*
import java.util.function.Consumer

class RequestService {
    fun handleRequest(request: Request): BufferedImage {
        val image = request.geoMap.copyMap()
        perform(image, request.tasks)
        Optional.ofNullable(request.geoMap.background)
            .map { background: BufferedImage? ->
                findArea(
                    background!!, Color.WHITE.rgbColor)
            }
            .orElseGet { emptySet() }
            .forEach(Consumer { point: Point? -> fillPoint(image, point!!, Color.WHITE.rgbColor) })
        return image
    }

    private fun perform(image: BufferedImage, tasks: Queue<ColorizationTask>) {
        tasks.forEach(Consumer { task: ColorizationTask -> perform(image, task) })
    }

    private fun perform(image: BufferedImage, task: ColorizationTask) = when {
        task.onlyPixel -> task.points.asSequence().forEach { fillPoint(image, it, task.rgbColor) }
        else -> task.points.asSequence().forEach { fillArea(image, it, task.rgbColor) }
    }
}