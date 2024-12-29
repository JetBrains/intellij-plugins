package org.jetbrains.qodana.highlight

import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.ex.MarkupModelEx
import com.intellij.openapi.editor.ex.RangeHighlighterEx
import com.intellij.openapi.editor.ex.util.EditorUtil
import com.intellij.openapi.editor.impl.DocumentMarkupModel
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.editor.impl.event.MarkupModelListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Key
import com.intellij.platform.util.coroutines.childScope
import com.intellij.psi.PsiFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.coroutines.qodanaProjectScope
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

private val QODANA_HIGHLIGHTING_PASS_STATE = Key.create<QodanaHighlightingPassState>("Qodana.Highlighting.Pass.State")

internal class QodanaHighlightingPassState(
  private val project: Project,
  private val editor: EditorEx,
) {
  companion object {
    fun getOrCreateForEditor(project: Project, editor: Editor, highlightedReportService: QodanaHighlightedReportService, file: PsiFile): QodanaHighlightingPassState? {
      if (editor !is EditorImpl) return null

      val newPassState = QodanaHighlightingPassState(project, editor)
      val cachedPassState = editor.putUserDataIfAbsent(QODANA_HIGHLIGHTING_PASS_STATE, newPassState)
      if (cachedPassState !== newPassState) return cachedPassState

      cachedPassState.scope.launch(QodanaDispatchers.Default) {
        cachedPassState.subscribeToOtherHighlights(highlightedReportService)
      }
      return cachedPassState
    }
  }

  private val infosFromPass = AtomicReference<List<HighlightInfo>>(emptyList())

  private var analyzedOnce = AtomicBoolean(false)

  val scope: CoroutineScope by lazy {
    val scope = project.qodanaProjectScope.childScope()
    scope.launch(QodanaDispatchers.Ui) {
      EditorUtil.disposeWithEditor(editor) {
        scope.cancel()
      }
    }
    return@lazy scope
  }

  fun updateWasAnalysedOnce(newStatus: Boolean): Boolean {
    if (analyzedOnce.compareAndSet(false, newStatus)) {
      return newStatus
    }
    return true
  }

  fun setInfosFromPass(infos: List<HighlightInfo>) {
    infosFromPass.set(infos)
  }

  suspend fun subscribeToOtherHighlights(highlightedReportService: QodanaHighlightedReportService) {
    val markupModel = DocumentMarkupModel.forDocument(editor.document, project, false) as? MarkupModelEx ?: return
    // TODO – emit from here in batches and process
    callbackFlow<Unit> {
      val disposable = Disposer.newDisposable()
      val listener = object : MarkupModelListener {
        override fun afterAdded(highlighter: RangeHighlighterEx) {
          val qodanaHighlights = infosFromPass.get()
            .filter { it.type is QodanaHighlightingInfoType }

          if (qodanaHighlights.isEmpty()) return
          if (highlighter.errorStripeTooltip in qodanaHighlights) return

          val qodanaHighlightersDuplicatingIde = getQodanaHighlightsDuplicatingIde(highlighter, qodanaHighlights).toSet()
          val toUpdate = qodanaHighlightersDuplicatingIde
            .map {
              SarifProblemPropertiesUpdater((it.type as QodanaHighlightingInfoType).sarifProblem) { properties ->
                properties.copy(isFixed = false)
              }
            }

          val highlightedReportData = highlightedReportService.highlightedReportState.value.highlightedReportDataIfSelected
          highlightedReportData?.updateProblemsProperties(toUpdate)

          qodanaHighlightersDuplicatingIde.forEach { qodanaHighlight ->
            val qodanaHighlighter = qodanaHighlight.highlighter ?: return@forEach
            markupModel.removeHighlighter(qodanaHighlighter)
          }
        }
      }
      markupModel.addMarkupModelListener(disposable, listener)
      awaitClose { Disposer.dispose(disposable) }
    }.flowOn(QodanaDispatchers.Default).collect()
  }
}