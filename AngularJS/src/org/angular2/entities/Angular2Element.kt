// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities

import com.intellij.psi.PsiElement

interface Angular2Element {

  val sourceElement: PsiElement

  val navigableElement: PsiElement
    get() = sourceElement
}
