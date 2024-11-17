// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.vaadin.endpoints

import com.intellij.codeInsight.AnnotationUtil
import com.intellij.codeInsight.daemon.ImplicitUsageProvider
import com.intellij.lang.jvm.JvmModifier
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.util.InheritanceUtil

internal class VaadinImplicitUsageProvider : ImplicitUsageProvider {
  override fun isImplicitUsage(element: PsiElement): Boolean {
    return element is PsiClass
           && !element.isInterface
           && !element.isEnum
           && !element.hasModifier(JvmModifier.ABSTRACT)
           && !element.isAnnotationType
           && (AnnotationUtil.isAnnotated(element, VAADIN_ROUTE, 0)
               || AnnotationUtil.isAnnotated(element, VAADIN_TAG, 0)
               || InheritanceUtil.isInheritor(element, VAADIN_APP_SHELL_CONFIGURATOR))
  }

  override fun isImplicitRead(element: PsiElement): Boolean {
    return false
  }

  override fun isImplicitWrite(element: PsiElement): Boolean {
    return element is PsiField
           && AnnotationUtil.isAnnotated(element, VAADIN_ID, 0)
  }
}
