// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.expr.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lexer.Lexer
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.tree.ILeafElementType
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.vuejs.codeInsight.attributes.VueAttributeNameParser.VueAttributeInfo
import org.jetbrains.vuejs.lang.LangMode
import org.jetbrains.vuejs.lang.VueEmbeddedContentTokenType
import org.jetbrains.vuejs.lang.VueScriptLangs

class VueJSEmbeddedExprTokenType private constructor(debugName: String,
                                                     private val attributeInfo: VueAttributeInfo?,
                                                     val langMode: LangMode,
                                                     private val project: Project?)
  : VueEmbeddedContentTokenType(debugName, langMode.exprLang, false), ILeafElementType {

  fun copyWithLanguage(langMode: LangMode): VueJSEmbeddedExprTokenType {
    val base = this
    return VueJSEmbeddedExprTokenType(makeDebugName(base.debugName, langMode), base.attributeInfo, langMode, base.project)
  }

  companion object {
    fun createEmbeddedExpression(attributeInfo: VueAttributeInfo, langMode: LangMode, project: Project?): VueJSEmbeddedExprTokenType {
      return VueJSEmbeddedExprTokenType(makeDebugName("VUE_JS:EMBEDDED_EXPR", langMode), attributeInfo, langMode, project)
    }

    fun createInterpolationExpression(langMode: LangMode, project: Project?): VueJSEmbeddedExprTokenType {
      return VueJSEmbeddedExprTokenType(makeDebugName("VUE_JS:INTERPOLATION_EXPR", langMode), null, langMode, project)
    }

    private fun makeDebugName(prefix: String, langMode: LangMode): String {
      val suffix = if (langMode == LangMode.PENDING) "" else "_${langMode.exprLang.id}"
      return prefix + suffix
    }

    @Deprecated(message = "please use overload that accepts LangMode of context")
    @ApiStatus.ScheduledForRemoval
    fun createEmbeddedExpression(attributeInfo: VueAttributeInfo, project: Project?): VueJSEmbeddedExprTokenType {
      thisLogger().warn("dysfunctional createEmbeddedExpression used. Please update the plugins relying on Vue plugin")
      return createEmbeddedExpression(attributeInfo, LangMode.DEFAULT, project)
    }

    @Deprecated(message = "please use overload that accepts LangMode of context")
    @ApiStatus.ScheduledForRemoval
    fun createInterpolationExpression(project: Project?): VueJSEmbeddedExprTokenType {
      thisLogger().warn("dysfunctional createInterpolationExpression used. Please update the plugins relying on Vue plugin")
      return createInterpolationExpression(LangMode.DEFAULT, project)
    }
  }

  override fun createLexer(): Lexer {
    return VueScriptLangs.createLexer(langMode, project)
  }

  override fun parse(builder: PsiBuilder) {
    VueScriptLangs.createParser(langMode, builder).parseEmbeddedExpression(this, attributeInfo)
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
