package com.jetbrains.lang.makefile

import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import com.jetbrains.lang.makefile.psi.MakefileTypes

private val PAIRS = arrayOf(
    BracePair(MakefileTypes.KEYWORD_DEFINE, MakefileTypes.KEYWORD_ENDEF, true),
    BracePair(MakefileTypes.OPEN_CURLY, MakefileTypes.CLOSE_CURLY, true),
    BracePair(MakefileTypes.OPEN_PAREN, MakefileTypes.CLOSE_PAREN, true)
)

/**
 * More complex cases, like `ifdef/ifndef/ifeq/ifneq ... else ... endif` are
 * handled via [MakefileCodeBlockSupportHandler].
 *
 * @see MakefileCodeBlockSupportHandler
 */
class MakefileBraceMatcherProvider : PairedBraceMatcher {
  override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int): Int = openingBraceOffset
  override fun getPairs(): Array<BracePair> = PAIRS
  override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean = true
}
