// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web

import com.intellij.codeInsight.navigation.targetPresentation
import com.intellij.ide.util.EditSourceUtil
import com.intellij.model.Pointer
import com.intellij.navigation.EmptyNavigatable
import com.intellij.platform.backend.navigation.NavigationRequest
import com.intellij.platform.backend.navigation.NavigationTarget
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiElement
import com.intellij.refactoring.suggested.createSmartPointer
import org.jetbrains.vuejs.model.source.VueComponents

class VueComponentSourceNavigationTarget(private val myElement: PsiElement) : NavigationTarget {

  override fun createPointer(): Pointer<out NavigationTarget> {
    val elementPointer = myElement.createSmartPointer()
    return Pointer {
      elementPointer.element?.let { VueComponentSourceNavigationTarget(it) }
    }
  }

  override fun navigationRequest(): NavigationRequest? =
    (VueComponents.getComponentDescriptor(myElement)?.source ?: myElement).let {
      it as? Navigatable
      ?: EditSourceUtil.getDescriptor(it)
      ?: EmptyNavigatable.INSTANCE
    }?.navigationRequest()

  override fun computePresentation(): TargetPresentation = targetPresentation(myElement)

  override fun equals(other: Any?): Boolean =
    this === other ||
    other is VueComponentSourceNavigationTarget
    && other.myElement == myElement

  override fun hashCode(): Int =
    myElement.hashCode()
}