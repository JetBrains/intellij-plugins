package org.jetbrains.qodana

import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager
import com.intellij.openapi.vfs.VirtualFile

suspend fun refreshVcsFileStatus(project: Project, virtualFile: VirtualFile) {
  readAction {
    VcsDirtyScopeManager.getInstance(project).fileDirty(virtualFile)
  }
}