// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.blocks

import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.util.elementType
import com.intellij.psi.xml.XmlDocument
import com.intellij.psi.xml.XmlElementType
import com.intellij.psi.xml.XmlText
import org.angular2.lang.html.Angular2TemplateSyntax

class Angular2BlocksTypedHandler : TypedHandlerDelegate() {

  override fun checkAutoPopup(charTyped: Char, project: Project, editor: Editor, file: PsiFile): Result {
    if (charTyped == '@' && Angular2TemplateSyntax.of(file)?.enableBlockSyntax == true) {
      val at = file.findElementAt(editor.getCaretModel().offset)
      if (at != null &&
          (at.parent.let { it is XmlDocument || it is XmlText }
           || at.elementType == XmlElementType.XML_END_TAG_START)) {
        AutoPopupController.getInstance(project)
          .scheduleAutoPopup(editor, CompletionType.BASIC, null)
      }
    }
    return Result.CONTINUE
  }


}