package name.kropp.intellij.makefile

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import name.kropp.intellij.makefile.psi.MakefileDefine
import name.kropp.intellij.makefile.psi.MakefileRule
import name.kropp.intellij.makefile.psi.MakefileTypes
import name.kropp.intellij.makefile.psi.MakefileVariableAssignment

class MakefileFoldingBuilder : FoldingBuilderEx(), DumbAware {
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
        if (it.length > 60) {
          it.substring(0, 42) + "..."
        } else {
          it
        }
      }?.trim() ?: ""
    }

    fun PsiElement.trimmedTextRange(): TextRange {
      var last = lastChild
      while (last != null && (last is PsiWhiteSpace || last.node.elementType == MakefileTypes.EOL || last.textRange.isEmpty)) {
        last = last.prevSibling
      }
      return TextRange.create(textRange.startOffset, last.textRange.endOffset )
    }
  }

  class MakefileRuleFoldingDescriptor(private val rule: MakefileRule) : FoldingDescriptor(rule, rule.trimmedTextRange()) {
    override fun getPlaceholderText() = rule.targetLine.targets.text + ":"
  }
  class MakefileVariableFoldingDescriptor(private val variable: MakefileVariableAssignment) : FoldingDescriptor(variable, variable.trimmedTextRange()) {
    override fun getPlaceholderText() = "${variable.variable.text}${variable.assignment?.text ?: "="}${cutValue(variable.value)}"
  }
  class MakefileDefineFoldingDescriptor(private val define: MakefileDefine) : FoldingDescriptor(define, define.trimmedTextRange()) {
    override fun getPlaceholderText() = "${define.variable?.text}${define.assignment?.text ?: "="}${cutValue(define.value)}"
  }
}