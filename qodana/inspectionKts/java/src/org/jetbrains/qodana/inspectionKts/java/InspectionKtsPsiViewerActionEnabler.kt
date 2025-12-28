package org.jetbrains.qodana.inspectionKts.java

import com.intellij.dev.psiViewer.PsiViewerActionEnabler
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import org.jetbrains.qodana.inspectionKts.INSPECTIONS_KTS_DIRECTORY

private class InspectionKtsPsiViewerActionEnabler : PsiViewerActionEnabler {
  override fun isEnabled(project: Project): Boolean {
    return project.guessProjectDir()?.findChild(INSPECTIONS_KTS_DIRECTORY) != null
  }
}