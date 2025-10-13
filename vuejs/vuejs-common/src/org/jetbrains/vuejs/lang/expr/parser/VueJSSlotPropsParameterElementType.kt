// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.expr.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.javascript.types.JSParameterElementType
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.lang.expr.psi.impl.VueJSSlotPropsParameterImpl

class VueJSSlotPropsParameterElementType : JSParameterElementType("SLOT_PROPS_PARAMETER") {
  override fun construct(node: ASTNode): PsiElement = VueJSSlotPropsParameterImpl(node)
}