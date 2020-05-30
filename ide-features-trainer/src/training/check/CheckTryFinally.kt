// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.check

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiKeyword
import com.intellij.psi.util.PsiTreeUtil

class CheckTryFinally : Check {
  private lateinit var project: Project
  private lateinit var editor: Editor

  override fun set(project: Project, editor: Editor) {
    this.project = project
    this.editor = editor
  }

  override fun before() {}

  override fun check(): Boolean {
    val document = editor.document
    val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document)
    var tryText = false
    var finallyText = false
    val childrenOfType: Collection<PsiKeyword> = PsiTreeUtil.findChildrenOfType(psiFile as PsiElement?, PsiKeyword::class.java)
    for (aChildrenOfType in childrenOfType) {
      if (aChildrenOfType.text == "try") tryText = true
      if (aChildrenOfType.text == "finally") finallyText = true
    }
    return tryText && finallyText
  }
}