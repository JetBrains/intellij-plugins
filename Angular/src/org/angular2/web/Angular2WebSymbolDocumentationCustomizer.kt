// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.web

import com.intellij.lang.javascript.highlighting.TypeScriptHighlighter
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.documentation.WebSymbolDocumentation
import com.intellij.webSymbols.documentation.WebSymbolDocumentationCustomizer
import com.intellij.webSymbols.utils.qualifiedKind
import org.angular2.codeInsight.blocks.Angular2BlockParameterPrefixSymbol
import org.angular2.codeInsight.blocks.Angular2BlockParameterSymbol
import org.angular2.codeInsight.blocks.Angular2HtmlBlockSymbol
import org.angular2.codeInsight.documentation.Angular2ElementDocumentationTarget.SyntaxPrinter
import org.angular2.lang.expr.psi.Angular2BlockParameter
import org.angular2.lang.html.highlighting.Angular2HtmlHighlighterColors.Companion.NG_BLOCK_NAME

class Angular2WebSymbolDocumentationCustomizer : WebSymbolDocumentationCustomizer {
  override fun customize(symbol: WebSymbol, location: PsiElement?, documentation: WebSymbolDocumentation): WebSymbolDocumentation {
    if (symbol is Angular2HtmlBlockSymbol && location != null) {
      val primaryBlock = symbol.primaryBlock?.let { SyntaxPrinter(location).append(NG_BLOCK_NAME, "@$it").toString() }
      return documentation.with(
        definition = SyntaxPrinter(location)
          .append(TypeScriptHighlighter.TS_KEYWORD, "block")
          .appendRaw(" ")
          .append(DefaultLanguageHighlighterColors.IDENTIFIER, "@" + symbol.name)
          .toString(),
        additionalSections = if (symbol.isPrimary)
          mapOf("Primary block" to "")
        else if (primaryBlock != null)
          mapOf("Primary block" to primaryBlock)
        else
          emptyMap()
      )
    }
    else if (symbol is Angular2BlockParameterSymbol && location != null) {
      val printer = SyntaxPrinter(location).append(TypeScriptHighlighter.TS_KEYWORD, "parameter")
        .appendRaw(" ")
      location.parentOfType<Angular2BlockParameter>()?.prefix?.let {
        printer.append(DefaultLanguageHighlighterColors.IDENTIFIER, it)
          .appendRaw(" ")
      }
      return documentation.with(
        definition = printer
          .append(DefaultLanguageHighlighterColors.IDENTIFIER, symbol.name)
          .toString()
      )
    }
    else if (symbol is Angular2BlockParameterPrefixSymbol && location != null) {
      return documentation.with(
        definition = SyntaxPrinter(location).append(TypeScriptHighlighter.TS_KEYWORD, "parameter prefix")
          .appendRaw(" ")
          .append(DefaultLanguageHighlighterColors.IDENTIFIER, symbol.name)
          .toString()
      )
    }
    else if (symbol.qualifiedKind == NG_DEFER_ON_TRIGGERS && location != null) {
      return documentation.with(
        definition = SyntaxPrinter(location).append(TypeScriptHighlighter.TS_KEYWORD, "trigger")
          .appendRaw(" ")
          .append(TypeScriptHighlighter.TS_GLOBAL_FUNCTION, symbol.name)
          .toString()
      )
    }
    else {
      return documentation
    }
  }
}