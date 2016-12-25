package name.kropp.intellij.makefile

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import name.kropp.intellij.makefile.psi.MakefileDefine
import name.kropp.intellij.makefile.psi.MakefileRule

class MakefileFoldingBuilder : FoldingBuilderEx() {
  override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean) =
      PsiTreeUtil.findChildrenOfAnyType(root, MakefileRule::class.java, MakefileDefine::class.java)
      .mapNotNull {
        when (it) {
          is MakefileRule -> MakefileRuleFoldingDescriptor(it)
          is MakefileDefine -> MakefileDefineFoldingDescriptor(it)
          else -> null
        }
      }.toTypedArray()


  override fun getPlaceholderText(node: ASTNode) = "..."
  override fun isCollapsedByDefault(node: ASTNode) = node.psi is MakefileDefine

  class MakefileRuleFoldingDescriptor(private val rule: MakefileRule) : FoldingDescriptor(rule, rule.textRange) {
    override fun getPlaceholderText() = rule.targetLine.targets.text + ":"
  }
  class MakefileDefineFoldingDescriptor(private val define: MakefileDefine) : FoldingDescriptor(define, define.textRange) {
    override fun getPlaceholderText(): String {
      val value = define.value?.let {
        if (it.length > 50) {
          it.substring(0, 32) + "..."
        } else {
          it
        }
      }?.trim() ?: ""
      return "${define.variable?.text} ${define.assignment?.text ?: "="} $value"
    }
  }
}