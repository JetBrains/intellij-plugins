// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.codeinsight

import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import org.intellij.terraform.hil.HILElementTypes.*

class HILBraceMatcher : PairedBraceMatcher {

  override fun getCodeConstructStart(file: PsiFile, openingBraceOffset: Int): Int {
    return openingBraceOffset
  }

  override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean {
    return true
  }

  companion object {
    private val BRACE_PAIRS = arrayOf(
        BracePair(INTERPOLATION_START, R_CURLY, true),
        BracePair(TEMPLATE_START, R_CURLY, true),
        BracePair(L_CURLY, R_CURLY, true),
        BracePair(L_PAREN, R_PAREN, true),
        BracePair(L_BRACKET, R_BRACKET, true)
    )
  }

  override fun getPairs(): Array<BracePair> {
    return BRACE_PAIRS
  }
}
