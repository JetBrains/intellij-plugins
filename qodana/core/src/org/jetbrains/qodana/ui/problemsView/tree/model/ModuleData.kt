package org.jetbrains.qodana.ui.problemsView.tree.model

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import java.nio.file.Path
import kotlin.io.path.Path

data class ModuleData(
  private val project: Project,
  val modulePath: Path,
  val module: Module
) {
  val modulePathRelativeToProject: Path = modulePathRelativeToProject(project)

  private fun modulePathRelativeToProject(project: Project): Path {
    val projectNioPath = project.basePath?.let { Path(it) } ?: project.guessProjectDir()?.toNioPath() ?: return Path("")
    return projectNioPath.relativize(modulePath)
  }
}