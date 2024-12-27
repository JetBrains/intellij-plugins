package com.intellij.javascript.ift

import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.psi.PsiFile
import training.onboarding.AbstractOnboardingTipsDocumentationProvider

private class JavaScriptOnboardingTipsDocumentationProvider: AbstractOnboardingTipsDocumentationProvider(JSTokenTypes.END_OF_LINE_COMMENT) {
  override fun isLanguageFile(file: PsiFile): Boolean {
    return file is JSFile
  }
}