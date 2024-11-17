package org.jetbrains.qodana.ui

import com.intellij.codeInsight.daemon.impl.IntentionsUI
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.writeIntentReadAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.EditorKind
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.FileTypeRegistry
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFileFactory
import com.intellij.ui.components.panels.Wrapper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.qodana.coroutines.QodanaDispatchers

suspend fun createInMemoryDocument(project: Project, text: String, filename: String): Document {
  val fileType = FileTypeRegistry.getInstance().getFileTypeByFileName(filename)
  return readAction {
    val eventSystemEnabled = true
    val psiFile = PsiFileFactory.getInstance(project).createFileFromText(filename, fileType, text, -1, eventSystemEnabled)
    val document = PsiDocumentManager.getInstance(project).getDocument(psiFile) ?: EditorFactory.getInstance().createDocument(text)
    document
  }
}

suspend fun createEditor(project: Project, document: Document, fileType: FileType): Editor {
  return withContext(QodanaDispatchers.Ui) {
    //maybe readaction
    writeIntentReadAction {
      val editor = EditorFactory.getInstance().createEditor(document, project, EditorKind.MAIN_EDITOR)
      (editor as? EditorEx)?.highlighter = EditorHighlighterFactory.getInstance().createEditorHighlighter(project, fileType)
      editor
    }
  }
}

fun editorViewComponentFromFlow(scope: CoroutineScope, editorFlow: Flow<Editor>, project: Project): Wrapper {
  val editorWrapper = Wrapper()
  scope.launch(QodanaDispatchers.Ui, start = CoroutineStart.UNDISPATCHED) {
    editorFlow.collectLatest {
      editorWrapper.setContentAndRepaint(it.component)
      try {
        awaitCancellation()
      }
      finally {
        IntentionsUI.getInstance(project).hideForEditor(it)
      }
    }
  }
  return editorWrapper
}