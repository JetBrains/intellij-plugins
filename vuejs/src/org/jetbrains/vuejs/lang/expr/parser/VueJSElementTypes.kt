// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.expr.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.lang.javascript.types.JSExpressionElementType
import com.intellij.lang.javascript.types.JSParameterElementType
import com.intellij.lang.javascript.types.JSVariableElementType
import com.intellij.psi.tree.ICompositeElementType
import com.intellij.psi.tree.IElementType
import org.jetbrains.annotations.NonNls
import org.jetbrains.vuejs.lang.expr.VueJSLanguage
import org.jetbrains.vuejs.lang.expr.VueTSLanguage
import org.jetbrains.vuejs.lang.expr.psi.impl.*

object VueJSElementTypes {
  const val EXTERNAL_ID_PREFIX: String = "VUE-JS:"

  val V_FOR_VARIABLE: JSVariableElementType = VueJSVForVariableElementType()

  val SLOT_PROPS_PARAMETER: JSParameterElementType = VueJSSlotPropsParameterElementType()

  val SCRIPT_SETUP_TYPE_PARAMETER_LIST: VueJSScriptSetupTypeParameterListElementType = VueJSScriptSetupTypeParameterListElementType()

  val EMBEDDED_EXPR_CONTENT_JS: VueJSEmbeddedExpressionContentElementType = VueJSEmbeddedExpressionContentElementType(
    "EMBEDDED_EXPR_CONTENT_JS", VueJSLanguage)

  val EMBEDDED_EXPR_CONTENT_TS: VueJSEmbeddedExpressionContentElementType = VueJSEmbeddedExpressionContentElementType(
    "EMBEDDED_EXPR_CONTENT_TS", VueTSLanguage)

  val FILTER_ARGUMENTS_LIST: IElementType = VueJSExpressionElementType(
    "FILTER_ARGUMENTS_LIST", ::VueJSFilterArgumentsListImpl)

  val FILTER_REFERENCE_EXPRESSION: IElementType = VueJSExpressionElementType(
    "FILTER_REFERENCE_EXPRESSION", ::VueJSFilterReferenceExpressionImpl)

  val FILTER_LEFT_SIDE_ARGUMENT: IElementType = VueJSExpressionElementType(
    "FILTER_LEFT_SIDE_ARGUMENT", ::VueJSFilterLeftSideArgumentImpl)

  val FILTER_EXPRESSION: IElementType = VueJSExpressionElementType(
    "FILTER_EXPRESSION", ::VueJSFilterExpressionImpl)

  val V_FOR_EXPRESSION: IElementType = VueJSExpressionElementType(
    "V_FOR_EXPRESSION", ::VueJSVForExpressionImpl)

  val SLOT_PROPS_EXPRESSION: IElementType = VueJSExpressionElementType(
    "SLOT_PROPS_EXPRESSION", ::VueJSSlotPropsExpressionImpl)

  private abstract class VueJSElementType(
    @NonNls debugName: String,
    language: Language,
    private val myClassConstructor: (VueJSElementType) -> ASTNode,
  ) : IElementType(debugName, language),
      ICompositeElementType {
    final override fun createCompositeNode(): ASTNode = myClassConstructor(this)
  }

  private class VueJSExpressionElementType(
    @NonNls debugName: String,
    classConstructor: (VueJSElementType) -> ASTNode,
  ) : VueJSElementType(debugName, VueJSLanguage.INSTANCE, classConstructor),
      JSExpressionElementType


}

