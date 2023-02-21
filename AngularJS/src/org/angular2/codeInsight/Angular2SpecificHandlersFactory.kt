// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import com.intellij.codeInsight.controlflow.ControlFlow
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.JavaScriptSpecificHandlersFactory
import com.intellij.lang.javascript.ecmascript6.TypeScriptQualifiedItemProcessor
import com.intellij.lang.javascript.findUsages.JSDialectSpecificReadWriteAccessDetector
import com.intellij.lang.javascript.psi.JSControlFlowScope
import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.lang.javascript.psi.resolve.*
import com.intellij.lang.javascript.psi.types.guard.TypeScriptTypeGuard
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.lang.javascript.psi.util.JSStubBasedScopeHandler
import com.intellij.lang.typescript.resolve.TypeScriptTypeHelper
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.impl.source.resolve.ResolveCache.PolyVariantResolver
import org.angular2.codeInsight.controlflow.Angular2ControlFlowBuilder
import org.angular2.codeInsight.refs.Angular2ReferenceExpressionResolver
import org.angular2.entities.Angular2ComponentLocator
import org.angular2.findUsages.Angular2ReadWriteAccessDetector

class Angular2SpecificHandlersFactory : JavaScriptSpecificHandlersFactory() {
  override fun createReferenceExpressionResolver(
    referenceExpression: JSReferenceExpressionImpl, ignorePerformanceLimits: Boolean): PolyVariantResolver<JSReferenceExpressionImpl> {
    return Angular2ReferenceExpressionResolver(referenceExpression, ignorePerformanceLimits)
  }

  override fun getControlFlow(scope: JSControlFlowScope): ControlFlow {
    return Angular2ControlFlowBuilder().buildControlFlow(scope)
  }

  override fun <T : ResultSink> createQualifiedItemProcessor(sink: T, place: PsiElement): QualifiedItemProcessor<T> {
    val clazz: JSClass? = Angular2ComponentLocator.findComponentClass(place)
    return if (clazz != null && DialectDetector.isTypeScript(clazz)) {
      TypeScriptQualifiedItemProcessor(sink, place.containingFile)
    }
    else super.createQualifiedItemProcessor(sink, place)
  }

  override fun getImportHandler(): JSImportHandler {
    return Angular2ImportHandler()
  }

  override fun newTypeEvaluator(context: JSEvaluateContext): JSTypeEvaluator {
    return Angular2TypeEvaluator(context)
  }

  override fun createAccessibilityProcessingHandler(place: PsiElement?, skipNsResolving: Boolean): AccessibilityProcessingHandler {
    return Angular2AccessibilityProcessingHandler(place)
  }

  override fun getReadWriteAccessDetector(): JSDialectSpecificReadWriteAccessDetector {
    return Angular2ReadWriteAccessDetector
  }

  override fun getTypeGuardEvaluator(): JSTypeGuardEvaluator {
    return Angular2TypeGuardEvaluator
  }

  override fun createTypeGuard(element: PsiElement): TypeScriptTypeGuard {
    return Angular2TypeGuard(element)
  }

  override fun getTypeHelper(): JSTypeHelper {
    return TypeScriptTypeHelper.getInstance()
  }

  override fun getExportScope(element: PsiElement): JSElement? =
    if (element is PsiFile)
      null
    else
      Angular2ComponentLocator.findComponentClass(element)
        ?.containingFile as? JSFile
      ?: super.getExportScope(element)

  override fun resolveReferenceLocallyForAnalysis(reference: PsiPolyVariantReference, referenceName: String): PsiElement? {
    return super.resolveReferenceLocallyForAnalysis(reference, referenceName)
           ?: Angular2ComponentLocator.findComponentClass(reference.element)?.let {
             JSStubBasedPsiTreeUtil.resolveLocally(referenceName, it, false)
           }
  }

}