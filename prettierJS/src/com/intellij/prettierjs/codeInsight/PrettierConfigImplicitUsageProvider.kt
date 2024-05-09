// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.prettierjs.codeInsight

import com.intellij.lang.javascript.inspections.JSConfigImplicitUsageProvider
import com.intellij.prettierjs.PrettierUtil.CONFIG_FILE_NAME


class PrettierConfigImplicitUsageProvider : JSConfigImplicitUsageProvider() {
  override val configNames = setOf(CONFIG_FILE_NAME)
}