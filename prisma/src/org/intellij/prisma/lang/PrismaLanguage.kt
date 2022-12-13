package org.intellij.prisma.lang

import com.intellij.lang.Language

object PrismaLanguage : Language("Prisma") {
  override fun getDisplayName(): String = "Prisma"
}