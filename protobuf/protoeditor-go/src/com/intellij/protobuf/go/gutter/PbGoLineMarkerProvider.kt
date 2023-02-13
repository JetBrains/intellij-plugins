package com.intellij.protobuf.go.gutter

import com.goide.GoLanguage
import com.intellij.protobuf.ide.gutter.PbLanguageSpecificLineMarkerProvider
import com.intellij.psi.PsiElement

internal class PbGoLineMarkerProvider: PbLanguageSpecificLineMarkerProvider() {
  override fun isAcceptableElement(element: PsiElement): Boolean {
    return element.language == GoLanguage.INSTANCE
  }
}