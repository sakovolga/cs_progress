package com.example.cs_progress.service.impl;

import com.example.cs_progress.util.LayeredLogger;

public abstract class BaseService {
    protected final LayeredLogger log = new LayeredLogger(
            this.getClass(), LayeredLogger.Layer.SERVICE, true
    );

}
