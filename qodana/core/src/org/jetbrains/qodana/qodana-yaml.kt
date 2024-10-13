package org.jetbrains.qodana

import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.findFile
import org.jetbrains.qodana.staticAnalysis.inspections.config.QODANA_YAML_CONFIG_FILENAME
import org.jetbrains.qodana.staticAnalysis.inspections.config.QODANA_YML_CONFIG_FILENAME

internal suspend fun Project.findQodanaConfigVirtualFile(): VirtualFile? {
  val projectVirtualFile = guessProjectDir() ?: return null
  val filenames = listOf(QODANA_YML_CONFIG_FILENAME, QODANA_YAML_CONFIG_FILENAME)
  for (filename in filenames) {
    val virtualFile = readAction { projectVirtualFile.findFile(filename) }
    if (virtualFile != null) return virtualFile
  }
  return null
}