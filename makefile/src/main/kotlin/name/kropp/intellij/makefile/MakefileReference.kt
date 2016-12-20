package name.kropp.intellij.makefile

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import name.kropp.intellij.makefile.psi.MakefileElementFactory
import name.kropp.intellij.makefile.psi.MakefilePrerequisite
import name.kropp.intellij.makefile.psi.MakefileTarget

class MakefileReference(private val dependency: MakefilePrerequisite) : PsiReference {
  override fun getElement() = dependency
  override fun getRangeInElement() = TextRange.create(0, element.textLength)
  override fun bindToElement(element: PsiElement): PsiElement? = null

  override fun isReferenceTo(element: PsiElement?): Boolean {
    if (element is MakefileTarget) {
      return element.name == dependency.text
    }
    return false
  }

  override fun getCanonicalText() = dependency.text ?: ""

  override fun handleElementRename(newName: String): PsiElement {
    val identifierNode = dependency.node.firstChildNode
    if (identifierNode != null) {
      val target = MakefileElementFactory.createTarget(dependency.project, newName)
      val newIdentifierNode = target.firstChild.node
      dependency.node.replaceChild(identifierNode, newIdentifierNode)
    }
    return dependency
  }

  override fun isSoft() = false

  override fun getVariants()
      = (dependency.containingFile as MakefileFile).targets.map {
    LookupElementBuilder.create(it).withIcon(AllIcons.Toolwindows.ToolWindowRun)
  }.toTypedArray()

  override fun resolve()
      = (dependency.containingFile as MakefileFile).targets
      .filter { it.name == dependency.text }
      .map(::PsiElementResolveResult)
      .firstOrNull()?.element
}