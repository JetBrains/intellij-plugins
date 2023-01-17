// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.sfc.lexer

import com.intellij.lang.javascript.JSFlexAdapter
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.JSTokenTypes.STRING_TEMPLATE_PART
import com.intellij.lang.javascript.JavaScriptSupportLoader
import com.intellij.lexer.*
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.xml.XmlTokenType.*
import it.unimi.dsi.fastutil.ints.IntArrayList
import org.jetbrains.astro.lang.sfc.lexer.AstroSfcTokenTypes.Companion.FRONTMATTER_SCRIPT

class AstroSfcLexerImpl(override val project: Project?, private val lexJsFragment: Boolean = false)
  : HtmlLexer(AstroMergingLexer(AstroFlexAdapter(false, false)), true), AstroSfcLexer {

  private var frontmatterScriptLexer: Lexer? = null

  override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
    if (initialState and HAS_NON_RESTARTABLE_STATE != 0) {
      thisLogger().error(IllegalStateException("Do not reset Astro Lexer to a non-restartable state"))
    }
    frontmatterScriptLexer = null
    super.start(buffer, startOffset, endOffset,
                if (initialState == 0 && lexJsFragment) _AstroSfcLexer.EXPRESSION_INITIAL else initialState)
  }

  override fun getState(): Int {
    return super.getState() or (if (frontmatterScriptLexer != null) HAS_NON_RESTARTABLE_STATE else 0)
  }

  override fun isRestartableState(state: Int): Boolean {
    return super.isRestartableState(state)
           && (state and HAS_NON_RESTARTABLE_STATE) == 0
  }

  override fun isHtmlTagState(state: Int): Boolean {
    return state == _AstroSfcLexer.START_TAG_NAME || state == _AstroSfcLexer.END_TAG_NAME
  }

  override fun createTagEmbedmentStartTokenSet(): TokenSet {
    return TokenSet.orSet(super.createTagEmbedmentStartTokenSet(), TAG_TOKENS)
  }

  override fun getStateForRestartDuringEmbedmentScan(): Int {
    return _AstroSfcLexer.HTML_INITIAL
  }

  override fun advance() {
    frontmatterScriptLexer?.let {
      it.advance()
      if (it.tokenType == null) {
        frontmatterScriptLexer = null
      }
    }

    if (frontmatterScriptLexer == null) {
      super.advance()
      if (myDelegate.tokenType === FRONTMATTER_SCRIPT) {
        frontmatterScriptLexer = JSFlexAdapter(JavaScriptSupportLoader.TYPESCRIPT.optionHolder)
          .also {
            it.start(myDelegate.bufferSequence, myDelegate.tokenStart, myDelegate.tokenEnd)
          }
      }
    }
  }

  override fun getTokenType(): IElementType? {
    return frontmatterScriptLexer?.tokenType ?: super.getTokenType()
  }

  override fun getTokenStart(): Int {
    return frontmatterScriptLexer?.tokenStart ?: super.getTokenStart()
  }

  override fun getTokenEnd(): Int {
    return frontmatterScriptLexer?.tokenEnd ?: super.getTokenEnd()
  }

  companion object {
    internal val TAG_TOKENS = TokenSet.create(FRONTMATTER_SCRIPT)

    const val HAS_NON_RESTARTABLE_STATE = 1 shl (BASE_STATE_SHIFT + 3)

  }

  open class AstroFlexAdapter(highlightMode: Boolean, jsDocTypesMode: Boolean)
    : FlexAdapter(_AstroSfcLexer(highlightMode, jsDocTypesMode)) {

    private val flex get() = (super.getFlex() as _AstroSfcLexer)

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
      flex.expressionStack.clear()
      super.start(buffer, startOffset, endOffset, initialState and HAS_NON_RESTARTABLE_STATE.inv())
    }

    override fun getState(): Int {
      return super.getState() + if (flex.isRestartable) 0 else HAS_NON_RESTARTABLE_STATE
    }

    override fun getCurrentPosition(): LexerPosition {
      return AstroFlexAdapterPosition(tokenStart, state, flex.expressionStack.clone())
    }

    override fun restore(position: LexerPosition) {
      flex.expressionStack = (position as AstroFlexAdapterPosition).expressionStack.clone()
      super.start(bufferSequence, position.offset, bufferEnd, position.state and HAS_NON_RESTARTABLE_STATE.inv())
    }

    private class AstroFlexAdapterPosition(private val offset: Int,
                                           private val state: Int,
                                           val expressionStack: IntArrayList) : LexerPosition {
      override fun getOffset(): Int = offset

      override fun getState(): Int = state

    }
  }

  open class AstroMergingLexer(original: FlexAdapter) : MergingLexerAdapterBase(original) {
    override fun getMergeFunction(): MergeFunction {
      return MergeFunction { type, originalLexer -> this.merge(type, originalLexer) }
    }

    protected open fun merge(type: IElementType?, originalLexer: Lexer): IElementType? {
      var result = type
      while (tokenStart == originalLexer.tokenStart
             && originalLexer.tokenType != null) {
        result = originalLexer.tokenType
        originalLexer.advance()
      }
      if (!TOKENS_TO_MERGE.contains(result)) {
        return result
      }
      while (true) {
        val nextTokenType = originalLexer.tokenType
        if (nextTokenType !== result) {
          break
        }
        originalLexer.advance()
      }
      return result
    }

    companion object {

      private val TOKENS_TO_MERGE = TokenSet.create(XML_COMMENT_CHARACTERS, XML_WHITE_SPACE, XML_REAL_WHITE_SPACE,
                                                    XML_ATTRIBUTE_VALUE_TOKEN, XML_DATA_CHARACTERS, XML_TAG_CHARACTERS,
                                                    STRING_TEMPLATE_PART, XML_BAD_CHARACTER, JSTokenTypes.XML_STYLE_COMMENT)

    }
  }
}

