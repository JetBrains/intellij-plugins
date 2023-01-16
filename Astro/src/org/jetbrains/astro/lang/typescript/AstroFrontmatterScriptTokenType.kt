// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.lang.typescript

import com.intellij.html.embedding.HtmlCustomEmbeddedContentTokenType
import com.intellij.javascript.web.html.XmlASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.javascript.JSFlexAdapter
import com.intellij.lang.javascript.JSLanguageUtil
import com.intellij.lang.javascript.JavaScriptSupportLoader
import com.intellij.lang.javascript.parsing.JavaScriptParserBase
import com.intellij.lexer.Lexer
import com.intellij.psi.PsiElement

object AstroFrontmatterScriptTokenType : HtmlCustomEmbeddedContentTokenType(
  "ASTRO:FRONTMATTER_SCRIPT", JavaScriptSupportLoader.TYPESCRIPT, true) {

  override fun parse(builder: PsiBuilder) {
    val parser = JSLanguageUtil.createJSParser(language, builder)
    builder.putUserData(JavaScriptParserBase.JS_DIALECT_KEY, parser.dialect)
    parser.parseJS(this)
  }

  override fun createLexer(): Lexer =
    JSFlexAdapter(JavaScriptSupportLoader.TYPESCRIPT.optionHolder)

  override fun createPsi(node: ASTNode): PsiElement =
    XmlASTWrapperPsiElement(node)
}