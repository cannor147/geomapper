package model.request;

import configuration.Configuration;
import model.Color;
import model.ColorizationTask;

import java.util.Queue;

public interface Request {
    String getConfiguration();
    Color getDefaultColor();
    Queue<ColorizationTask> toTasks(Configuration configuration);
}
