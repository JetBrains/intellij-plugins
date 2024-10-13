package org.jetbrains.qodana.staticAnalysis.stat

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.serviceAsync
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import java.util.concurrent.ConcurrentHashMap

@Service(Service.Level.PROJECT)
class InspectionFingerprintAggregatorService(val project: Project) {
  companion object {
    suspend fun getInstance(project: Project): InspectionFingerprintAggregatorService = project.serviceAsync()
  }

  private class CountsInfo(var totalCount: Int, var analyzedCount: Int)

  private val filetypes: ConcurrentHashMap<String, CountsInfo> = ConcurrentHashMap<String, CountsInfo>()

  init {
    iterateFilesAndAct { registerExistingFile(it) }
  }

  private fun iterateFilesAndAct(action: (PsiFile) -> Unit) {
    val psiManager = PsiManager.getInstance(project)

    ApplicationManager.getApplication().runReadAction {
      ProjectFileIndex.getInstance(project).iterateContent { fileOrDir ->
        psiManager.findFile(fileOrDir)?.let { action.invoke(it) }
        true
      }
    }
  }

  private fun registerFile(file: PsiFile, defaultInfo: CountsInfo, incrementer: (CountsInfo) -> Unit) {
    if (file.language.associatedFileType == null) return
    val filetype = file.language.associatedFileType!!.name
    filetypes.compute(filetype) { _, v ->
      if (v == null) {
        defaultInfo
      }
      else {
        incrementer.invoke(v)
        v
      }
    }
  }

  private fun registerExistingFile(file: PsiFile) {
    registerFile(file, CountsInfo(1, 0)) { v -> v.totalCount++ }
  }

  fun registerAnalyzedFile(file: PsiFile) {
    registerFile(file, CountsInfo(1, 1)) { v -> v.analyzedCount++ }
  }

  fun logFingerprint() {
    filetypes.entries.filter { it.key.isNotEmpty() }.forEach { (key, value) ->
      InspectionEventsCollector.logInspectionFingerprint(key, value.totalCount, value.analyzedCount, project)
    }
  }
}
