// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.expr.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.lang.javascript.psi.types.guard.markAsCfgAwareInjectedFile
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.psi.impl.source.resolve.FileContextUtil
import com.intellij.psi.tree.IElementType
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser

object VueExprParsing {
  const val EXPRESSION: String = "expr"
  const val INTERPOLATION: String = "int"

  fun postProcessFile(file: JSFile) {
    if (file.name.endsWith(INTERPOLATION)) {
      file.markAsCfgAwareInjectedFile()
    }
  }

  abstract class PsiParserAdapter : PsiParser {
    abstract fun createExprParser(builder: PsiBuilder): VueExprParser

    override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
      val containingFile = builder.getUserData(FileContextUtil.CONTAINING_FILE_KEY)
      if (containingFile != null) {
        when (FileUtilRt.getExtension(containingFile.name)) {
          EXPRESSION -> {
            val info = containingFile.name
              .let { fileName ->
                val lastDot = fileName.lastIndexOf('.')
                val preLastDot = fileName.lastIndexOf('.', lastDot - 1)
                if (preLastDot >= 0)
                  fileName.substring(preLastDot + 1, lastDot)
                    .replace(' ', '.')
                else
                  ""
              }
              .let {
                VueAttributeNameParser.parse(it)
              }
            createExprParser(builder).parseEmbeddedExpression(root, info)
          }
          "js" -> {
            //special case for creation of AST from the text
            createExprParser(builder).parseJS(root)
          }
          else -> {
            createExprParser(builder).parseInterpolation(root)
          }
        }
      }
      else {
        thisLogger().error("No containing file while parsing Vue expression.")
      }
      return builder.treeBuilt

    }
  }

  interface VueExprParser {
    fun parseEmbeddedExpression(root: IElementType, attributeInfo: VueAttributeNameParser.VueAttributeInfo?)

    fun parseInterpolation(root: IElementType) {
      parseEmbeddedExpression(root, null)
    }

    fun parseJS(root: IElementType)
  }
}