package org.jetbrains.qodana.cpp

import com.intellij.openapi.project.Project
import com.intellij.util.PlatformUtils
import com.jetbrains.cidr.cpp.cmake.CMakeSettings
import com.jetbrains.cidr.cpp.cmake.presets.CMakeEnabledProfileInitializer

class QodanaCppCMakeEnabledProfileInitializer : CMakeEnabledProfileInitializer {
  override fun isEnabled(profile: CMakeSettings.Profile): Boolean {
    val profileName = qodanaConfig.cpp?.cmakePreset
    checkNotNull(profileName) {
      "This initializer should not be called if no CMake profile was requested"
    }

    return profile.name == profileName
  }

  override fun isApplicable(project: Project): Boolean {
    return PlatformUtils.isQodana() && qodanaConfig.cpp?.cmakePreset != null
  }
}