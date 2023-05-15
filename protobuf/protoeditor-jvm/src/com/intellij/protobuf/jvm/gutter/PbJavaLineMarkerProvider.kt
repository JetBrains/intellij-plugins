package com.intellij.protobuf.jvm.gutter

import com.intellij.lang.java.JavaLanguage
import com.intellij.protobuf.ide.gutter.PbLanguageSpecificLineMarkerProvider
import com.intellij.psi.PsiElement

internal class PbJavaLineMarkerProvider: PbLanguageSpecificLineMarkerProvider() {
  override fun isAcceptableElement(element: PsiElement): Boolean {
    return element.language == JavaLanguage.INSTANCE
  }
}