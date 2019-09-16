// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex

import com.intellij.lang.ASTNode
import com.intellij.lang.ecmascript6.psi.ES6FunctionProperty
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.index.FrameworkIndexingHandler
import com.intellij.lang.javascript.index.JSSymbolUtil
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.impl.JSCallExpressionImpl
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.lang.javascript.psi.stubs.JSElementIndexingData
import com.intellij.lang.javascript.psi.stubs.JSImplicitElementStructure
import com.intellij.lang.javascript.psi.stubs.impl.JSElementIndexingDataImpl
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.vuejs.index.VueFrameworkHandler
import org.jetbrains.vuejs.index.createImplicitElement
import org.jetbrains.vuejs.libraries.vuex.VuexStoreUtils.VUEX_COMPONENT_FUNCTIONS

class VuexFrameworkHandler : FrameworkIndexingHandler() {

  private val VUEX_INDEXES = mapOf(
    VueFrameworkHandler.record(VuexStoreIndex.KEY)
  )

  override fun shouldCreateStubForCallExpression(node: ASTNode?): Boolean {
    return node
      ?.takeIf { it.elementType === JSElementTypes.CALL_EXPRESSION }
      ?.let { JSCallExpressionImpl.getMethodExpression(it) }
      ?.takeIf {
        it.elementType === JSElementTypes.REFERENCE_EXPRESSION &&
        JSReferenceExpressionImpl.getQualifierNode(it) == null
      }
      ?.let { JSReferenceExpressionImpl.getReferenceName(it) }
      ?.let { VUEX_COMPONENT_FUNCTIONS.contains(it) } == true
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

  override fun processAnyProperty(property: JSProperty, outData: JSElementIndexingData?): JSElementIndexingData? {
    val out = outData ?: JSElementIndexingDataImpl()

    if (VuexStoreUtils.STATE == property.name) {
      val properties = PsiTreeUtil.findChildrenOfType(property, JSProperty::class.java)
      properties
        .filter { it.parent.parent == property && it.name != null }
        .forEach {
          out.addImplicitElement(createImplicitElement(it.name!!, it, VuexStoreIndex.JS_KEY, VuexStoreUtils.STATE))
        }
    }
    else if (VuexStoreUtils.ACTION == property.name || VuexStoreUtils.MUTATION == property.name || VuexStoreUtils.GETTER == property.name) {
      //Actions can be action: function(){} or action(){}
      val es6properties = PsiTreeUtil.findChildrenOfType(property, ES6FunctionProperty::class.java)
      val jsProperties = PsiTreeUtil.findChildrenOfType(property, JSProperty::class.java)

      es6properties
        .filter { it.parent.parent == property }
        .forEach {
          //          For such cases:
          //          var SOME_MUTATION = 'computed name'
          //          mutations = {
          //            [SOME_MUTATION]() {
          //            }
          //          };
          if (it.computedPropertyName != null) {
            val expr = PsiTreeUtil.findChildOfType(it, JSReferenceExpression::class.java)
            if (expr != null && expr.referenceName != null) {
              val reference = JSSymbolUtil.resolveLocallyIncludingDefinitions(expr.referenceName!!, expr)
              val referenceText = PsiTreeUtil.findChildOfType(reference, JSLiteralExpression::class.java)?.value
              if (referenceText != null) out.addImplicitElement(
                createImplicitElement(referenceText.toString(), it, VuexStoreIndex.JS_KEY, property.name))
            }
          }
          if (it.name != null) {
            out.addImplicitElement(createImplicitElement(it.name!!, it, VuexStoreIndex.JS_KEY, property.name))
          }

        }
      jsProperties
        .filter { it.parent.parent == property }
        .forEach {
          if (it.name != null) {
            out.addImplicitElement(createImplicitElement(it.name!!, it, VuexStoreIndex.JS_KEY, property.name))
          }
        }
    }
    return if (out.isEmpty) outData else out
  }

  override fun indexImplicitElement(element: JSImplicitElementStructure, sink: IndexSink?): Boolean {
    val index = VUEX_INDEXES[element.userString]
    if (index != null) {
      sink?.occurrence(index, element.name)
    }
    return false
  }

}
