package org.intellij.prisma.ide.editor

import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.findParentOfType
import com.intellij.psi.util.startOffset
import org.intellij.prisma.lang.psi.PrismaDeclaration
import org.intellij.prisma.lang.psi.PrismaElementTypes

class PrismaBraceMatcher : PairedBraceMatcher {
  override fun getPairs(): Array<BracePair> = BRACES

  override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean = true

  override fun getCodeConstructStart(file: PsiFile, openingBraceOffset: Int): Int {
    return file.findElementAt(openingBraceOffset)?.findParentOfType<PrismaDeclaration>()?.startOffset
           ?: return openingBraceOffset
  }

}

private val BRACES = arrayOf(
  BracePair(PrismaElementTypes.LBRACE, PrismaElementTypes.RBRACE, true),
  BracePair(PrismaElementTypes.LBRACKET, PrismaElementTypes.RBRACKET, false),
  BracePair(PrismaElementTypes.LPAREN, PrismaElementTypes.RPAREN, false),
)