package org.jetbrains.qodana

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager
import com.intellij.openapi.vfs.VirtualFile

fun refreshVcsFileStatus(project: Project, virtualFile: VirtualFile) {
  VcsDirtyScopeManager.getInstance(project).fileDirty(virtualFile)
}
