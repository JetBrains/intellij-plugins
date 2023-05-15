package com.intellij.vaadin.endpoints

import com.intellij.psi.PsiAnchor

internal class VaadinRoute(
    val urlMapping: String,
    val locationString: String,
    val anchor: PsiAnchor
) {
  fun isValid(): Boolean = anchor.retrieve() != null
}