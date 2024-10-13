package org.jetbrains.qodana.extensions

import com.intellij.codeHighlighting.Pass
import com.intellij.codeInspection.ex.LocalInspectionToolWrapper
import com.intellij.openapi.progress.blockingContext
import com.intellij.openapi.project.Project
import com.intellij.profile.codeInspection.InspectionProfileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DefaultQodanaHighlightingSupportInfoProvider : QodanaHighlightingSupportInfoProvider {
  override suspend fun getEnabledInspections(project: Project): Set<String> {
    return withContext(Dispatchers.Default) {
      blockingContext {
        InspectionProfileManager.getInstance(project).currentProfile.getAllEnabledInspectionTools(project)
          .filter { it.isEnabled && it.tool is LocalInspectionToolWrapper }
          .map { it.tool.shortName }.toSet()
      }
    }
  }

  override fun getPrecedingPassesIds(): List<Int> {
    return listOf(Pass.LOCAL_INSPECTIONS)
  }
}