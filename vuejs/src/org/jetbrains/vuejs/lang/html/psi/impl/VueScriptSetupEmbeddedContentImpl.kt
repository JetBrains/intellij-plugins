// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.html.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSExportScopeProvider
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeParameterList
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeParameterListOwner
import com.intellij.lang.javascript.psi.impl.JSEmbeddedContentImpl
import com.intellij.lang.javascript.psi.stubs.JSEmbeddedContentStub
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.util.contextOfType
import com.intellij.psi.util.parents
import com.intellij.psi.xml.XmlTag
import org.jetbrains.vuejs.codeInsight.GENERIC_ATTRIBUTE_NAME
import org.jetbrains.vuejs.codeInsight.SETUP_ATTRIBUTE_NAME
import org.jetbrains.vuejs.codeInsight.findJSExpression
import org.jetbrains.vuejs.codeInsight.stubSafeGetAttribute
import org.jetbrains.vuejs.codeInsight.findVueJSEmbeddedExpressionContent
import org.jetbrains.vuejs.index.findModule
import org.jetbrains.vuejs.index.isScriptSetupTag
import org.jetbrains.vuejs.lang.expr.parser.VueJSStubElementTypes
import org.jetbrains.vuejs.lang.expr.psi.VueJSScriptSetupExpression

class VueScriptSetupEmbeddedContentImpl : JSEmbeddedContentImpl, TypeScriptTypeParameterListOwner, JSExportScopeProvider {

  constructor(node: ASTNode?) : super(node)

  constructor(stub: JSEmbeddedContentStub, type: IStubElementType<*, *>) : super(stub, type)

  override fun processDeclarations(processor: PsiScopeProcessor, state: ResolveState, lastParent: PsiElement?, place: PsiElement): Boolean =
    super.processDeclarations(processor, state, lastParent, place)
    && listOfNotNull(
      findScriptSetupTypeParameterList(this)?.takeIf { paramList -> place.parents(true).none { it == paramList } },
      findScriptSetupExpression(this),
      findModule(place, false)
    )
      .all { it.processDeclarations(processor, state, lastParent, place) }

  override fun getContextExportScope(): JSElement? =
    findModule(this, false)

  override fun getTypeParameterList(): TypeScriptTypeParameterList? =
    findScriptSetupTypeParameterList(this)

  private fun findScriptSetupExpression(place: PsiElement): VueJSScriptSetupExpression? =
    place.contextOfType<XmlTag>()
      ?.takeIf { it.isScriptSetupTag() }
      ?.stubSafeGetAttribute(SETUP_ATTRIBUTE_NAME)
      ?.valueElement
      ?.findJSExpression<VueJSScriptSetupExpression>()

  companion object {
    fun findScriptSetupTypeParameterList(place: PsiElement): TypeScriptTypeParameterList? =
      place.contextOfType<XmlTag>()
        ?.takeIf { it.isScriptSetupTag() }
        ?.stubSafeGetAttribute(GENERIC_ATTRIBUTE_NAME)
        ?.let {
          val stub = (it as? StubBasedPsiElement<*>)?.stub
          if (stub != null) {
            stub.findChildStubByType(VueJSStubElementTypes.EMBEDDED_EXPR_CONTENT_TS)
              ?.findChildStubByType(VueJSStubElementTypes.SCRIPT_SETUP_TYPE_PARAMETER_LIST)
              ?.psi
          }
          else {
            it.valueElement
              ?.findVueJSEmbeddedExpressionContent()
              ?.firstChild as? TypeScriptTypeParameterList
          }
        }
  }

}