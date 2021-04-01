package com.github.cannor147.model.request;

import com.github.cannor147.configuration.Configuration;
import com.github.cannor147.model.Color;
import com.github.cannor147.model.ColorizationTask;

import java.util.Queue;

public interface Request {
    String getConfiguration();
    Color getDefaultColor();
    Queue<ColorizationTask> toTasks(Configuration configuration);
}
