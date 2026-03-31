package org.jetbrains.qodana.cpp

import com.intellij.openapi.util.registry.Registry
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/** Registry keys for Qodana C++ timeouts. Defaults are in `intellij.qodana.cpp.xml`. */
object QodanaCppRegistry {
    val startupTimeout: Duration
        get() = Registry.intValue("qd.cpp.startup.timeout.minutes").minutes

    val workspaceReadyTimeout: Duration
        get() = Registry.intValue("qd.cpp.workspace.ready.timeout.seconds").seconds

    val progressManagerTimeout: Duration
        get() = Registry.intValue("qd.cpp.progress.manager.timeout.seconds").seconds

    val ocResolveTimeout: Duration
        get() = Registry.intValue("qd.cpp.oc.resolve.timeout.ms").milliseconds

    val projectModelTimeout: Duration
        get() = Registry.intValue("qd.cpp.project.model.timeout.seconds").seconds
}
