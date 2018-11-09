// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.jetbrains.vuejs.codeInsight.completion

import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.codeInsight.lookup.LookupManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.html.HtmlTag
import org.jetbrains.vuejs.VueLanguage

class VueAutoPopupHandler : TypedHandlerDelegate() {
  override fun checkAutoPopup(charTyped: Char, project: Project, editor: Editor, file: PsiFile): Result {
    if (LookupManager.getActiveLookup(editor) != null) return Result.CONTINUE
    if (file.language != VueLanguage.INSTANCE) return Result.CONTINUE

    val element = file.findElementAt(editor.caretModel.offset)
    if (element?.parent !is HtmlTag) return Result.CONTINUE

    if (charTyped == ':' || charTyped == '@') {
      AutoPopupController.getInstance(project).scheduleAutoPopup(editor)
      return Result.STOP
    }

    return Result.CONTINUE
  }
}