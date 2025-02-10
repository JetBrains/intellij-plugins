package org.jetbrains.astro.codeInsight.refs

import com.intellij.lang.javascript.inspections.JSConfigImplicitUsageProvider
import org.jetbrains.astro.codeInsight.ASTRO_CONFIG_NAME

class AstroConfigImplicitUsageProvider : JSConfigImplicitUsageProvider() {
  override val configNames: Set<String> = setOf(ASTRO_CONFIG_NAME)
}