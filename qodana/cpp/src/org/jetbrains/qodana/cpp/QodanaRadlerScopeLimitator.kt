package org.jetbrains.qodana.cpp

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.analysis.AnalysisScope
import com.intellij.codeInspection.ex.JobDescriptor
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.application.readAction
import com.jetbrains.cidr.lang.workspace.OCResolveConfigurations
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaGlobalInspectionContext
import com.intellij.codeInspection.ex.GlobalInspectionContextBase
import org.jetbrains.qodana.staticAnalysis.inspections.runner.externalTools.ExternalToolsConfigurationProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class QodanaRadlerScopeLimitator: ExternalToolsConfigurationProvider {
  override fun announceJobDescriptors(context: QodanaGlobalInspectionContext) = emptyArray<JobDescriptor>()

  override suspend fun performPreRunActivities(context: QodanaGlobalInspectionContext) {
    val project = (context as GlobalInspectionContextBase).project
    context.currentScope.ignoreNonSolutionItems(project)
    if (java.lang.Boolean.getBoolean("qodana.cpp.log.scope")) {
      logScopeToLogDir(context.currentScope)
    }
  }

  override suspend fun performPostRunActivities(context: QodanaGlobalInspectionContext) {  }

  private fun AnalysisScope.ignoreNonSolutionItems(project: Project) {
    val currentFilter = this.filter
    this.setFilter(object : GlobalSearchScope() {
      override fun contains(file: VirtualFile): Boolean {
        val conf = OCResolveConfigurations.getPreselectedConfiguration(file, project)
        val defaultChoice = currentFilter?.contains(file) ?: true
        return defaultChoice && (conf == null || conf.hasSourceFile(file))
      }

      override fun isSearchInModuleContent(aModule: Module): Boolean {
        return currentFilter?.isSearchInModuleContent(aModule) ?: true
      }

      override fun isSearchInLibraries(): Boolean {
        return currentFilter?.isSearchInLibraries() ?: true
      }
    })
    this.invalidate()
  }

  private suspend fun logScopeToLogDir(scope: AnalysisScope) {
    val logDir = PathManager.getLogPath()
    val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
    val currentDateTime = dateTimeFormat.format(Date())
    val logFileName = "scope_${currentDateTime}.log"
    val logFile = File(logDir, logFileName)
    logFile.writer().let {
      val files = mutableSetOf<VirtualFile>()
      readAction {
        scope.accept { f ->
          files.add(f)
        }
      }

      files.map { f -> f.path }.sorted().forEach(it::appendLine)
      it.close()
    }
  }
}