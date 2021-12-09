package com.jetbrains.lang.makefile

import com.intellij.lang.*
import com.intellij.psi.*
import com.intellij.psi.tree.*
import com.jetbrains.lang.makefile.psi.*

private val PAIRS = arrayOf(
    BracePair(MakefileTypes.KEYWORD_IFDEF, MakefileTypes.KEYWORD_ENDIF, true),
    BracePair(MakefileTypes.KEYWORD_IFNDEF, MakefileTypes.KEYWORD_ENDIF, true),
    BracePair(MakefileTypes.KEYWORD_IFEQ, MakefileTypes.KEYWORD_ENDIF, true),
    BracePair(MakefileTypes.KEYWORD_IFNEQ, MakefileTypes.KEYWORD_ENDIF, true),
    BracePair(MakefileTypes.KEYWORD_DEFINE, MakefileTypes.KEYWORD_ENDEF, true),
    BracePair(MakefileTypes.OPEN_CURLY, MakefileTypes.CLOSE_CURLY, true),
    BracePair(MakefileTypes.OPEN_PAREN, MakefileTypes.CLOSE_PAREN, true)
)

class MakefileBraceMatcherProvider : PairedBraceMatcher {
  override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int): Int = openingBraceOffset
  override fun getPairs(): Array<BracePair> = PAIRS
  override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean = true
}