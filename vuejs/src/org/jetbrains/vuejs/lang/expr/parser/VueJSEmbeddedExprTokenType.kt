// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.expr.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.tree.ILeafElementType
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser.VueAttributeInfo
import org.jetbrains.vuejs.lang.VueEmbeddedContentTokenType
import org.jetbrains.vuejs.lang.expr.VueJSLanguage

class VueJSEmbeddedExprTokenType private constructor(debugName: String,
                                                     private val attributeInfo: VueAttributeInfo?,
                                                     private val project: Project?)
  : VueEmbeddedContentTokenType(debugName, VueJSLanguage.INSTANCE, false), ILeafElementType {

  companion object {
    fun createEmbeddedExpression(attributeInfo: VueAttributeInfo,
                                 project: Project?): VueJSEmbeddedExprTokenType {
      return VueJSEmbeddedExprTokenType("VUE_JS:EMBEDDED_EXPR", attributeInfo, project)
    }

    fun createInterpolationExpression(project: Project?): VueJSEmbeddedExprTokenType {
      return VueJSEmbeddedExprTokenType("VUE_JS:INTERPOLATION_EXPR", null, project)
    }
  }

  override fun createLexer(): Lexer {
    return VueJSParserDefinition.createLexer(project)
  }

  override fun parse(builder: PsiBuilder) {
    VueJSParser(builder).parseEmbeddedExpression(this, attributeInfo)
  }

  override fun hashCode(): Int {
    var result = attributeInfo.hashCode()
    result = 31 * result + project.hashCode()
    return result
  }

  override fun createLeafNode(leafText: CharSequence): ASTNode = LeafPsiElement(this, leafText)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    other as VueJSEmbeddedExprTokenType
    return attributeInfo == other.attributeInfo
           && project == other.project
  }
}
