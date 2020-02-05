package name.kropp.intellij.makefile

import com.intellij.lang.*
import com.intellij.lang.folding.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import name.kropp.intellij.makefile.psi.*

class MakefileFoldingBuilder : FoldingBuilderEx(), DumbAware {
  override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean) =
      PsiTreeUtil.findChildrenOfAnyType(root,
          MakefileRule::class.java,
          MakefileVariableAssignment::class.java,
          MakefileDefine::class.java,
          MakefileConditional::class.java)
      .mapNotNull {
        when (it) {
          is MakefileRule -> MakefileRuleFoldingDescriptor(it)
          is MakefileVariableAssignment -> MakefileVariableFoldingDescriptor(it)
          is MakefileDefine -> MakefileDefineFoldingDescriptor(it)
          is MakefileConditional -> MakefileConditionalFoldingDescriptor(it)
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

    fun PsiElement.trimmedTextRange() = TextRange.create(textRange.startOffset, textRange.startOffset + text.indexOfLast { !it.isWhitespace() } + 1)
    fun PsiElement.withoutFirstNode() = TextRange.create(firstChild.nextNonWhiteSpaceSibling().textRange.startOffset, textRange.endOffset)
    private tailrec fun PsiElement.nextNonWhiteSpaceSibling(): PsiElement = if (nextSibling !is PsiWhiteSpace) nextSibling else nextSibling.nextNonWhiteSpaceSibling()
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
  class MakefileConditionalFoldingDescriptor(private val conditional: MakefileConditional) : FoldingDescriptor(conditional, conditional.withoutFirstNode()) {
    override fun getPlaceholderText() = cutValue(conditional.condition?.text)
  }
}