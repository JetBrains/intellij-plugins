package com.intellij.flexunit.log {

import mx.core.mx_internal;
import mx.logging.targets.LineFormattedTarget;

use namespace mx_internal;

public class LogTarget extends LineFormattedTarget {

    private var instance:Object;
    private var logger:Function;

    public function LogTarget(instance:Object, logger:Function) {
        this.instance = instance;
        this.logger = logger;

        includeCategory = true;
        includeDate = true;
        includeLevel = true;
        includeTime = true;
    }

    mx_internal override function internalLog(message:String):void {
        logger.apply(instance, new Array(message));
    }
}
}