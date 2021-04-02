package com.github.cannor147;

import com.github.cannor147.request.Request;
import com.github.cannor147.request.RequestService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class GeoMapper {
    public static final String PNG = "png";

    private final RequestService requestService;

    public GeoMapper() {
        this.requestService = new RequestService();
    }

    public BufferedImage createMap(Request request) {
        return requestService.handleRequest(request);
    }

    public void createMapToFile(Request request, File file) throws IOException {
        ImageIO.write(createMap(request), PNG, file);
    }
}
