// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.types.JSAnyType
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.model.VueComponent
import org.jetbrains.vuejs.model.VueEntitiesContainer
import org.jetbrains.vuejs.model.getDefaultVueComponentInstanceType

class VueUnresolvedComponent(private val context: PsiElement) : VueComponent {

  override val defaultName: String? = null
  override val source: PsiElement? = null
  override val parents: List<VueEntitiesContainer> = emptyList()

  override val thisType: JSType
    get() = getDefaultVueComponentInstanceType(context) ?: JSAnyType.get(context, false)

}
