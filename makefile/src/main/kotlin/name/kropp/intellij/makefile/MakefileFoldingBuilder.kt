package name.kropp.intellij.makefile

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import name.kropp.intellij.makefile.psi.MakefileDefine
import name.kropp.intellij.makefile.psi.MakefileRule
import name.kropp.intellij.makefile.psi.MakefileVariableAssignment

class MakefileFoldingBuilder : FoldingBuilderEx() {
  override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean) =
      PsiTreeUtil.findChildrenOfAnyType(root, MakefileRule::class.java, MakefileVariableAssignment::class.java, MakefileDefine::class.java)
      .mapNotNull {
        when (it) {
          is MakefileRule -> MakefileRuleFoldingDescriptor(it)
          is MakefileVariableAssignment -> MakefileVariableFoldingDescriptor(it)
          is MakefileDefine -> MakefileDefineFoldingDescriptor(it)
          else -> null
        }
      }.toTypedArray()


  override fun getPlaceholderText(node: ASTNode) = "..."
  override fun isCollapsedByDefault(node: ASTNode) = node.psi is MakefileDefine

  companion object {
    fun cutValue(value: String?): String {
      return value?.let {
        if (it.length > 50) {
          it.substring(0, 32) + "..."
        } else {
          it
        }
      }?.trim() ?: ""
    }
  }

  class MakefileRuleFoldingDescriptor(private val rule: MakefileRule) : FoldingDescriptor(rule, rule.textRange) {
    override fun getPlaceholderText() = rule.targetLine.targets.text + ":"
  }
  class MakefileVariableFoldingDescriptor(private val variable: MakefileVariableAssignment) : FoldingDescriptor(variable, variable.textRange) {
    override fun getPlaceholderText() = "${variable.variable.text} ${variable.assignment?.text ?: "="} ${cutValue(variable.value)}"
  }
  class MakefileDefineFoldingDescriptor(private val define: MakefileDefine) : FoldingDescriptor(define, define.textRange) {
    override fun getPlaceholderText() = "${define.variable?.text} ${define.assignment?.text ?: "="} ${cutValue(define.value)}"
  }
}