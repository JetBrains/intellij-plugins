/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.check

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.util.PsiTreeUtil

class CheckParameterInfo : Check {
  internal var project: Project? = null
  internal var editor: Editor? = null

  override fun set(project: Project, editor: Editor) {
    this.project = project
    this.editor = editor
  }

  override fun before() {}

  override fun check(): Boolean {
    val document = editor!!.document
    val psiFile = PsiDocumentManager.getInstance(project!!).getPsiFile(document)
    val childrenOfType: Collection<PsiMethodCallExpression> = PsiTreeUtil.findChildrenOfType(psiFile, PsiMethodCallExpression::class.java)
    var myMethodCall: PsiMethodCallExpression? = null
    for (methodCall in childrenOfType) {
      methodCall.methodExpression.canonicalText == "frame.getSize"
      myMethodCall = methodCall
      break
    }
    if (myMethodCall == null) return false
    val literals: Collection<PsiLiteralExpression> = PsiTreeUtil.findChildrenOfType(myMethodCall, PsiLiteralExpression::class.java)
    if (literals.size != 2) return false else {
      if (literals.toTypedArray()[0].value is Int && literals.toTypedArray()[1].value is Int) {
        val width = literals.toTypedArray()[0].value as Int?
        val height = literals.toTypedArray()[1].value as Int?
        if (width != null && height != null && width == 175 && height == 100) return true
      }
    }
    return false
  }

  override fun listenAllKeys(): Boolean {
    return false
  }
}