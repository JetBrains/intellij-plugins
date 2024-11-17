package com.jetbrains.cidr.cpp.embedded.platformio.ui

import com.intellij.ide.IdeBundle
import com.intellij.ide.impl.isTrusted
import com.intellij.openapi.project.Project
import com.intellij.ide.trustedProjects.TrustedProjectsDialog
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle
import java.util.concurrent.CancellationException

suspend fun ensureProjectIsTrusted(project: Project) {
  if (project.isTrusted()) return
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
      trustButtonText = IdeBundle.message("untrusted.project.dialog.trust.button"),
      distrustButtonText = IdeBundle.message("untrusted.project.dialog.distrust.button")
    )