package com.intellij.deno.editor

import com.intellij.deno.DenoUtil
import com.intellij.deno.model.DenoModel
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.impl.EditorTabTitleProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

private class DenoEditorTabTitleProvider : EditorTabTitleProvider, DumbAware {
  override fun getEditorTabTitle(project: Project, file: VirtualFile): String? {
    if (DenoUtil.isDenoCacheFile(file)) {
      val model = project.service<DenoModel>()
      return ReadAction.compute<String, RuntimeException> { model.findOwnUrlForFile(file) }
    }

    return null
  }
}