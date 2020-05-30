// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.completion

import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.codeInsight.lookup.LookupManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.html.HtmlTag
import org.jetbrains.vuejs.codeInsight.ATTR_ARGUMENT_PREFIX
import org.jetbrains.vuejs.codeInsight.ATTR_EVENT_SHORTHAND
import org.jetbrains.vuejs.codeInsight.ATTR_SLOT_SHORTHAND
import org.jetbrains.vuejs.lang.html.VueLanguage

class VueAutoPopupHandler : TypedHandlerDelegate() {
  override fun checkAutoPopup(charTyped: Char, project: Project, editor: Editor, file: PsiFile): Result {
    if (LookupManager.getActiveLookup(editor) != null) return Result.CONTINUE
    if (file.language != VueLanguage.INSTANCE) return Result.CONTINUE

    val element = file.findElementAt(editor.caretModel.offset)
    if (element?.parent !is HtmlTag) return Result.CONTINUE

    if (charTyped == ATTR_ARGUMENT_PREFIX
        || charTyped == ATTR_EVENT_SHORTHAND
        || charTyped == ATTR_SLOT_SHORTHAND) {
      AutoPopupController.getInstance(project).scheduleAutoPopup(editor)
      return Result.STOP
    }

    return Result.CONTINUE
  }
}
