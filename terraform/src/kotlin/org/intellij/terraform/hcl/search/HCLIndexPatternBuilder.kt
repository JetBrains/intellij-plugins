// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.search

import com.intellij.lexer.Lexer
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.search.IndexPatternBuilder
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import org.intellij.terraform.hcl.HCLElementTypes
import org.intellij.terraform.hcl.HCLParserDefinition
import org.intellij.terraform.hcl.HCLTokenTypes
import org.intellij.terraform.hcl.psi.HCLFile

class HCLIndexPatternBuilder : IndexPatternBuilder {
  override fun getIndexingLexer(file: PsiFile): Lexer? {
    return if (file is HCLFile) {
      HCLParserDefinition.createLexer()
    } else null
  }

  override fun getCommentTokenSet(file: PsiFile): TokenSet? {
    return if (file is HCLFile) {
      HCLTokenTypes.HCL_COMMENTARIES
    } else null
  }

  override fun getCommentStartDelta(tokenType: IElementType): Int {
    return when (tokenType) {
      HCLElementTypes.LINE_C_COMMENT -> 2
      HCLElementTypes.LINE_HASH_COMMENT -> 1
      HCLElementTypes.BLOCK_COMMENT -> 2
      else -> 0
    }
  }

  override fun getCommentEndDelta(tokenType: IElementType): Int {
    return when (tokenType) {
      HCLElementTypes.BLOCK_COMMENT -> 2
      else -> 0
    }
  }
}