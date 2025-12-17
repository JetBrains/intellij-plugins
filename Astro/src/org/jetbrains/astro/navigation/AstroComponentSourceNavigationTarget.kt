// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.navigation

import com.intellij.codeInsight.navigation.targetPresentation
import com.intellij.ide.util.EditSourceUtil
import com.intellij.model.Pointer
import com.intellij.navigation.EmptyNavigatable
import com.intellij.platform.backend.navigation.NavigationRequest
import com.intellij.platform.backend.navigation.NavigationTarget
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer

internal class AstroComponentSourceNavigationTarget(private val element: PsiElement) : NavigationTarget {
  override fun createPointer(): Pointer<out NavigationTarget> {
    val ptr = element.createSmartPointer()
    return Pointer { ptr.element?.let { AstroComponentSourceNavigationTarget(it) } }
  }

  override fun navigationRequest(): NavigationRequest? =
    (element as? Navigatable
     ?: EditSourceUtil.getDescriptor(element)
     ?: EmptyNavigatable.INSTANCE)
      .navigationRequest()

  override fun computePresentation(): TargetPresentation = targetPresentation(element)

  override fun equals(other: Any?): Boolean =
    this === other || other is AstroComponentSourceNavigationTarget && other.element == element

  override fun hashCode(): Int = element.hashCode()
}
