package com.jetbrains.lang.makefile

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import com.jetbrains.lang.makefile.psi.MakefileTypes
import com.jetbrains.lang.makefile.psi.MakefileVariable
import com.jetbrains.lang.makefile.psi.MakefileVariableUsage

class MakefileVariableReference(private val usage: MakefileVariableUsage) : PsiPolyVariantReferenceBase<MakefileVariableUsage>(usage, false) {
  override fun getRangeInElement(): TextRange {
    val startOffset = nameNode.startOffset - usage.node.startOffset
    return TextRange.create(startOffset, startOffset + nameNode.textLength)
  }

  private val nameNode: ASTNode
    get() {
      val second = usage.firstChild.nextSibling.node
      return if (second.elementType == MakefileTypes.OPEN_CURLY || second.elementType == MakefileTypes.OPEN_PAREN) {
        usage.firstChild.nextSibling.nextSibling.node
      } else {
        second
      }
    }

  override fun isReferenceTo(element: PsiElement): Boolean {
    if (element is MakefileVariable) {
      return element.text == nameNode.text
    }
    return false
  }

  override fun getVariants()
      = (usage.containingFile as MakefileFile).variables.distinctBy { it.text }.map {
    LookupElementBuilder.create(it)
  }.toTypedArray()

  override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
    return (usage.containingFile as MakefileFile).variables
        .filter { it.text == nameNode.text }
        .map(::PsiElementResolveResult)
        .toTypedArray()
  }
}