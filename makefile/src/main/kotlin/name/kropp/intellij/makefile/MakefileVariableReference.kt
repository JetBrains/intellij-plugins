package name.kropp.intellij.makefile

import com.intellij.codeInsight.lookup.*
import com.intellij.lang.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import name.kropp.intellij.makefile.psi.*

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

  override fun handleElementRename(newName: String): PsiElement {
    val newNameNode = MakefileElementFactory.createChars(usage.project, newName)
    usage.node.replaceChild(nameNode, newNameNode)
    return usage
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