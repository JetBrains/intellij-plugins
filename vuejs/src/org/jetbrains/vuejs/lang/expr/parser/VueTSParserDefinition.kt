// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.expr.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.lang.javascript.DialectOptionHolder
import com.intellij.lang.javascript.JSFlexAdapter
import com.intellij.lang.javascript.JavascriptParserDefinition
import com.intellij.lang.javascript.parsing.JavaScriptParser
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.lang.javascript.types.JSFileElementType
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.tree.IFileElementType
import org.jetbrains.vuejs.lang.expr.VueTSLanguage

internal val VUETS_FILE: IFileElementType = JSFileElementType.create(VueTSLanguage)

internal class VueTSParserDefinition : JavascriptParserDefinition() {
  object Util {
    @Suppress("UNUSED_PARAMETER")
    fun createLexer(project: Project?): Lexer {
      return JSFlexAdapter(DialectOptionHolder.TS)
    }
  }

  override fun createParser(project: Project?): PsiParser {
    return object : VueExprParsing.PsiParserAdapter() {
      override fun createExprParser(builder: PsiBuilder) = VueTSParser(builder)
    }
  }

  override fun createJSParser(builder: PsiBuilder): JavaScriptParser {
    return VueTSParser(builder)
  }

  override fun createLexer(project: Project?): Lexer {
    return Util.createLexer(project)
  }

  override fun getFileNodeType(): IFileElementType {
    return VUETS_FILE
  }

  override fun createFile(viewProvider: FileViewProvider): JSFile {
    val file = super.createFile(viewProvider) as JSFile
    VueExprParsing.postProcessFile(file)
    return file
  }
}
