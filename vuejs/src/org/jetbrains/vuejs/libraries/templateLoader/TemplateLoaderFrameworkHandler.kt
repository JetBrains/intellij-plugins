// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.templateLoader

import com.intellij.lang.ASTNode
import com.intellij.lang.ecmascript6.psi.ES6ImportDeclaration
import com.intellij.lang.ecmascript6.psi.ES6ImportedBinding
import com.intellij.lang.javascript.JSElementTypes
import com.intellij.lang.javascript.index.FrameworkIndexingHandler
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.stubs.JSElementIndexingData
import com.intellij.lang.javascript.psi.stubs.impl.JSElementIndexingDataImpl
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.psi.PsiElement
import com.intellij.util.PathUtil
import org.jetbrains.vuejs.codeInsight.es6Unquote
import org.jetbrains.vuejs.index.VueUrlIndex

class TemplateLoaderFrameworkHandler : FrameworkIndexingHandler() {

  override fun shouldCreateStubForCallExpression(node: ASTNode): Boolean {
    val methodExpression = node.firstChildNode
    if (methodExpression.elementType !== JSElementTypes.REFERENCE_EXPRESSION) return false

    val referencedNameElement = methodExpression.firstChildNode
    return WITH_RENDER.equals(referencedNameElement?.text, ignoreCase = true)
  }

  override fun processDecorator(decorator: ES6Decorator, data: JSElementIndexingDataImpl?): JSElementIndexingDataImpl? {
    val decoratorName = decorator.decoratorName
    if (decoratorName?.equals(WITH_RENDER, true) == true) {
      return addImplicitElementForWithRender(decoratorName, decorator, data)
    }
    return data
  }

  override fun processCallExpression(callExpression: JSCallExpression, outData: JSElementIndexingData) {
    val functionName = (callExpression.methodExpression as? JSReferenceExpression)?.referenceName
    if (WITH_RENDER.equals(functionName, ignoreCase = true)) {
      addImplicitElementForWithRender(functionName!!, callExpression, outData as JSElementIndexingDataImpl)
    }
  }

  private fun addImplicitElementForWithRender(name: String,
                                              context: PsiElement,
                                              outData: JSElementIndexingDataImpl?): JSElementIndexingDataImpl? {
    val out = outData ?: JSElementIndexingDataImpl()
    JSStubBasedPsiTreeUtil.resolveLocallyWithMergedResults(name, context)
      .asSequence()
      .filterIsInstance<ES6ImportedBinding>()
      .mapNotNull { (it.parent as? ES6ImportDeclaration)?.fromClause?.referenceText }
      .map { es6Unquote(it).takeWhile { char -> char != '?' } }
      .map { PathUtil.getFileName(it) }
      .find { it.isNotBlank() }
      ?.let {
        out.addImplicitElement(
          JSImplicitElementImpl.Builder(it, context)
            .setUserString(VueUrlIndex.JS_KEY)
            .forbidAstAccess()
            .toImplicitElement()
        )
      }
    return if (out.isEmpty) outData else out
  }

  companion object {
    const val WITH_RENDER = "withrender"
  }

}
