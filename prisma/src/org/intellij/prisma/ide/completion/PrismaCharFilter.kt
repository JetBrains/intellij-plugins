package org.intellij.prisma.ide.completion

import com.intellij.codeInsight.lookup.CharFilter
import com.intellij.codeInsight.lookup.Lookup
import org.intellij.prisma.lang.psi.PrismaFile

class PrismaCharFilter : CharFilter() {
  override fun acceptChar(c: Char, prefixLength: Int, lookup: Lookup): Result? {
    if (lookup.psiFile !is PrismaFile) {
      return null
    }

    if (c == '@') {
      return Result.ADD_TO_PREFIX
    }

    return null
  }
}