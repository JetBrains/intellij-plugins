// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web

import com.intellij.codeInsight.navigation.targetPresentation
import com.intellij.ide.util.EditSourceUtil
import com.intellij.model.Pointer
import com.intellij.navigation.EmptyNavigatable
import com.intellij.navigation.NavigationRequest
import com.intellij.navigation.NavigationTarget
import com.intellij.navigation.TargetPresentation
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

  override fun presentation(): TargetPresentation = targetPresentation(myElement)

  override fun equals(other: Any?): Boolean =
    this === other ||
    other is VueComponentSourceNavigationTarget
    && other.myElement == myElement

  override fun hashCode(): Int =
    myElement.hashCode()
}