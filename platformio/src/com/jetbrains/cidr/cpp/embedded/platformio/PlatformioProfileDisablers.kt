package com.jetbrains.cidr.cpp.embedded.platformio

import com.intellij.execution.configurations.RunProfile
import com.intellij.profiler.clion.CLionProfilerDisabler

class PlatformioProfilerDisabler : CLionProfilerDisabler {
  override fun disableCLionProfilerFor(runConfiguration: RunProfile): Boolean = runConfiguration is PlatformioDebugConfiguration
}