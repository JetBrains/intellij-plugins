// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.findUsages

import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import com.intellij.psi.tree.TokenSet
import org.intellij.terraform.hcl.HCLElementTypes
import org.intellij.terraform.hcl.HCLLexer
import org.intellij.terraform.hcl.HCLTokenTypes

class HCLWordsScanner(lexer: HCLLexer) : DefaultWordsScanner(lexer, TokenSet.create(HCLElementTypes.IDENTIFIER), HCLTokenTypes.HCL_COMMENTARIES, HCLTokenTypes.HCL_LITERALS) {
  init {
    setMayHaveFileRefsInLiterals(true)
  }
}
