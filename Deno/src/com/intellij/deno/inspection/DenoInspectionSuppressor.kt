package com.intellij.deno.inspection

import com.intellij.codeInspection.InspectionSuppressor
import com.intellij.codeInspection.SuppressQuickFix
import com.intellij.deno.DenoSettings
import com.intellij.psi.PsiElement

class DenoInspectionSuppressor : InspectionSuppressor {
  override fun getSuppressActions(element: PsiElement?, toolId: String): Array<SuppressQuickFix> {
    return emptyArray()
  }

  override fun isSuppressedFor(element: PsiElement, toolId: String): Boolean {
    if (!DenoSettings.getService(element.project).isUseDeno()) return false

    return toolId == "ES6AwaitOutsideAsyncFunction" ||
           toolId == "TypeScriptValidateJSTypes"
  }
}