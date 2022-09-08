// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.model

import com.intellij.psi.PsiElement

interface VueSourceElement {

  val source: PsiElement?

  @Suppress("DEPRECATION")
  @JvmDefault
  val rawSource: PsiElement?
    get() = source

}