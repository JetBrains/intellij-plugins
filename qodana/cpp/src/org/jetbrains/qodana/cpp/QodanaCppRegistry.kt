package org.jetbrains.qodana.cpp

import com.intellij.openapi.util.registry.Registry

object QodanaCppRegistry {
  val isForceCMakeOutput: Boolean
    get() = Registry.`is`("qd.force.cmake.output.enabled", true)
}