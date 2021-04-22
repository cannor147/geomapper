package com.github.cannor147.painter

import java.awt.Point
import java.awt.image.BufferedImage
import java.util.*

private val SHIFTS = arrayOf(Point(0, +1), Point(0, -1), Point(+1, 0), Point(-1, 0))

fun findArea(image: BufferedImage, rgbColor: RGBColor): Set<Point> = generateCoordinates(image.width, image.height)
    .flatten()
    .filter { point: Point -> rgbColor == getRGBColor(image, point) }
    .toSet()

fun fillArea(image: BufferedImage, point: Point, rgbColor: RGBColor) {
    val originalColor = getRGBColor(image, point)
    if (originalColor == rgbColor) {
        return
    }
    val area: MutableSet<Point> = HashSet()
    val queue: Queue<Point> = LinkedList()
    queue.add(point)
    area.add(point)
    while (!queue.isEmpty()) {
        val currentPoint = queue.poll()
        SHIFTS.asSequence()
            .map(::Point)
            .onEach { it.translate(currentPoint.x, currentPoint.y) }
            .filter { it.x >= 0 && it.x < image.width }
            .filter { it.y >= 0 && it.y < image.height }
            .filterNot { area.contains(it) }
            .filter { originalColor == getRGBColor(image, it) }
            .forEach {
                queue.add(it)
                area.add(it)
            }
    }
    area.forEach { fillPoint(image, it, rgbColor) }
}

fun fillPoint(image: BufferedImage, point: Point, rgbColor: RGBColor) {
    image.setRGB(point.x, point.y, rgbColor.toInt())
}

@JvmOverloads
fun generateScheme(from: RGB, to: RGB, count: Int = 100): List<RGBColor> {
    val distance = RGBDistance.between(to, from)
    return (0..count).asSequence()
        .map { it.toDouble() / count }
        .map(distance::multiply)
        .map(from::add)
        .map(RGB::asColor)
        .toList()
}

private fun getRGBColor(image: BufferedImage, point: Point): RGBColor = image.getRGB(point.x, point.y)
    .let(RGBColor::fromInt)

private fun generateCoordinates(width: Int, height: Int): Array<Array<Point>> =
    Array(width) { x -> Array(height) { y -> Point(x, y) } }