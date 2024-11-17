package org.jetbrains.qodana.jvm.swagger

import com.intellij.openapi.project.Project
import org.jetbrains.qodana.staticAnalysis.projectDescription.QodanaProjectDescriber
import java.nio.file.Path

internal class OpenApiDescriber : QodanaProjectDescriber {
  override val id: String = "OpenAPI"

  override suspend fun describe(path: Path, project: Project) {
    return
  }
}