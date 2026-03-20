// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript.service

import com.intellij.openapi.util.NlsSafe

enum class VueTSPluginVersion(
  @param:NlsSafe
  val versionString: String,
) {
  DEFAULT("3.2.6"),
  LEGACY("3.0.10"),

  ;
}
