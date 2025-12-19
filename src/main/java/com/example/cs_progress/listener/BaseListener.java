package com.example.cs_progress.listener;

import com.example.cs_progress.util.LayeredLogger;

public abstract class BaseListener {
    protected final LayeredLogger log = new LayeredLogger(
            this.getClass(), LayeredLogger.Layer.LISTENER, true
    );

}
