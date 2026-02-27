package org.intellij.qodana.rust

import com.intellij.openapi.util.registry.Registry
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

object QodanaRustRegistry {
  val configurationTimeout: Duration
    get() = Registry.intValue("qd.rust.configuration.timeout.minutes", 10).minutes
}
