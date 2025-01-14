// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.highlighting

import com.intellij.ide.highlighter.HtmlFileHighlighter
import com.intellij.lang.javascript.DialectOptionHolder
import com.intellij.lang.javascript.highlighting.JSHighlighter
import com.intellij.lang.javascript.highlighting.TypeScriptHighlighter
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.XmlHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.Pair
import com.intellij.psi.tree.IElementType
import com.intellij.util.containers.map2Array
import org.jetbrains.astro.lang.AstroLanguage
import org.jetbrains.astro.lang.lexer.AstroLexer
import org.jetbrains.astro.lang.lexer.AstroTokenTypes
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set

internal class AstroFileHighlighter : JSHighlighter(AstroLanguage.INSTANCE.optionHolder) {

  private val htmlHighlighter = HtmlFileHighlighter()

  override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
    return keys[tokenType] ?: mapToTsKeys(htmlHighlighter.getTokenHighlights(tokenType), tokenType)
  }

  override fun getHighlightingLexer(): Lexer {
    return AstroLexer(null, true, false)
  }

  companion object {
    private val keys: MutableMap<IElementType, Array<TextAttributesKey>> = HashMap()
    private val ourJsHighlighter = JSHighlighter(DialectOptionHolder.JS_1_5)
    private val ourTsHighlighter = TypeScriptHighlighter()
    private val ourTsKeyMap: MutableMap<Pair<TextAttributesKey, IElementType>, TextAttributesKey> = ConcurrentHashMap()

    private fun put(token: IElementType, vararg keysArr: TextAttributesKey) {
      keys[token] = keysArr.toList().toTypedArray()
    }

    init {
      put(AstroTokenTypes.FRONTMATTER_SEPARATOR, XmlHighlighterColors.HTML_CODE, AstroHighlighterColors.ASTRO_FRONTMATTER_SEPARATOR)
    }

    private fun mapToTsKeys(tokenHighlights: Array<TextAttributesKey>, tokenType: IElementType): Array<TextAttributesKey> {
      return tokenHighlights.map2Array { key -> getTsMappedKey(key, tokenType) }
    }

    private fun getTsMappedKey(key: TextAttributesKey, tokenType: IElementType): TextAttributesKey {
      return if (!key.externalName.startsWith("JS."))
        key
      else
        ourTsKeyMap.computeIfAbsent(Pair(key, tokenType)) { p: Pair<TextAttributesKey, IElementType> ->
          val jsHighlights = ourJsHighlighter.getTokenHighlights(p.second)
          val tsHighlights = ourTsHighlighter.getTokenHighlights(p.second)
          val jsKey = jsHighlights.lastOrNull()
          val tsKey = tsHighlights.lastOrNull()
          if (jsKey === p.first && tsKey != null) tsKey else p.first
        }
    }
  }
}