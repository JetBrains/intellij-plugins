package name.kropp.intellij.makefile

import com.intellij.codeInsight.lookup.*
import com.intellij.psi.*
import name.kropp.intellij.makefile.psi.*

class MakefileTargetReference(private val prerequisite: MakefilePrerequisite) : PsiPolyVariantReferenceBase<MakefilePrerequisite>(prerequisite, false) {
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

  val rule: MakefileRule?
    get() = prerequisite.parent.parent.parent.parent as? MakefileRule

  override fun getVariants()
      = (prerequisite.containingFile as MakefileFile).targets.filterNot { it.isPatternTarget || rule?.targets?.any { t -> t.name == it.name } == true }.distinctBy { it.name }.map {
    LookupElementBuilder.create(it).withIcon(MakefileTargetIcon)
  }.toTypedArray()

  override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
    val match = Regex("""\$\((.*)\)""").find(prerequisite.text)
    if (match != null) {
      val name = match.groups[1]!!.value
      return (prerequisite.containingFile as MakefileFile).variables
          .filter { it.text == name }
          .map(::PsiElementResolveResult)
          .toTypedArray()
    }
    return (prerequisite.containingFile as MakefileFile).allTargets
        .filter { it.matches(prerequisite.text) }
        .map(::PsiElementResolveResult)
        .toTypedArray()
  }
}