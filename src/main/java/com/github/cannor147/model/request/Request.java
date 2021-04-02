package com.github.cannor147.model.request;

import com.github.cannor147.configuration.Configuration;
import com.github.cannor147.model.Color;
import com.github.cannor147.model.ColorizationTask;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Queue;

@Data
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class Request {
    private final Queue<ColorizationTask> tasks;
    private final Configuration configuration;
    private final Color defaultColor;
}
