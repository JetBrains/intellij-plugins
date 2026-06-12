package com.intellij.openRewrite.run

import com.intellij.execution.ExecutionException
import com.intellij.execution.ExecutionResult
import com.intellij.execution.filters.HyperlinkInfo
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.ui.ConsoleView
import com.intellij.history.ActivityId
import com.intellij.history.LocalHistory
import com.intellij.history.integration.LocalHistoryImpl
import com.intellij.history.integration.ui.views.DirectoryHistoryDialog
import com.intellij.openRewrite.OpenRewriteBundle
import com.intellij.openRewrite.RECIPE_CLASS_NAME
import com.intellij.openRewrite.RECIPE_FILE_NAME
import com.intellij.openRewrite.STYLE_CLASS_NAME
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.findFile
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiModifier
import com.intellij.util.concurrency.annotations.RequiresReadLock
import java.io.File
import java.util.function.Supplier
import kotlin.io.path.Path

@Throws(ExecutionException::class)
fun executeInLocalHistoryAction(
  @NlsSafe name: String,
  project: Project,
  workingDirectory: String?,
  resultSupplier: Supplier<ExecutionResult?>,
): ExecutionResult? {
  val activityId = ActivityId(OpenRewriteBundle.OPEN_REWRITE, "Recipe")
  val action = LocalHistory.getInstance().startAction(
    OpenRewriteBundle.message("open.rewrite.recipe.local.history.label", name),
    activityId)
  try {
    val result = resultSupplier.get()
    if (result == null) {
      action.finish()
      return null
    }

    val processHandler = result.processHandler
    processHandler.addProcessListener(object : ProcessAdapter() {
      override fun processTerminated(event: ProcessEvent) {
        processHandler.removeProcessListener(this)
        val file = workingDirectory?.let { VfsUtil.findFileByIoFile(File(it), false) }
                   ?: getBaseDir(project)
        if (file == null) {
          action.finish()
          return
        }
        val writer =
          if (processHandler.exitCode == 0) {
            (result.executionConsole as? ConsoleView)?.let {
              ChangesHyperlinkConsoleWriter(it, project, file)
            }
          }
          else {
            null
          }
        if (writer != null) {
          Disposer.register(result.executionConsole, writer)
        }
        file.refresh(true, true) {
          action.finish()
          writer?.printHyperlink()
        }
      }
    })
    return result
  }
  catch (e: ExecutionException) {
    action.finish()
    throw e
  }
}

private fun getBaseDir(project: Project): VirtualFile? {
  return ProjectRootManager.getInstance(project).contentRoots.minByOrNull { it.path.length }
}

@RequiresReadLock
internal fun findConfigFile(workingDirectory: String?, configLocation: String?): VirtualFile? {
  if (!configLocation.isNullOrBlank()) {
    return VfsUtil.findFile(Path(configLocation), false)
  }
  if (workingDirectory.isNullOrBlank()) {
    return null
  }
  val workingDirectoryFile = VfsUtil.findFile(Path(workingDirectory), false) ?: return null
  return workingDirectoryFile.findFile(RECIPE_FILE_NAME)
}

fun splitConfigurationValue(value: String?): List<String> {
  val values = value?.split(",") ?: emptyList()
  return values.map { it.trim() }.filter { it.isNotEmpty() }
}

internal fun isRecipe(psiClass: PsiClass): Boolean {
  if (!psiClass.hasModifierProperty(PsiModifier.PUBLIC) || psiClass.hasModifierProperty(PsiModifier.ABSTRACT)) return false
  val recipeClass = JavaPsiFacade.getInstance(psiClass.project).findClass(RECIPE_CLASS_NAME, psiClass.resolveScope) ?: return false
  return psiClass.isInheritor(recipeClass, true)
}

internal fun isStyle(psiClass: PsiClass): Boolean {
  if (!psiClass.hasModifierProperty(PsiModifier.PUBLIC) || psiClass.hasModifierProperty(PsiModifier.ABSTRACT)) return false
  val recipeClass = JavaPsiFacade.getInstance(psiClass.project).findClass(STYLE_CLASS_NAME, psiClass.resolveScope) ?: return false
  return psiClass.isInheritor(recipeClass, true)
}

private class ChangesHyperlinkConsoleWriter(
  private var console: ConsoleView?,
  private var project: Project?,
  private var file: VirtualFile?,
) : Disposable {
  override fun dispose() {
    console = null
    project = null
    file = null
  }

  fun printHyperlink() {
    console?.printHyperlink(OpenRewriteBundle.message("open.rewrite.recipe.show.changes"), HyperlinkInfo {
      val project = project ?: return@HyperlinkInfo
      val file = file ?: return@HyperlinkInfo

      val gateway = LocalHistoryImpl.getInstanceImpl().gateway
      DirectoryHistoryDialog(project, gateway, file).show()
    })
  }
}