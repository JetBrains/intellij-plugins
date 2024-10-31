// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.vaadin.endpoints

import com.intellij.codeInsight.highlighting.HighlightedReference
import com.intellij.lang.properties.references.PropertyReference
import com.intellij.microservices.jvm.url.uastUrlPathReferenceInjectorForScheme
import com.intellij.microservices.jvm.url.uastUrlReferenceProvider
import com.intellij.microservices.url.HTTP_SCHEMES
import com.intellij.patterns.PsiJavaPatterns.psiMethod
import com.intellij.patterns.uast.callExpression
import com.intellij.patterns.uast.injectionHostUExpression
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import org.jetbrains.uast.UElement
import org.jetbrains.uast.expressions.UInjectionHost

internal class VaadinReferenceContributor : PsiReferenceContributor() {
  override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
    registrar.registerUastReferenceProvider(
        injectionHostUExpression()
            .annotationParam(VAADIN_ROUTE, "value"),
        uastUrlReferenceProvider(uastUrlPathReferenceInjectorForScheme(HTTP_SCHEMES, vaadinUrlPksParser))
    )

    registrar.registerUastReferenceProvider(
        injectionHostUExpression()
            .callParameter(0, callExpression()
                .withMethodName("getTranslation")
                .withAnyResolvedMethod(psiMethod()
                    .withName("getTranslation")
                    .definedInClass("com.vaadin.flow.component.Component"))),
        object : UastReferenceProvider() {
          override fun getReferencesByElement(element: UElement, context: ProcessingContext): Array<PsiReference> {
            if (element !is UInjectionHost) return PsiReference.EMPTY_ARRAY
            val key = element.evaluateToString() ?: return PsiReference.EMPTY_ARRAY
            val sourcePsi = element.sourcePsi ?: return PsiReference.EMPTY_ARRAY

            return arrayOf(object : PropertyReference(key, sourcePsi, null, false), HighlightedReference {
            })
          }
        }
    )
  }
}