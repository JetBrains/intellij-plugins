package org.angular2.lang.expr.service.tcb

import com.intellij.lang.javascript.TypeScriptFileType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.readAction
import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.util.DimensionService
import com.intellij.openapi.wm.WindowManager
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.angular2.Angular2InjectionUtils
import org.angular2.lang.Angular2LangUtil

private class Angular2VisualizeGeneratedTcbAction : AnAction() {
  override fun getActionUpdateThread(): ActionUpdateThread =
    ActionUpdateThread.BGT

  override fun update(e: AnActionEvent) {
    e.presentation.isEnabledAndVisible = false
    if (!ApplicationManager.getApplication().isInternal) return
    val file = e.getData(CommonDataKeys.PSI_FILE)
    if (file != null
        && file.fileType.let {
        it == TypeScriptFileType
        || Angular2LangUtil.isAngular2HtmlFileType(it)
        || Angular2LangUtil.isAngular2SvgFileType(it)
      } && Angular2LangUtil.isAngular2Context(file)) {
      e.presentation.isEnabledAndVisible = true
    }
  }

  override fun actionPerformed(e: AnActionEvent) {
    val element = Angular2InjectionUtils.getElementAtCaretFromContext(e.dataContext) ?: return

    // Ensure that we will recreate TCB
    element.manager.dropPsiCaches()
    @Suppress("HardCodedStringLiteral")
    runWithModalProgressBlocking(element.project, "Building TCBs") {
      val transpiledTemplate =
        readAction {
          Angular2TranspiledDirectiveFileBuilder.getTranspiledDirectiveAndTopLevelSourceFile(
            element.containingFile ?: return@readAction null)?.first
        }
        ?: return@runWithModalProgressBlocking


      withContext(Dispatchers.EDT) {
        val project = element.project
        val dimensionKey = "TcbMapInspector.frame"
        if (DimensionService.getInstance().getSize(dimensionKey, project) == null) {
          val frameSize = WindowManager.getInstance().getFrame(project)?.size
          DimensionService.getInstance().setSize(dimensionKey, frameSize, project)
        }

        val dialogBuilder = DialogBuilder(project)
        val mapInspector = Angular2TranspiledTemplateInspector(transpiledTemplate, project, dialogBuilder)

        @Suppress("HardCodedStringLiteral")
        dialogBuilder
          .title("Visualization of Angular Template Transpilation")
          .centerPanel(mapInspector.createMainComponent())
          .dimensionKey(dimensionKey)
          .showNotModal()
      }
    }
  }
}