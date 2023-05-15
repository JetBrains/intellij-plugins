// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.findUsages

import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import com.intellij.psi.tree.TokenSet
import org.intellij.terraform.hil.HILElementTypes
import org.intellij.terraform.hil.HILTokenTypes
import org.intellij.terraform.hil.psi.HILLexer

class HILWordsScanner(lexer: HILLexer) : DefaultWordsScanner(lexer, TokenSet.create(HILElementTypes.ID), TokenSet.EMPTY, HILTokenTypes.TIL_LITERALS) {
  init {
    setMayHaveFileRefsInLiterals(true)
  }
}
