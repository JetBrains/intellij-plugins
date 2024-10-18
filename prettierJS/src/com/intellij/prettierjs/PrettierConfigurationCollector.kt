// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs

import com.intellij.internal.statistic.eventLog.EventLogGroup
import com.intellij.internal.statistic.eventLog.events.EventFields.Enum
import com.intellij.internal.statistic.eventLog.events.EventId3
import com.intellij.internal.statistic.service.fus.collectors.CounterUsagesCollector

internal object PrettierConfigurationCollector : CounterUsagesCollector() {
  private val GROUP: EventLogGroup = EventLogGroup("prettier.configuration", 2)

  override fun getGroup(): EventLogGroup = GROUP

  private val AUTO_ENABLE_IN_NEW_PROJECT: EventId3<EnabledStatus, PackageDeclarationLocation, ConfigLocation> = GROUP.registerEvent(
    "auto.enable.in.new.project",
    Enum("enabled_status", EnabledStatus::class.java),
    Enum("package_declaration_location", PackageDeclarationLocation::class.java),
    Enum("config_location", ConfigLocation::class.java)
  )

  enum class EnabledStatus {
    AUTOMATIC,
    UNCHANGED
  }

  enum class PackageDeclarationLocation {
    IN_PROJECT_ROOT_PACKAGE,
    IN_SUBDIR_PACKAGE,
    IN_MULTIPLE_SUBDIR_PACKAGES,
    NONE
  }

  enum class ConfigLocation {
    CONFIG_FILE,
    PACKAGE_JSON,
    MIXED,
    NONE
  }

  fun logAutoEnableInNewProject(
    resolvedConfigMode: EnabledStatus,
    packageDeclarationPlace: PackageDeclarationLocation = PackageDeclarationLocation.NONE,
    configLocation: ConfigLocation = ConfigLocation.NONE,
  ) {
    AUTO_ENABLE_IN_NEW_PROJECT.log(resolvedConfigMode, packageDeclarationPlace, configLocation)
  }
}
