package org.jetbrains.qodana.php

import com.jetbrains.php.config.PhpLanguageLevel
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaConfigurationException

internal fun String.toPhpLanguageLevel(): PhpLanguageLevel {
  val level = PhpLanguageLevel.from(this)
  if (level == PhpLanguageLevel.DEFAULT && level.versionString != this) {
    val available = PhpLanguageLevel.values().joinToString { it.versionString }
    throw QodanaConfigurationException("Unknown PHP language level '$this', use one of $available")
  }
  return level
}