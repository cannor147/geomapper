package com.github.cannor147;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

class ResourceReader {
    private final ObjectMapper objectMapper;
    private final ClassLoader classLoader;

    public ResourceReader() {
        this.classLoader = getClass().getClassLoader();
        this.objectMapper = new ObjectMapper().configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    }

    public <T> T readJson(String filePath, Class<T> jsonType) throws IOException {
        final URL resource = classLoader.getResource(filePath);
        Objects.requireNonNull(resource);
        return objectMapper.readValue(resource, jsonType);
    }

    public BufferedImage readImage(String filePath) throws IOException {
        final URL resource = classLoader.getResource(filePath);
        Objects.requireNonNull(resource);
        return ImageIO.read(resource);
    }

    public File getResource(String filePath) throws URISyntaxException {
        return new File(classLoader.getResource(filePath).toURI());
    }
}
