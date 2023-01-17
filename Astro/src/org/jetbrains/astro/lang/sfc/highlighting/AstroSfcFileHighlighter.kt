// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.astro.lang.sfc.highlighting

import com.intellij.ide.highlighter.HtmlFileHighlighter
import com.intellij.lang.javascript.DialectOptionHolder
import com.intellij.lang.javascript.highlighting.JSHighlighter
import com.intellij.lang.javascript.highlighting.TypeScriptHighlighter
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.XmlHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.FileTypeRegistry
import com.intellij.openapi.util.Pair
import com.intellij.psi.tree.IElementType
import com.intellij.util.ArrayUtil
import com.intellij.util.containers.map2Array
import org.jetbrains.astro.lang.sfc.AstroSfcLanguage
import org.jetbrains.astro.lang.sfc.lexer.AstroSfcTokenTypes
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set

internal class AstroSfcFileHighlighter : JSHighlighter(AstroSfcLanguage.INSTANCE.optionHolder) {

  private val htmlHighlighter = HtmlFileHighlighter()

  override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
    val actualTokenType = (tokenType as? AstroFrontmatterHighlighterToken)?.original
                          ?: tokenType
    var result = keys[actualTokenType]
    if (result != null) {
      return result
    }
    result = htmlHighlighter.getTokenHighlights(actualTokenType)
    if (tokenType is AstroFrontmatterHighlighterToken) {
      result = ArrayUtil.insert(result, 1, AstroSfcHighlighterColors.ASTRO_FRONTMATTER)
    }
    return mapToTsKeys(result, tokenType)
  }

  override fun getHighlightingLexer(): Lexer {
    return AstroSfcHighlightingLexer(FileTypeRegistry.getInstance().findFileTypeByName("CSS"))
  }

  companion object {
    private val keys: MutableMap<IElementType, Array<TextAttributesKey>> = HashMap()
    private val ourJsHighlighter = JSHighlighter(DialectOptionHolder.JS_1_5)
    private val ourTsHighlighter = TypeScriptHighlighter(false)
    private val ourTsKeyMap: MutableMap<Pair<TextAttributesKey, IElementType>, TextAttributesKey> = ConcurrentHashMap()
    private fun put(token: IElementType, vararg keysArr: TextAttributesKey) {
      keys[token] = keysArr.toList().toTypedArray()
    }

    init {
      put(AstroSfcTokenTypes.FRONTMATTER_SEPARATOR, XmlHighlighterColors.HTML_CODE, AstroSfcHighlighterColors.ASTRO_FRONTMATTER,
          AstroSfcHighlighterColors.ASTRO_FRONTMATTER_SEPARATOR)
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