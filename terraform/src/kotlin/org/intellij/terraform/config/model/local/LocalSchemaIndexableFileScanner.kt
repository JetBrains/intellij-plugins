// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.model.local

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.util.indexing.roots.IndexableFileScanner

private class LocalSchemaIndexableFileScanner : IndexableFileScanner {
  override fun startSession(project: Project): IndexableFileScanner.ScanSession {
    return IndexableFileScanner.ScanSession {
      IndexableFileScanner.IndexableFileVisitor { fileOrDir ->
        if (isTFLock(fileOrDir)) {
          logger<LocalSchemaService>().info("Scanning local schema: $fileOrDir")
          project.service<LocalSchemaService>().scheduleModelRebuild(setOf(fileOrDir))
        }
      }
    }
  }
}

private class LocalSchemaVfsListener : AsyncFileListener {
  override fun prepareChange(events: MutableList<out VFileEvent>): AsyncFileListener.ChangeApplier? {
    val lockFiles = events.filter { isTFLock(it.file) && it !is VFileDeleteEvent }
    if (lockFiles.isEmpty()) return null
    logger<LocalSchemaService>().info("LocalSchemaVfsListener: $events")
    return object : AsyncFileListener.ChangeApplier {
      override fun afterVfsChange() {
        logger<LocalSchemaService>().info("LocalSchemaVfsListener after: $events")

        val files = events.mapNotNullTo(mutableSetOf()) { it.file }
        val projectFiles = mutableMapOf<Project, MutableSet<VirtualFile>>()
        for (file in files) {
          for (project in ProjectManager.getInstance().openProjects) {
            if (!ProjectFileIndex.getInstance(project).isInProject(file)) continue
            projectFiles.getOrPut(project) { mutableSetOf() }.add(file)
          }
        }

        for ((project, pfiles) in projectFiles) {
          project.service<LocalSchemaService>().scheduleModelRebuild(pfiles)
        }

      }
    }
  }

}

internal fun isTFLock(virtualFile: VirtualFile?): Boolean = virtualFile?.name == ".terraform.lock.hcl"