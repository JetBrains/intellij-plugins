package org.jetbrains.qodana.ui.ci.providers

import com.intellij.openapi.application.readAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.ClientProperty
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.JBColor
import com.intellij.ui.SideBorder
import com.intellij.ui.components.panels.NonOpaquePanel
import com.intellij.ui.components.panels.Wrapper
import com.intellij.util.ui.JBEmptyBorder
import com.intellij.util.ui.components.BorderLayoutPanel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.qodana.coroutines.QodanaDispatchers
import org.jetbrains.qodana.report.BannerContentProvider
import org.jetbrains.qodana.staticAnalysis.inspections.runner.FULL_SARIF_REPORT_NAME
import org.jetbrains.qodana.ui.editorViewComponentFromFlow
import java.nio.file.InvalidPathException
import java.nio.file.Path
import javax.swing.JPanel
import kotlin.io.path.Path

suspend fun VirtualFile.alreadyContainsQodana(): Boolean {
  val text = withContext(QodanaDispatchers.IO) {
    this@alreadyContainsQodana.inputStream.bufferedReader().readText()
  }
  return text.contains("qodana", ignoreCase = true)
}

suspend fun getPhysicalConfigState(project: Project, configFileWithQodana: VirtualFile): CIConfigFileState.Physical? {
  val document = readAction { FileDocumentManager.getInstance().getDocument(configFileWithQodana) } ?: return null
  return CIConfigFileState.Physical(project, document, configFileWithQodana.toNioPath())
}

fun String.toNioPathSafe(): Path? {
  return try {
    Path(this)
  }
  catch (_ : InvalidPathException) {
    null
  }
}

fun bannerWithEditorComponent(
  scope: CoroutineScope,
  bannerContentProviderFlow: Flow<BannerContentProvider?>,
  editorFlow: Flow<Editor>,
  project: Project,
): BorderLayoutPanel {
  val editorWrapper = editorViewComponentFromFlow(scope, editorFlow.onEach { it.setBorder(null) }, project)
  val mainPanel = BorderLayoutPanel().apply {
    addToCenter(editorWrapper)
  }
  scope.launch(QodanaDispatchers.Ui, start = CoroutineStart.UNDISPATCHED) {
    var lastBannerView: NonOpaquePanel? = null
    bannerContentProviderFlow.collect {
      val newBannerView = it?.let { bannerView(scope, it) }
      if (lastBannerView != null) {
        mainPanel.remove(lastBannerView)
      }
      if (newBannerView != null) {
        mainPanel.addToTop(newBannerView)
        mainPanel.border = SideBorder(JBColor.border(), SideBorder.LEFT or SideBorder.BOTTOM or SideBorder.RIGHT)
      } else {
        mainPanel.border = SideBorder(JBColor.border(), SideBorder.ALL)
      }
      lastBannerView = newBannerView
      mainPanel.revalidate()
      mainPanel.repaint()
    }
  }

  return mainPanel
}

fun JPanel.withBottomInsetBeforeComment(inset: Int = 6): Wrapper {
  return Wrapper(
    Wrapper(this).apply {
      border = JBEmptyBorder(0, 0, inset, 0)
    }
  )
}

private fun bannerView(viewScope: CoroutineScope, bannerContentProvider: BannerContentProvider): NonOpaquePanel {
  val editorNotificationPanel = EditorNotificationPanel(EditorNotificationPanel.Status.Warning)
  for (action in bannerContentProvider.actions) {
    editorNotificationPanel.createActionLabel(action.text) {
      viewScope.launch(QodanaDispatchers.Default) { action.callback.invoke() }
    }
  }
  editorNotificationPanel.text = bannerContentProvider.text
  editorNotificationPanel.setCloseAction(bannerContentProvider.onClose)
  val wrapper = NonOpaquePanel(editorNotificationPanel)
  wrapper.border = ClientProperty.get(editorNotificationPanel, FileEditorManager.SEPARATOR_BORDER)
  return wrapper
}

suspend fun getSarifBaseline(project: Project): String? {
  return readAction {
    project.guessProjectDir()?.findChild(FULL_SARIF_REPORT_NAME)?.presentableName
  }
}