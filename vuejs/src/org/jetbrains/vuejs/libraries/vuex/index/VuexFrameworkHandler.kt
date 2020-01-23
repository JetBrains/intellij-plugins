// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex.index

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.index.FrameworkIndexingHandler
import com.intellij.lang.javascript.index.JSSymbolUtil
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSNewExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.impl.JSCallExpressionImpl
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.lang.javascript.psi.stubs.JSElementIndexingData
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.stubs.JSImplicitElementStructure
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl
import com.intellij.psi.stubs.IndexSink
import com.intellij.util.castSafelyTo
import org.jetbrains.vuejs.index.VueFrameworkHandler
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.REGISTER_MODULE
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.STORE
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.VUEX_MAPPERS
import org.jetbrains.vuejs.libraries.vuex.VuexUtils.VUEX_NAMESPACE

class VuexFrameworkHandler : FrameworkIndexingHandler() {

  private val VUEX_INDEXES = mapOf(
    VueFrameworkHandler.record(VuexStoreIndex.KEY)
  )

  override fun shouldCreateStubForCallExpression(node: ASTNode?): Boolean {
    if (node?.elementType === JSElementTypes.CALL_EXPRESSION) {
      val reference = node?.let { JSCallExpressionImpl.getMethodExpression(it) }
                        ?.takeIf { it.elementType === JSElementTypes.REFERENCE_EXPRESSION }
                      ?: return false
      val refName = JSReferenceExpressionImpl.getReferenceName(reference) ?: return false
      if (JSReferenceExpressionImpl.getQualifierNode(reference) == null) {
        return VUEX_MAPPERS.contains(refName)
      } else {
        return refName == REGISTER_MODULE
      }
    }
    else {
      // new Vuex.Store call
      return node
        ?.takeIf { it.elementType === JSElementTypes.NEW_EXPRESSION }
        ?.let { JSCallExpressionImpl.getMethodExpression(it) }
        ?.takeIf { it.elementType === JSElementTypes.REFERENCE_EXPRESSION }
        ?.let { reference ->
          JSReferenceExpressionImpl.getQualifierNode(reference)
            ?.let { JSReferenceExpressionImpl.getReferenceName(it) } == VUEX_NAMESPACE
          && JSReferenceExpressionImpl.getReferenceName(reference) == STORE
        } == true
    }
  }

  override fun shouldCreateStubForLiteral(node: ASTNode?): Boolean {
    var callExpr = node
    var withinInitializer = false
    while (callExpr?.treeParent != null
           && callExpr.treeParent.elementType != JSElementTypes.CALL_EXPRESSION) {
      callExpr = callExpr.treeParent
      withinInitializer = withinInitializer
                          || callExpr.elementType === JSElementTypes.ARRAY_LITERAL_EXPRESSION
                          || callExpr.elementType === JSElementTypes.OBJECT_LITERAL_EXPRESSION
    }
    return callExpr != null && withinInitializer && shouldCreateStubForCallExpression(callExpr)
  }

  override fun processCallExpression(callExpression: JSCallExpression, outData: JSElementIndexingData) {
    val reference = callExpression.methodExpression
      ?.castSafelyTo<JSReferenceExpression>()
    if (callExpression is JSNewExpression
        && JSSymbolUtil.isAccurateReferenceExpressionName(reference, VUEX_NAMESPACE, STORE)) {
      outData.addImplicitElement(
        JSImplicitElementImpl.Builder(STORE, callExpression)
          .setUserString(VuexStoreIndex.JS_KEY)
          .setType(JSImplicitElement.Type.Variable)
          .forbidAstAccess()
          .toImplicitElement())
    } else if (reference?.referenceName == REGISTER_MODULE) {
      outData.addImplicitElement(
        JSImplicitElementImpl.Builder(REGISTER_MODULE, callExpression)
          .setUserString(VuexStoreIndex.JS_KEY)
          .setType(JSImplicitElement.Type.Variable)
          .forbidAstAccess()
          .toImplicitElement())
    }
  }

  override fun indexImplicitElement(element: JSImplicitElementStructure, sink: IndexSink?): Boolean {
    val index = VUEX_INDEXES[element.userString]
    if (index != null) {
      sink?.occurrence(index, element.name)
    }
    return false
  }

}
