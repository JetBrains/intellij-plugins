// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.util.NlsSafe
import com.intellij.util.text.SemVer

enum class VueLanguageToolsVersion(
  @param:NlsSafe
  val versionString: String,
) {
  DEFAULT("3.2.7"),
  LEGACY("3.0.10"),

  ;

  companion object {
    private val LOG = logger<VueLanguageToolsVersion>()

    fun fromVersion(versionString: String): VueLanguageToolsVersion? =
      entries.firstOrNull { it.versionString == versionString }

    fun fromVersionOrInfer(versionString: String): VueLanguageToolsVersion {
      val existingVersion = fromVersion(versionString)
      if (existingVersion != null) {
        return existingVersion
      }

      LOG.warn("No version found for $versionString")
      val semVer = SemVer.parseFromText(versionString)
      if (semVer == null) {
        LOG.warn("Cannot parse Vue Language Tools version '$versionString', falling back to DEFAULT")
        return DEFAULT
      }

      val legacySemVer = requireNotNull(SemVer.parseFromText(LEGACY.versionString))
      return if (legacySemVer.isGreaterOrEqualThan(semVer)) // looks like version that supports Vue 2
        LEGACY
      else
        DEFAULT
    }
  }
}