// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang

import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.JSLanguageDialect
import com.intellij.psi.tree.IElementType
import org.jetbrains.vuejs.lang.expr.VueJSLanguage
import org.jetbrains.vuejs.lang.expr.VueTSLanguage
import org.jetbrains.vuejs.lang.html.lexer.VueLangModeMarkerElementType
import org.jetbrains.vuejs.lang.html.lexer.VueScriptEmbedmentInfo

/**
 * Vue files embed JavaScript or TypeScript fragments inside them.
 * We don't want to mix those languages, so we determine LangMode of the whole file.
 * At the same time, there's a possibility of typos & we also support embedding languages that are neither JS nor TS,
 * so the only answer we provide is if there's any TS, or no TS at all.
 * For example, it is still possible to sneak in some other dialect of JavaScript besides TS in the same file, which could be buggy.
 * Enum values are designed to be a little strange in order to make the reader think about the above.
 */
enum class LangMode(val exprLang: JSLanguageDialect, scriptElementType: IElementType, vararg val attrValues: String?) {
  PENDING(VueJSLanguage, JSElementTypes.MOD_ES6_EMBEDDED_CONTENT),
  NO_TS(
      VueJSLanguage, JSElementTypes.MOD_ES6_EMBEDDED_CONTENT, "js", "javascript",
        null /* null -> lang attribute is missing */),
  HAS_TS(VueTSLanguage, JSElementTypes.MOD_TS_EMBEDDED_CONTENT, "ts", "typescript");

  val canonicalAttrValue: String get() = if (this == HAS_TS) "ts" else "js"

  // can be internal after usages moved to common module WEB-76545
  val scriptEmbedmentInfo: VueScriptEmbedmentInfo = VueScriptEmbedmentInfo(scriptElementType)

  // can be internal after usages moved to common module WEB-76545
  val astMarkerToken: VueLangModeMarkerElementType = VueLangModeMarkerElementType(this)

  companion object {
    val DEFAULT: LangMode = NO_TS

    val knownAttrValues: Set<String?>

    init {
      val attrValues = entries.flatMap { it.attrValues.toList() }
      knownAttrValues = attrValues.toSet()
      assert(attrValues.size == knownAttrValues.size) { "more than one enum value claimed the same attr value" }
    }

    private val reverseMap = entries.flatMap { enumValue -> enumValue.attrValues.map { attrValue -> attrValue to enumValue } }.toMap()

    fun fromAttrValue(attrValue: String?): LangMode {
      return reverseMap.getOrDefault(attrValue, NO_TS)
    }
  }
}