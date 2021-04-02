package com.github.cannor147;

import com.github.cannor147.model.Color;
import com.github.cannor147.model.GeoMap;
import com.github.cannor147.request.Request;
import com.github.cannor147.request.ScaleRequestBuilder;
import com.github.cannor147.request.StepRequestBuilder;
import com.github.cannor147.request.StraightRequestBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class Main {
    public static final String COUNTRIES = "countries";

    public static void main(String[] args) throws IOException, URISyntaxException {
        final ResourceReader resourceReader = new ResourceReader();
        final GeoMapper geoMapper = new GeoMapper();
        final GeoMap geoMap = geoMapper.findGeoMap(COUNTRIES);

        createStraight(geoMapper, geoMap);
        createScale(resourceReader, geoMapper, geoMap);
        createStep(resourceReader, geoMapper, geoMap);
    }

    public static void createStraight(GeoMapper geoMapper, GeoMap geoMap) throws IOException {
        final Request request = new StraightRequestBuilder(geoMap)
                .appendAll(Color.BLUE, "Iceland", "Denmark", "Norway", "Sweden", "Finland", "Estonia", "Latvia",
                        "Lithuania", "Russia", "Belarus", "Poland", "Ukraine", "Moldova", "Czechia", "Slovakia",
                        "Slovenia", "Croatia", "Bosnia", "Serbia", "Montenegro", "Macedonia", "Kosovo", "Albania",
                        "Bulgaria", "Romania", "Hungary", "Austria", "Switzerland", "Liechtenstein", "Germany",
                        "Netherlands", "Belgium", "Luxembourg", "UK", "Ireland", "France", "Andorra", "Spain",
                        "Portugal", "Monaco", "Italy", "San Marino", "Vatican", "Greece", "Malta", "Cyprus", "Turkey",
                        "Armenia", "Georgia", "Azerbaijan", "Kazakhstan", "Uzbekistan", "Kyrgyzstan", "Turkmenistan")
                .appendAll(Color.RED, "China", "Mongolia", "North Korea", "South Korea", "Japan", "Taiwan")
                .appendAll(Color.ORANGE, "Cape Verde", "Senegal", "Gambia", "Guinea Bissau", "Guinea", "Sierra Leone",
                        "Liberia", "Ivory Coast", "Mali", "Burkina Faso", "Ghana", "Togo", "Benin", "Niger", "Nigeria",
                        "Cameroon", "Chad", "CAR", "South Sudan", "Eritrea", "Ethiopia", "Somalia", "Kenya", "Uganda",
                        "Rwanda", "Burundi", "DRC", "Republic of Congo", "Gabon", "Equatorial Guinea", "Sao Tome",
                        "Namibia", "Tanzania", "Zimbabwe", "Zambia", "Malawi", "Angola", "Botswana", "Mozambique",
                        "South Africa", "Lesotho", "Eswatini", "Madagascar", "Seychelles", "Comoros", "Mauritius")
                .appendAll(Color.YELLOW, "India", "Nepal", "Bhutan", "Bangladesh", "Sri Lanka", "Maldives")
                .appendAll(Color.GREEN, "Pakistan", "Tajikistan", "Afghanistan", "Iran", "Iraq", "Saudi Arabia",
                        "Yemen", "Oman", "UAE", "Qatar", "Bahrain", "Kuwait", "Syria", "Lebanon", "Jordan", "Israel",
                        "Egypt", "Sudan", "Djibouti", "Libya", "Tunisia", "Algeria", "Morocco", "Mauritania")
                .appendAll(Color.FUCHSIA, "Brunei", "Cambodia", "Indonesia", "Laos", "Malaysia", "Myanmar",
                        "Philippines", "Singapore", "Thailand", "Vietnam", "Philippines", "Timor Leste")
                .appendAll(Color.TEAL, "Canada", "USA", "Mexico", "Guatemala", "Belize", "El Salvador", "Honduras",
                        "Nicaragua", "Costa Rica", "Panama", "Bahamas", "Cuba", "Haiti", "Dominican Republic",
                        "Dominica", "Antigua", "Saint Kitts", "Saint Lucia", "Saint Vincent", "Grenada", "Trinidad",
                        "Barbados", "Colombia", "Venezuela", "Guyana", "Suriname", "Brazil", "Ecuador", "Peru", "Chile",
                        "Argentina", "Bolivia", "Paraguay", "Uruguay", "Australia", "New Zealand", "Papua New Guinea",
                        "Marshall Islands", "Solomon Islands", "Micronesia", "Tonga", "Fiji", "Samoa", "Kiribati",
                        "Nauru", "Palau", "Tuvalu", "Vanuatu")
                .build();

        final File file = new File("example/straight.png");
        geoMapper.createMapToFile(request, file);
    }

    public static void createScale(ResourceReader resourceReader, GeoMapper geoMapper, GeoMap geoMap) throws IOException, URISyntaxException {
        final Request request = new ScaleRequestBuilder(geoMap)
                .fromCsv(resourceReader.getResource("example/gdp.csv"), 1, 2)
                .useColor(Color.GREEN)
                .addLogarithmization(10)
                .build();
        geoMapper.createMapToFile(request, new File("example/scale.png"));
    }

    public static void createStep(ResourceReader resourceReader, GeoMapper geoMapper, GeoMap geoMap) throws IOException, URISyntaxException {
        final Request request = new StepRequestBuilder(geoMap)
                .fromCsv(resourceReader.getResource("example/hdr.csv"), 2, 3)
                .useColor(Color.BLUE, Color.RED)
                .withSeparators(0.8, 0.7, 0.55)
                .build();
        geoMapper.createMapToFile(request, new File("example/step.png"));
    }
}
