// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.expr.parser

import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.lang.javascript.JSFlexAdapter
import com.intellij.lang.javascript.JavascriptParserDefinition
import com.intellij.lang.javascript.parsing.JavaScriptParser
import com.intellij.lang.javascript.settings.JSRootConfiguration
import com.intellij.lang.javascript.types.JSFileElementType
import com.intellij.lexer.Lexer
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.psi.impl.source.resolve.FileContextUtil
import com.intellij.psi.tree.IFileElementType
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser
import org.jetbrains.vuejs.lang.expr.VueJSLanguage

class VueJSParserDefinition : JavascriptParserDefinition() {
  companion object {
    private val FILE: IFileElementType = JSFileElementType.create(VueJSLanguage.INSTANCE)
    private val LOG = Logger.getInstance(VueJSParserDefinition::class.java)

    const val EXPRESSION: String = "expr"
    const val INTERPOLATION: String = "int"

    fun createLexer(project: Project?): Lexer {
      return JSFlexAdapter(JSRootConfiguration.getInstance(project).languageLevel.dialect.optionHolder)
    }

  }

  override fun createParser(project: Project?): PsiParser {
    return PsiParser { root, builder ->
      val containingFile = builder.getUserData(FileContextUtil.CONTAINING_FILE_KEY)
      if (containingFile != null) {
        when (FileUtilRt.getExtension(containingFile.name)) {
          EXPRESSION -> {
            val info = containingFile.name
              .let {
                val lastDot = it.lastIndexOf('.')
                val preLastDot = it.lastIndexOf('.', lastDot - 1)
                if (preLastDot >= 0)
                  it.substring(preLastDot + 1, lastDot)
                    .replace(' ', '.')
                else
                  ""
              }
              .let {
                VueAttributeNameParser.parse(it)
              }
            VueJSParser.parseEmbeddedExpression(builder, root, info)
          }
          "js" -> //special case for creation of AST from text
            VueJSParser.parseJS(builder, root)
          else -> VueJSParser.parseInterpolation(builder, root)
        }
      }
      else {
        LOG.error("No containing file while parsing Vue expression.")
      }
      return@PsiParser builder.treeBuilt
    }
  }

  override fun createJSParser(builder: PsiBuilder): JavaScriptParser<*, *, *, *> {
    return VueJSParser(builder)
  }

  override fun createLexer(project: Project?): Lexer {
    return Companion.createLexer(project)
  }

  override fun getFileNodeType(): IFileElementType {
    return FILE
  }
}
