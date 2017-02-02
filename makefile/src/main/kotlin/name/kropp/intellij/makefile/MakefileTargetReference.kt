package name.kropp.intellij.makefile

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiReference
import name.kropp.intellij.makefile.psi.MakefileElementFactory
import name.kropp.intellij.makefile.psi.MakefilePrerequisite
import name.kropp.intellij.makefile.psi.MakefileTarget
import name.kropp.intellij.makefile.psi.MakefileVariable

class MakefileTargetReference(private val prerequisite: MakefilePrerequisite) : PsiReference {
  override fun getElement() = prerequisite
  override fun getRangeInElement() = TextRange.create(0, element.textLength)
  override fun bindToElement(element: PsiElement): PsiElement? = null

  override fun isReferenceTo(element: PsiElement?): Boolean {
    if (element is MakefileTarget) {
      return element.matches(prerequisite.text)
    }
    if (element is MakefileVariable) {
      return "\$(${element.text})" == prerequisite.text
    }
    return false
  }

  override fun getCanonicalText() = prerequisite.text ?: ""

  override fun handleElementRename(newName: String): PsiElement {
    if (newName.contains("%")) {
      return prerequisite
    }
    val identifierNode = prerequisite.node.firstChildNode
    if (identifierNode != null) {
      val target = MakefileElementFactory.createTarget(prerequisite.project, newName)
      val newIdentifierNode = target.firstChild.node
      prerequisite.node.replaceChild(identifierNode, newIdentifierNode)
    }
    return prerequisite
  }

  override fun isSoft() = false

  override fun getVariants()
      = (prerequisite.containingFile as MakefileFile).targets.map {
    LookupElementBuilder.create(it).withIcon(MakefileTargetIcon)
  }.toTypedArray()

  override fun resolve(): PsiElement? {
    val match = Regex("\\\$\\((.*)\\)").find(prerequisite.text)
    if (match != null) {
      val name = match.groups[1]!!.value
      return (prerequisite.containingFile as MakefileFile).variables
          .filter { it.text == name }
          .map(::PsiElementResolveResult)
          .firstOrNull()?.element
    }
    return (prerequisite.containingFile as MakefileFile).allTargets
        .filter { it.matches(prerequisite.text) }
        .map(::PsiElementResolveResult)
        .firstOrNull()?.element
  }
}