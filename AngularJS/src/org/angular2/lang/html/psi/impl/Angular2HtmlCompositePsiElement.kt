// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.lang.html.psi.impl

import com.intellij.psi.impl.source.tree.CompositePsiElement
import com.intellij.psi.tree.IElementType
import org.jetbrains.annotations.NonNls

private const val IMPL_SUFFIX: @NonNls String = "Impl"

abstract class Angular2HtmlCompositePsiElement(type: IElementType) : CompositePsiElement(type) {
  override fun toString(): String {
    return javaClass.simpleName.removeSuffix(IMPL_SUFFIX)
  }
}