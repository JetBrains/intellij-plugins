package name.kropp.intellij.makefile

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import name.kropp.intellij.makefile.psi.MakefileRule

class MakefileFoldingBuilder : FoldingBuilderEx() {
  override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean) =
      PsiTreeUtil.findChildrenOfType(root, MakefileRule::class.java)
          .map(::MakefileFoldingDescriptor)
          .toTypedArray()

  override fun getPlaceholderText(node: ASTNode) = "..."
  override fun isCollapsedByDefault(node: ASTNode) = false

  class MakefileFoldingDescriptor(private val rule: MakefileRule) : FoldingDescriptor(rule, rule.textRange) {
    override fun getPlaceholderText(): String? = rule.targetLine.targetName + ":"
  }
}