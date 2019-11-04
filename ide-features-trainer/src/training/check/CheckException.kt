/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.check

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil

class CheckException : Check {
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
    val mainMethod = PsiTreeUtil.findChildOfType(psiFile as PsiElement?, PsiMethod::class.java)
    val referenceList = PsiTreeUtil.findChildOfType(mainMethod as PsiElement?, PsiReferenceList::class.java)
    val javaCodeReferenceElement = PsiTreeUtil.findChildOfType(referenceList, PsiJavaCodeReferenceElement::class.java)
    return javaCodeReferenceElement!!.text == "IOException"
  }
}