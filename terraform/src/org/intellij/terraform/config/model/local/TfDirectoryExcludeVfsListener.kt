// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.model.local

import com.intellij.openapi.components.service
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent

internal class TfDirectoryExcludeVfsListener : AsyncFileListener {
  override fun prepareChange(events: List<VFileEvent>): AsyncFileListener.ChangeApplier? {
    val terraformPaths = events
      .filterIsInstance<VFileCreateEvent>()
      .filter { it.isDirectory && it.childName == TF_DIRECTORY_NAME }
      .map { it.path }
    if (terraformPaths.isEmpty()) return null

    return object : AsyncFileListener.ChangeApplier {
      override fun afterVfsChange() {
        val fileSystem = StandardFileSystems.local()
        val terraformDirs = terraformPaths.mapNotNull { fileSystem.findFileByPath(it) }

        val projects = ProjectManager.getInstance().openProjects
        for (project in projects) {
          val fileIndex = ProjectFileIndex.getInstance(project)
          val dirsToExclude = terraformDirs.filter { fileIndex.isInProject(it) }

          if (dirsToExclude.isNotEmpty()) {
            project.service<TfDirectoryExcludeService>().excludeTerraformDirs(dirsToExclude)
          }
        }
      }
    }
  }
}