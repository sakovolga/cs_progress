package com.example.cs_progress.mapper.impl;

import com.example.cs_progress.util.LayeredLogger;

public abstract class BaseMapper {
    protected final LayeredLogger log = new LayeredLogger(
            this.getClass(), LayeredLogger.Layer.MAPPER, true
    );

}
