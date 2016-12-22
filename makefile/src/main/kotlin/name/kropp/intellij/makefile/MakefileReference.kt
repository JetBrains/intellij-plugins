package name.kropp.intellij.makefile

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import name.kropp.intellij.makefile.psi.MakefileElementFactory
import name.kropp.intellij.makefile.psi.MakefilePrerequisite
import name.kropp.intellij.makefile.psi.MakefileTarget

class MakefileReference(private val prerequisite: MakefilePrerequisite) : PsiReference {
  override fun getElement() = prerequisite
  override fun getRangeInElement() = TextRange.create(0, element.textLength)
  override fun bindToElement(element: PsiElement): PsiElement? = null

  override fun isReferenceTo(element: PsiElement?): Boolean {
    if (element is MakefileTarget) {
      return element.name == prerequisite.text
    }
    return false
  }

  override fun getCanonicalText() = prerequisite.text ?: ""

  override fun handleElementRename(newName: String): PsiElement {
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
    LookupElementBuilder.create(it).withIcon(AllIcons.Toolwindows.ToolWindowRun)
  }.toTypedArray()

  override fun resolve()
      = (prerequisite.containingFile as MakefileFile).targets
      .filter { it.name == prerequisite.text }
      .map(::PsiElementResolveResult)
      .firstOrNull()?.element
}