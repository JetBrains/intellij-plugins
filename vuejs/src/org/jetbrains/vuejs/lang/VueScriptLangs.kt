// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang

import com.intellij.injected.editor.VirtualFileWindow
import com.intellij.lang.PsiBuilder
import com.intellij.lang.javascript.JSLanguageDialect
import com.intellij.lang.javascript.JSStubElementTypes
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.PsiManagerEx
import com.intellij.psi.tree.IElementType
import org.jetbrains.vuejs.lang.expr.VueJSLanguage
import org.jetbrains.vuejs.lang.expr.VueTSLanguage
import org.jetbrains.vuejs.lang.expr.highlighting.VueJSSyntaxHighlighter
import org.jetbrains.vuejs.lang.expr.highlighting.VueTSSyntaxHighlighter
import org.jetbrains.vuejs.lang.expr.parser.*
import org.jetbrains.vuejs.lang.html.lexer.VueLangModeMarkerElementType
import org.jetbrains.vuejs.lang.html.lexer.VueTagEmbeddedContentProvider
import org.jetbrains.vuejs.lang.html.parser.VueFile

object VueScriptLangs {
  val LANG_MODE = Key.create<LangMode>("LANG_MODE")

  fun createLexer(langMode: LangMode, project: Project?): Lexer {
    if (langMode == LangMode.HAS_TS) {
      return VueTSParserDefinition.createLexer(project)
    }
    else {
      return VueJSParserDefinition.createLexer(project)
    }
  }

  fun createParser(langMode: LangMode, builder: PsiBuilder): VueExprParser {
    return if (langMode == LangMode.HAS_TS) {
      VueTSParser(builder)
    }
    else {
      VueJSParser(builder)
    }
  }

  fun createExprHighlightingLexer(langMode: LangMode): Lexer {
    return if (langMode == LangMode.HAS_TS) {
      VueTSSyntaxHighlighter().highlightingLexer
    }
    else {
      VueJSSyntaxHighlighter().highlightingLexer
    }
  }

  fun getLatestKnownLang(project: Project?, virtualFile: VirtualFile?): LangMode {
    project ?: return LangMode.DEFAULT
    virtualFile ?: return LangMode.DEFAULT
    if (!virtualFile.isValid) return LangMode.DEFAULT
    val file = if (virtualFile is VirtualFileWindow) {
      // InjectionRegistrarImpl.parseFile used in InjectionRegistrarImpl.reparse
      // doesn't set ViewProvider for some reason, and PsiManager#findFile fails because of that
      PsiManagerEx.getInstanceEx(project).fileManager.getCachedPsiFile(virtualFile)
    }
    else {
      PsiManager.getInstance(project).findFile(virtualFile)
    }
    return (file as? VueFile)?.langMode ?: LangMode.DEFAULT
  }

  fun getLatestKnownLang(element: PsiElement): LangMode {
    return (element.containingFile as? VueFile)?.langMode ?: return LangMode.DEFAULT
  }

}

/**
 * Vue files embed JavaScript or TypeScript fragments inside them.
 * We don't want to mix those languages, so we determine LangMode of the whole file.
 * At the same time, there's a possibility of typos & we also support embedding languages that are neither JS nor TS,
 * so the only answer we provide is if there's any TS, or no TS at all.
 * For example, it is still possible to sneak in some other dialect of JavaScript besides TS in the same file, which could be buggy.
 * Enum values are designed to be a little strange in order to make the reader think about the above.
 */
enum class LangMode(val exprLang: JSLanguageDialect, scriptElementType: IElementType, vararg val attrValues: String?) {
    PENDING(VueJSLanguage.INSTANCE, JSStubElementTypes.MOD_ES6_EMBEDDED_CONTENT),
    NO_TS(VueJSLanguage.INSTANCE, JSStubElementTypes.MOD_ES6_EMBEDDED_CONTENT, "js", "javascript", null /* null -> lang attribute is missing */),
    HAS_TS(VueTSLanguage.INSTANCE, JSStubElementTypes.MOD_TS_EMBEDDED_CONTENT, "ts", "typescript");

  val canonicalAttrValue: String get() = if (this == HAS_TS) "ts" else "js"

  val scriptEmbedmentInfo = VueTagEmbeddedContentProvider.VueScriptEmbedmentInfo(scriptElementType)

  val astMarkerToken = VueLangModeMarkerElementType(this)

  companion object {
    val DEFAULT = NO_TS

    val knownAttrValues: Set<String?>

    init {
      val attrValues = values().flatMap { it.attrValues.toList() }
      knownAttrValues = attrValues.toSet()
      assert(attrValues.size == knownAttrValues.size) { "more than one enum value claimed the same attr value" }
    }

    private val reverseMap = values().flatMap { enumValue -> enumValue.attrValues.map { attrValue -> attrValue to enumValue } }.toMap()

    fun fromAttrValue(attrValue: String?): LangMode {
      return reverseMap.getOrDefault(attrValue, NO_TS)
    }
  }
}
