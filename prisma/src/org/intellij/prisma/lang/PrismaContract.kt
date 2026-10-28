// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.lang

import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import org.intellij.prisma.lang.psi.PrismaFile

/**
 * The same directive as `isPrismaNextSchema` in `@prisma/language-server`.
 */
private val CONTRACT_DIRECTIVE = Regex("""\s*// *use +prisma-(?:next|8) *(?!\S)""")

/**
 * Returns `true` when [text] starts with the `// use prisma-8` or the `// use prisma-next` directive.
 * Only blank text can come before the directive.
 */
fun isPrismaContractText(text: CharSequence): Boolean = CONTRACT_DIRECTIVE.matchesAt(text, 0)

/**
 * `true` for a Prisma 8 contract.
 * The Prisma 7 semantics of the plugin do not apply to a contract.
 */
val PsiFile.isPrismaContract: Boolean
  get() {
    if (this !is PrismaFile) return false
    return CachedValuesManager.getCachedValue(this) {
      CachedValueProvider.Result.create(isPrismaContractText(viewProvider.contents), this)
    }
  }
