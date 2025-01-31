package org.angular2.editor

import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.CodeInsightSettings
import com.intellij.codeInsight.completion.CompletionConfidence
import com.intellij.codeInsight.hint.ShowParameterInfoHandler
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.ThreeState
import org.angular2.codeInsight.shouldPopupParameterInfoOnCompletion

private val SHOULD_SKIP_AUTO_POPUP_ON_IMPORT_COMPLETION: Key<Boolean> = Key.create("angular2.skip.auto.popup.on.import.completion")
private val AUTO_POPUP_ON_IMPORT_COMPLETION_SKIPPED: Key<Boolean> = Key.create("angular2.auto.popup.on.import.completion.skipped")

class Angular2PreventCompletionAutoPopupOnImport : CompletionConfidence() {

  override fun shouldSkipAutopopup(editor: Editor, contextElement: PsiElement, psiFile: PsiFile, offset: Int): ThreeState {
    if (editor.getUserData(SHOULD_SKIP_AUTO_POPUP_ON_IMPORT_COMPLETION) == true) {
      editor.putUserData(AUTO_POPUP_ON_IMPORT_COMPLETION_SKIPPED, true)
      editor.putUserData(SHOULD_SKIP_AUTO_POPUP_ON_IMPORT_COMPLETION, null)
      return ThreeState.YES
    }
    return super.shouldSkipAutopopup(editor, contextElement, psiFile, offset)
  }

}

fun delayCompletionAutoPopupOnImport(editor: Editor) {
  editor.putUserData(SHOULD_SKIP_AUTO_POPUP_ON_IMPORT_COMPLETION, true)
  editor.putUserData(AUTO_POPUP_ON_IMPORT_COMPLETION_SKIPPED, null)
}

fun scheduleDelayedAutoPopupIfNeeded(editor: Editor?, place: PsiElement) {
  editor?.putUserData(SHOULD_SKIP_AUTO_POPUP_ON_IMPORT_COMPLETION, null)
  val project = editor?.project ?: return
  if (editor.getUserData(AUTO_POPUP_ON_IMPORT_COMPLETION_SKIPPED) == true) {
    editor.putUserData(AUTO_POPUP_ON_IMPORT_COMPLETION_SKIPPED, null)
    if (shouldPopupParameterInfoOnCompletion(place) && CodeInsightSettings.getInstance().AUTO_POPUP_PARAMETER_INFO) {
      ApplicationManager.getApplication().invokeLater {
        ShowParameterInfoHandler.invoke(project, editor, place.containingFile, -1, null, false)
      }
    }
    AutoPopupController.getInstance(project).scheduleAutoPopup(editor)
  }
}