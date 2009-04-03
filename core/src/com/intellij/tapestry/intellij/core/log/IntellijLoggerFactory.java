package com.intellij.tapestry.intellij.core.log;

import com.intellij.tapestry.core.log.Logger;
import com.intellij.tapestry.core.log.LoggerFactory;

public class IntellijLoggerFactory extends LoggerFactory {

    public Logger getLogger(Class clazz) {
        return new IntellijLogger(clazz.getName());
    }
}
