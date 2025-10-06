// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import com.intellij.codeInsight.controlflow.ControlFlow
import com.intellij.lang.ecmascript6.psi.impl.ES6FieldStatementImpl
import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.JavaScriptSpecificHandlersFactory
import com.intellij.lang.javascript.ecmascript6.TypeScriptQualifiedItemProcessor
import com.intellij.lang.javascript.findUsages.JSDialectSpecificReadWriteAccessDetector
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.JSVarStatement
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl
import com.intellij.lang.javascript.psi.resolve.*
import com.intellij.lang.javascript.psi.types.guard.TypeScriptTypeGuard
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil
import com.intellij.lang.javascript.refactoring.JSVisibilityUtil
import com.intellij.lang.typescript.resolve.TypeScriptTypeHelper
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.impl.source.resolve.ResolveCache.PolyVariantResolver
import com.intellij.util.asSafely
import org.angular2.codeInsight.config.Angular2Compiler.isStrictNullInputTypes
import org.angular2.codeInsight.controlflow.Angular2ControlFlowBuilder
import org.angular2.codeInsight.refs.Angular2ReferenceExpressionResolver
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.entities.source.Angular2SourceUtil
import org.angular2.findUsages.Angular2ReadWriteAccessDetector
import org.angular2.lang.expr.psi.Angular2Binding
import org.angular2.lang.expr.psi.Angular2TemplateBinding
import org.angular2.lang.html.psi.Angular2HtmlBoundAttribute

class Angular2SpecificHandlersFactory : JavaScriptSpecificHandlersFactory() {
  override fun createReferenceExpressionResolver(
    referenceExpression: JSReferenceExpressionImpl, ignorePerformanceLimits: Boolean,
  ): PolyVariantResolver<JSReferenceExpressionImpl> {
    return Angular2ReferenceExpressionResolver(referenceExpression, ignorePerformanceLimits)
  }

  override fun getControlFlow(scope: JSControlFlowScope): ControlFlow {
    return Angular2ControlFlowBuilder().buildControlFlow(scope)
  }

  override fun <T : ResultSink> createQualifiedItemProcessor(sink: T, place: PsiElement): QualifiedItemProcessor<T> {
    val clazz: PsiElement? = Angular2EntitiesProvider.findTemplateComponent(place)?.jsResolveScope
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

  override fun newExpectedTypeEvaluator(
    parent: PsiElement,
    expectedTypeKind: JSExpectedTypeKind,
  ): ExpectedTypeEvaluator {
    return TypeScriptExpectedTypeEvaluator(parent, expectedTypeKind)
  }

  override fun getExportScope(element: PsiElement): JSElement? =
    if (element is PsiFile)
      null
    else
      (Angular2EntitiesProvider.findTemplateComponent(element)?.jsExportScope as? JSElement)
      ?: super.getExportScope(element)

  override fun resolveLimited(reference: PsiPolyVariantReference, referenceName: String): PsiElement? {
    return super.resolveLimited(reference, referenceName)
           ?: Angular2EntitiesProvider.findTemplateComponent(reference.element)?.jsResolveScope?.let {
             JSStubBasedPsiTreeUtil.resolveLocally(referenceName, it, false)
           }
  }

  override fun strictNullChecks(context: PsiElement): Boolean {
    if ((context.context.let { it is Angular2Binding || it is Angular2TemplateBinding }
         || context is Angular2HtmlBoundAttribute)
        && !isStrictNullInputTypes(context)
    ) {
      return false
    }
    return TypeScriptConfigUtil.getConfigForPsiFile(context.getContainingFile())?.strictNullChecks() == true
  }

  override fun isAccessible(subject: PsiElement, accessType: JSAttributeList.AccessType, subjectClass: JSClass?, from: PsiElement, options: JSVisibilityUtil.Options): Boolean =
    super.isAccessible(subject, accessType, subjectClass, from, options)
    || (accessType == JSAttributeList.AccessType.PROTECTED && isProtectedMemberAccessibleFromTemplate(subject, from))

  private fun isProtectedMemberAccessibleFromTemplate(subject: PsiElement, from: PsiElement): Boolean {
    val clazz = subject.context.let { it.asSafely<JSClass>() ?: it.asSafely<JSVarStatement>()?.parent?.asSafely<JSClass>()}
                ?: return false
    return Angular2SourceUtil.findComponentClasses(from).any { it == clazz }
  }

}