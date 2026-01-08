// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.web

import com.intellij.javascript.nodejs.PackageJsonData
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.highlighting.TypeScriptHighlighter
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.documentation.PolySymbolDocumentation
import com.intellij.polySymbols.documentation.PolySymbolDocumentationCustomizer
import com.intellij.polySymbols.html.framework
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.polySymbols.utils.kindName
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.intellij.util.applyIf
import org.angular2.Angular2Framework
import org.angular2.codeInsight.blocks.Angular2BlockParameterPrefixSymbol
import org.angular2.codeInsight.blocks.Angular2BlockParameterSymbol
import org.angular2.codeInsight.blocks.Angular2HtmlBlockSymbol
import org.angular2.codeInsight.documentation.Angular2ElementDocumentationTarget.SyntaxPrinter
import org.angular2.lang.expr.psi.Angular2BlockParameter
import org.angular2.lang.html.highlighting.Angular2HtmlHighlighterColors.Companion.NG_BLOCK_NAME

class Angular2SymbolDocumentationCustomizer : PolySymbolDocumentationCustomizer {
  override fun customize(symbol: PolySymbol, location: PsiElement?, documentation: PolySymbolDocumentation): PolySymbolDocumentation =
    if (location != null) {
      when {
        symbol is Angular2HtmlBlockSymbol -> {
          val primaryBlock = symbol.primaryBlock?.let { SyntaxPrinter(location).append(NG_BLOCK_NAME, "@$it").toString() }
          documentation.withDefinition(
            SyntaxPrinter(location)
              .append(TypeScriptHighlighter.TS_KEYWORD, "block")
              .appendRaw(" ")
              .append(DefaultLanguageHighlighterColors.IDENTIFIER, "@" + symbol.name)
              .toString()
          ).withDescriptionSections(
            if (symbol.isPrimary)
              mapOf("Primary block" to "")
            else if (primaryBlock != null)
              mapOf("Primary block" to primaryBlock)
            else
              emptyMap()
          )
        }
        symbol is Angular2BlockParameterSymbol -> {
          val printer = SyntaxPrinter(location).append(TypeScriptHighlighter.TS_KEYWORD, "parameter")
            .appendRaw(" ")
          location.parentOfType<Angular2BlockParameter>()?.prefix?.let {
            printer.append(DefaultLanguageHighlighterColors.IDENTIFIER, it)
              .appendRaw(" ")
          }
          documentation.withDefinition(
            printer
              .append(DefaultLanguageHighlighterColors.IDENTIFIER, symbol.name)
              .toString()
          )
        }
        symbol is Angular2BlockParameterPrefixSymbol -> {
          documentation.withDefinition(
            SyntaxPrinter(location).append(TypeScriptHighlighter.TS_KEYWORD, "parameter prefix")
              .appendRaw(" ")
              .append(DefaultLanguageHighlighterColors.IDENTIFIER, symbol.name)
              .toString()
          )
        }
        symbol.kind == NG_DEFER_ON_TRIGGERS -> {
          documentation.withDefinition(
            SyntaxPrinter(location).append(TypeScriptHighlighter.TS_KEYWORD, "trigger")
              .appendRaw(" ")
              .append(TypeScriptHighlighter.TS_GLOBAL_FUNCTION, symbol.name)
              .toString()
          )
        }
        else -> {
          documentation
        }
      }
    }
    else {
      documentation
    }
      .applyIf(symbol.framework == Angular2Framework.ID) {
        withAngularLibrary(symbol)
      }

  private fun PolySymbolDocumentation.withAngularLibrary(symbol: PolySymbol): PolySymbolDocumentation {
    val source = if (symbol is PsiSourcedPolySymbol)
      symbol.source
    else
      null
    val psiFile = source?.containingFile
    val virtualFile = psiFile?.virtualFile
    val pkgJson = if (virtualFile != null) PackageJsonUtil.findUpPackageJson(virtualFile) else null
    val data = if (pkgJson != null) PackageJsonData.getOrCreate(pkgJson) else null

    return when {
      data != null -> withLibrary(data.name + (data.version?.toString()?.let { "@$it" } ?: ""))
      symbol.kindName.startsWith("ng-form-") -> withLibrary("@angular/forms")
      symbol is Angular2Symbol -> withLibrary("@angular/core")
      else -> this
    }
  }
}