/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.check

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager

class CheckJumpFromString : Check {
  internal var project: Project? = null
  internal var editor: Editor? = null

  override fun set(project: Project, editor: Editor) {
    this.project = project
    this.editor = editor
  }

  override fun before() {}
  
  override fun check(): Boolean {
    val caretModel = editor!!.caretModel
    val document = editor!!.document
    val psiFile = PsiDocumentManager.getInstance(project!!).getPsiFile(document)!!
    val elementAt = psiFile.findElementAt(caretModel.offset)!!
    return elementAt.parent != null && elementAt.parent.text == "String"
  }
}