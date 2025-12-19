package com.example.cs_progress.controller;

import com.example.cs_progress.util.LayeredLogger;

public abstract class BaseController {
    protected final LayeredLogger log = new LayeredLogger(
            this.getClass(), LayeredLogger.Layer.CONTROLLER, true
    );
}
