package com.jetbrains.lang.makefile

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.lang.folding.FoldingDescriptor.EMPTY
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.lang.makefile.psi.*
import com.jetbrains.lang.makefile.psi.MakefileTypes.*

class MakefileFoldingBuilder : FoldingBuilderEx(), DumbAware {
  override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
    if (root !is MakefileFile) {
      return EMPTY
    }

    return PsiTreeUtil.findChildrenOfAnyType(root,
                                             MakefileRule::class.java,
                                             MakefileVariableAssignment::class.java,
                                             MakefileDefine::class.java,
                                             MakefileConditional::class.java,
                                             MakefileConditionalElse::class.java)
      .mapNotNull {
        when (it) {
          is MakefileRule -> MakefileRuleFoldingDescriptor(it)
          is MakefileVariableAssignment -> MakefileVariableFoldingDescriptor(it)
          is MakefileDefine -> MakefileDefineFoldingDescriptor(it)
          is MakefileConditional -> {
            val range = it.withoutFirstNode()
            if (!range.isEmpty) MakefileConditionalFoldingDescriptor(it, range) else null
          }
          is MakefileConditionalElse -> {
            /*-
             * A conditional else may be followed by either:
             *
             * - the next conditional else, or
             * - the `endif` keyword (should be folded along with the block).
             */
            val range = it.withoutFirstNodeWithNextSiblingOfType(siblingType = KEYWORD_ENDIF)
            when {
              range.isEmpty -> null
              else -> MakefileConditionalElseFoldingDescriptor(it, range)
            }
          }
          else -> null
        }
      }.toTypedArray()
  }


  override fun getPlaceholderText(node: ASTNode): @NlsSafe String =
    DEFAULT_PLACEHOLDER_TEXT

  override fun isCollapsedByDefault(node: ASTNode) = node.psi is MakefileDefine

  companion object {
    @NlsSafe
    private const val DEFAULT_PLACEHOLDER_TEXT = "..."

    fun cutValue(value: String?): String {
      return value?.let {
        if (it.length > 60) {
          it.substring(0, 42) + DEFAULT_PLACEHOLDER_TEXT
        } else {
          it
        }
      }?.trim() ?: ""
    }

    fun PsiElement.trimmedTextRange() = TextRange.create(textRange.startOffset, textRange.startOffset + text.indexOfLast { !it.isWhitespace() } + 1)

    /**
     * @see withoutFirstNodeWithNextSiblingOfType
     */
    fun PsiElement.withoutFirstNode(): TextRange {
      val startOffset = firstChild?.nextNonWhiteSpaceSibling()?.textRange?.startOffset ?: textRange.endOffset
      return TextRange.create(startOffset, textRange.endOffset)
    }

    /**
     * @param siblingType the expected type of the next sibling. If it matches
     *   the actual type of the next sibling, the sibling is included in the
     *   range returned.
     * @see withoutFirstNode
     */
    fun PsiElement.withoutFirstNodeWithNextSiblingOfType(siblingType: IElementType): TextRange {
      val nextSibling = nextNonWhiteSpaceSibling()

      return when {
        nextSibling == null -> withoutFirstNode()

        nextSibling.node.elementType == siblingType -> {
          /*
           * Conditional else:
           *
           * - the 1st child is always `else`;
           * - the 2nd child may be a COMMENT, en EOL, or any of ifdef/ifndef/ifeq/ifneq.
           */
          val startOffset = firstChild?.nextNonWhiteSpaceSibling()?.textRange?.startOffset
                            ?: textRange.endOffset

          /*
           * Include the next sibling in the range returned.
           */
          val endOffset = nextSibling.textRange.endOffset
          TextRange.create(startOffset, endOffset)
        }

        else -> withoutFirstNode()
      }
    }

    private tailrec fun PsiElement.nextNonWhiteSpaceSibling(): PsiElement? = if (nextSibling !is PsiWhiteSpace) nextSibling else nextSibling?.nextNonWhiteSpaceSibling()
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

  /**
   * Handles the folding of `ifeq/ifneq/ifdef/ifndef ... endif` pairs.
   *
   * @see MakefileConditionalElseFoldingDescriptor
   * @see MakefileCodeBlockSupportHandler
   */
  class MakefileConditionalFoldingDescriptor(private val conditional: MakefileConditional, range: TextRange) : FoldingDescriptor(conditional, range) {
    override fun getPlaceholderText() = cutValue(conditional.condition?.text)
  }

  /**
   * Handles the folding of `else ... endif` pairs.
   *
   * @see MakefileConditionalFoldingDescriptor
   * @see MakefileCodeBlockSupportHandler
   */
  class MakefileConditionalElseFoldingDescriptor(private val conditionalElse: MakefileConditionalElse,
                                                 range: TextRange)
    : FoldingDescriptor(conditionalElse, range) {

    /**
     * @return the short value of either the condition (if available), or the
     *   enclosed block (otherwise).
     */
    override fun getPlaceholderText(): String =
      when (val condition = conditionalElse.condition?.text) {
        /*
         * else <block> endif
         */
        null -> {
          val placeholderText = cutValue(conditionalElse.block?.text)
          when {
            /*
             * The body of the `else` clause consists of whitespace and/or
             * comments, return "else ..." in this case.
             */
            placeholderText.isEmpty() -> " $DEFAULT_PLACEHOLDER_TEXT"

            /*
             * Return "else <descriptor>" instead of "else<descriptor>".
             */
            else -> " $placeholderText"
          }
        }

        /*
         * else (ifdef|ifndef|ifeq|ifneq) <condition> <block> endif
         */
        else -> {
          /*
           * The 1st child is always `else`.
           * The 2nd child is either of ifdef/ifndef/ifeq/ifneq.
           */
          val secondChild = conditionalElse.firstChild?.nextNonWhiteSpaceSibling()

          /*
           * Use "(ifdef|ifndef|ifeq|ifneq) <condition>" and abbreviate it.
           */
          val placeholderText = when {
            secondChild != null && secondChild.node.elementType in setOf(KEYWORD_IFDEF,
                                                                         KEYWORD_IFNDEF,
                                                                         KEYWORD_IFEQ,
                                                                         KEYWORD_IFNEQ) -> secondChild.text + ' ' + condition

            /*
             * Unlikely.
             */
            else -> condition
          }.let(::cutValue)

          /*-
           * Since a conditional else may be followed by another conditional else,
           * append a space to the end of the descriptor.
           *
           * In other words, return
           *
           * "else <descriptor> else ... endif"
           *
           * instead of
           *
           * "else <descriptor>else ... endif".
           */
          when {
            placeholderText.endsWith(' ') -> placeholderText
            else -> "$placeholderText "
          }
        }
      }
  }
}
