package org.jetbrains.qodana.inspectionKts.api

import com.intellij.psi.PsiElement

/**
 * DO NOT CHANGE THE SIGNATURE OF THIS METHOD IN NOT API-COMPATIBLE WAYS!
 * IT IS USED BY USER'S .inspection.kts SCRIPTS!!!
 * IF NEEDED, ASK ANY QUESTIONS QODANA CORE TEAM
 */
fun PsiElement.resolveAsReference(): PsiElement? {
  return reference?.resolve()
}