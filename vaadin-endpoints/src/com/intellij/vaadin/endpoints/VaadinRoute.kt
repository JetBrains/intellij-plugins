// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.vaadin.endpoints

import com.intellij.psi.PsiAnchor

internal class VaadinRoute(
    val urlMapping: String,
    val locationString: String,
    val anchor: PsiAnchor
) {
  fun isValid(): Boolean = anchor.retrieve() != null
}