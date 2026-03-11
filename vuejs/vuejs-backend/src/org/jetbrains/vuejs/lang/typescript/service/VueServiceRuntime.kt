// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service

sealed interface VueServiceRuntime {
  data class Bundled(
    val version: VueLanguageToolsVersion,
  ) : VueServiceRuntime

  data object Manual :
    VueServiceRuntime
}

internal val allVueServiceRuntimes: List<VueServiceRuntime> =
  listOf(
    VueServiceRuntime.Bundled(VueLanguageToolsVersion.LEGACY),
    VueServiceRuntime.Bundled(VueLanguageToolsVersion.DEFAULT),
    VueServiceRuntime.Manual,
  )
