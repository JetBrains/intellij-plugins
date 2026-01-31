package com.jetbrains.cidr.cpp.embedded.platformio.ui

import com.intellij.ide.IdeBundle
import com.intellij.ide.trustedProjects.TrustedProjects
import com.intellij.ide.trustedProjects.TrustedProjectsDialog
import com.intellij.openapi.project.Project
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle
import java.util.concurrent.CancellationException

suspend fun ensureProjectIsTrusted(project: Project) {
  if (TrustedProjects.isProjectTrusted(project)) return
  if (!showUntrustedProjectLoadDialog(project)) {
    throw CancellationException(ClionEmbeddedPlatformioBundle.message("project.not.trusted"))
  }
}

suspend fun showUntrustedProjectLoadDialog(project: Project): Boolean =
  TrustedProjectsDialog
    .confirmLoadingUntrustedProjectAsync(
      project = project,
      title = IdeBundle.message("untrusted.project.dialog.title", ClionEmbeddedPlatformioBundle.message("platformio.name"), 1),
      message = IdeBundle.message("untrusted.project.dialog.text", ClionEmbeddedPlatformioBundle.message("platformio.name"), 1),
    )