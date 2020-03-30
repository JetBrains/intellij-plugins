package name.kropp.intellij.makefile

import com.intellij.codeInsight.lookup.*
import com.intellij.lang.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import name.kropp.intellij.makefile.psi.*

class MakefileVariableReference(private val usage: MakefileVariableUsage) : PsiReference {
  override fun getElement() = usage
  override fun getRangeInElement(): TextRange {
    val startOffset = nameNode.startOffset - usage.node.startOffset
    return TextRange.create(startOffset, startOffset + nameNode.textLength)
  }
  override fun bindToElement(element: PsiElement): PsiElement? = null

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

  override fun getCanonicalText() = nameNode.text

  override fun handleElementRename(newName: String): PsiElement {
    val newNameNode = MakefileElementFactory.createChars(usage.project, newName)
    usage.node.replaceChild(nameNode, newNameNode)
    return usage
  }

  override fun isSoft() = true

  override fun getVariants()
      = (usage.containingFile as MakefileFile).variables.distinctBy { it.text }.map {
    LookupElementBuilder.create(it)
  }.toTypedArray()

  override fun resolve(): PsiElement? {
    return (usage.containingFile as MakefileFile).variables
        .filter { it.text == nameNode.text }
        .map(::PsiElementResolveResult)
        .firstOrNull()?.element
  }
}