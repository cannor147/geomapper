package com.github.cannor147

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import java.awt.image.BufferedImage
import java.io.IOException
import java.util.*
import javax.imageio.ImageIO

internal object ResourceReader {
    private val classLoader: ClassLoader = javaClass.classLoader
    private val objectMapper: ObjectMapper = ObjectMapper()
        .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)

    fun <T> readJsonSafely(filePath: String, jsonType: Class<T>): T? = try {
        readJson(filePath, jsonType)
    } catch (e: IOException) {
        null
    }

    @Throws(IOException::class)
    fun <T> readJson(filePath: String, jsonType: Class<T>): T {
        val resource = classLoader.getResource(filePath)
        Objects.requireNonNull(resource)
        return objectMapper.readValue(resource, jsonType)
    }

    @Throws(IOException::class)
    fun readImage(filePath: String): BufferedImage {
        val resource = classLoader.getResource(filePath)
        Objects.requireNonNull(resource)
        return ImageIO.read(resource)
    }
}