package com.intellij.deno.editor

import com.intellij.deno.DenoUtil
import com.intellij.deno.lang.DenoFileTypeOverrider
import com.intellij.openapi.fileEditor.impl.EditorTabTitleProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager

class DenoEditorTabTitleProvider : EditorTabTitleProvider, DumbAware {
  override fun getEditorTabTitle(project: Project, file: VirtualFile): String? {
    if (DenoUtil.isDenoCacheFile(file)) {
      val psiManager = PsiManager.getInstance(project)
      return DenoUtil.getOwnUrlForFile(psiManager, file)
    }

    return null
  }
}