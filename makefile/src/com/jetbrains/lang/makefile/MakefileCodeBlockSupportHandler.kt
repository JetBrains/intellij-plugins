package com.jetbrains.lang.makefile

import com.intellij.codeInsight.highlighting.AbstractCodeBlockSupportHandler
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.jetbrains.lang.makefile.MakefileFoldingBuilder.MakefileConditionalElseFoldingDescriptor
import com.jetbrains.lang.makefile.MakefileFoldingBuilder.MakefileConditionalFoldingDescriptor
import com.jetbrains.lang.makefile.psi.MakefileTypes.*

/**
 * Code block handler (which highlights matching tokens) for constructs like
 *
 * ```
 * ifdef ...
 *   ...
 * else ifeq ...
 *   ...
 * endif
 * ```
 *
 * Pairs of `define ... endef` are handled (in a simpler way) via
 * [MakefileBraceMatcherProvider].
 *
 * Block Folding is handled via [MakefileConditionalFoldingDescriptor] and
 * [MakefileConditionalElseFoldingDescriptor].
 *
 * @see MakefileBraceMatcherProvider
 * @see MakefileConditionalFoldingDescriptor
 * @see MakefileConditionalElseFoldingDescriptor
 */
class MakefileCodeBlockSupportHandler : AbstractCodeBlockSupportHandler() {
  override fun getTopLevelElementTypes(): TokenSet =
    TOP_LEVEL_ELEMENTS

  override fun getKeywordElementTypes(): TokenSet =
    KEYWORD_ELEMENTS

  override fun getBlockElementTypes(): TokenSet =
    BLOCK_ELEMENTS

  override fun getDirectChildrenElementTypes(parentElementType: IElementType?): TokenSet =
    when (parentElementType) {
      null -> TokenSet.EMPTY
      else -> HIERARCHY_MAP[parentElementType] ?: TokenSet.EMPTY
    }

  private companion object {
    private val HIERARCHY_MAP = mapOf(
      CONDITIONAL to TokenSet.create(KEYWORD_IFDEF,
                                     KEYWORD_IFNDEF,
                                     KEYWORD_IFEQ,
                                     KEYWORD_IFNEQ,
                                     CONDITIONAL_ELSE,
                                     KEYWORD_ENDIF),
      CONDITIONAL_ELSE to TokenSet.create(KEYWORD_ELSE,
                                          KEYWORD_IFDEF,
                                          KEYWORD_IFNDEF,
                                          KEYWORD_IFEQ,
                                          KEYWORD_IFNEQ),
    )

    private val TOP_LEVEL_ELEMENTS: TokenSet

    private val KEYWORD_ELEMENTS: TokenSet

    private val BLOCK_ELEMENTS: TokenSet

    init {
      val parents = mutableSetOf<IElementType>()
      val children = mutableSetOf<IElementType>()

      HIERARCHY_MAP.forEach { (parent, immediateChildren) ->
        parents += parent
        children += immediateChildren.types
      }

      TOP_LEVEL_ELEMENTS = TokenSet.create(*(parents - children).toTypedArray())

      KEYWORD_ELEMENTS = TokenSet.create(*(children - parents).toTypedArray())

      BLOCK_ELEMENTS = TokenSet.create(*parents.toTypedArray())
    }
  }
}
