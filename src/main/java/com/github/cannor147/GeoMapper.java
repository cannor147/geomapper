package com.github.cannor147;

import com.github.cannor147.configuration.Configurer;
import com.github.cannor147.model.request.Request;
import com.github.cannor147.resources.ResourceReader;
import com.github.cannor147.service.RequestService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class GeoMapper {
    public static final String PNG = "png";

    private final RequestService requestService;

    public GeoMapper() throws IOException {
        this.requestService = new RequestService(new Configurer(new ResourceReader()));
    }

    public BufferedImage createMap(Request request) throws IOException {
        return requestService.handleRequest(request);
    }

    public void createMapToFile(Request request, File file) throws IOException {
        ImageIO.write(createMap(request), PNG, file);
    }
}
