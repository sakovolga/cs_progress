package com.example.cs_progress.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.Map;

public class LayeredLogger {

    public enum Layer { CONTROLLER, SERVICE, LISTENER, MAPPER }

    private static final Map<Layer, String> LAYER_COLORS = new EnumMap<>(Layer.class);
    static {
        LAYER_COLORS.put(Layer.CONTROLLER, "\u001B[34m"); // Blue
        LAYER_COLORS.put(Layer.SERVICE, "\u001B[32m");    // Green
        LAYER_COLORS.put(Layer.LISTENER, "\u001B[33m");   // Yellow
        LAYER_COLORS.put(Layer.MAPPER, "\u001B[35m");     // Purple
    }

    private static final String RESET = "\u001B[0m";

    private final Logger logger;
    private final Layer layer;
    private final boolean enableColors;

    public LayeredLogger(Class<?> clazz, Layer layer, boolean enableColors) {
        this.logger = LoggerFactory.getLogger(clazz);
        this.layer = layer;
        this.enableColors = enableColors;
    }

    private String colorize(String msg) {
        if (!enableColors) return msg;
        return LAYER_COLORS.getOrDefault(layer, "") + msg + RESET;
    }

    public void info(String msg, Object... args) { logger.info(colorize(msg), args); }
    public void warn(String msg, Object... args) { logger.warn(colorize(msg), args); }
    public void error(String msg, Object... args) { logger.error(colorize(msg), args); }
    public void success(String msg, Object... args) { logger.info(colorize(msg), args); }
}
