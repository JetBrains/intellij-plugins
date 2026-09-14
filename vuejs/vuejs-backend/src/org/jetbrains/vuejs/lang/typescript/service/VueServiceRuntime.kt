// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service

data class VueServiceRuntime(
  val version: VueLanguageToolsVersion,
) {
  internal companion object {
    val ALL: List<VueServiceRuntime> = listOf(
      VueServiceRuntime(VueLanguageToolsVersion.LEGACY),
      VueServiceRuntime(VueLanguageToolsVersion.DEFAULT),
    )
  }
}
