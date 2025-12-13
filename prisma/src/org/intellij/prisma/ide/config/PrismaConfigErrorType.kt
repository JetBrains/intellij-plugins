// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.ide.config

import org.intellij.prisma.PrismaBundle

private val MISSING_ENV_ERROR = Regex("""Missing required environment variable(:\s*(\w+))?""", RegexOption.IGNORE_CASE)

enum class PrismaConfigErrorType(
  val externalCode: String? = null,
  val regex: Regex? = null,
  val variableIdx: Int = -1,
  val matcher: (Throwable) -> Boolean = { false },
) {
  MISSING_ENVIRONMENT("PrismaConfigEnvError", MISSING_ENV_ERROR, variableIdx = 2),
  TSX_PACKAGE_MISSING(matcher = { it is RuntimeException && it.message?.contains(PrismaBundle.message("prisma.config.tsx.package.not.found.error")) == true });

  fun matches(throwable: Throwable): Boolean {
    val message = throwable.cause?.message ?: throwable.message ?: return false

    if (externalCode != null && message.contains(externalCode, ignoreCase = true)) {
      return true
    }
    if (regex != null && regex.matches(message)) {
      return true
    }

    return matcher(throwable)
  }

  fun extractVariable(throwable: Throwable): String? {
    if (variableIdx < 0) return null
    val message = throwable.cause?.message ?: throwable.message ?: return null
    return regex?.find(message)?.groupValues?.get(variableIdx)
  }

  companion object {
    fun match(throwable: Throwable): PrismaConfigErrorType? = entries.firstOrNull { it.matches(throwable) }
  }
}
