import configuration.Configurer;
import model.request.Request;
import model.request.ScaleRequest;
import model.request.StraightRequest;
import resources.ResourceReader;
import service.RequestService;

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
        return handleRequest(request);
    }

    public void createMapToFile(Request request, File file) throws IOException {
        ImageIO.write(createMap(request), PNG, file);
    }

    private BufferedImage handleRequest(Request request) throws IOException {
        if (request instanceof ScaleRequest) {
            return requestService.handleRequest((ScaleRequest) request);
        } else if (request instanceof StraightRequest) {
            return requestService.handleRequest((StraightRequest) request);
        }

        throw new UnsupportedOperationException("Unknown request type.");
    }
}
