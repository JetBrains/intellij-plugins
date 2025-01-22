// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime

import com.intellij.openapi.components.service
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent

internal interface TfToolSettings {
  var toolPath: String
}

internal class SettingsUpdater<T : TfToolSettings>(private val fileEvents: List<VFileEvent>, val string: String, val settingsClass: Class<T>) : AsyncFileListener.ChangeApplier {
  override fun afterVfsChange() {
    val openProjects = ProjectManager.getInstance().openProjects
    val processedFiles = fileEvents.mapNotNull { it.file }.toSet()
    openProjects
      .mapNotNull { project -> project.getService(settingsClass)?.takeIf { it.toolPath.isEmpty() }?.let { project to it } }
      .filter { (project, _) -> processedFiles.any { file -> ProjectFileIndex.getInstance(project).isInProject(file) } }
      .forEach { (project, settings) -> project.service<ToolPathDetector>().detectPathAndUpdateSettingsAsync(settings, string) }
  }
}
