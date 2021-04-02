package com.github.cannor147.request;

import com.github.cannor147.model.GeoMap;
import com.github.cannor147.model.Color;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Queue;

@Data
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class Request {
    private final Queue<ColorizationTask> tasks;
    private final GeoMap geoMap;
    private final Color defaultColor;
}
