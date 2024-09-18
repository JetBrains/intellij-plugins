package com.intellij.deno.lang

import com.intellij.deno.DenoSettings
import com.intellij.deno.DenoUtil
import com.intellij.deno.model.DenoModel
import com.intellij.lang.javascript.index.JSItemPresentationProvider
import com.intellij.openapi.components.service
import com.intellij.psi.PsiFile

class DenoItemPresentationProvider: JSItemPresentationProvider {
  override fun getFileName(psiFile: PsiFile): String? {
    val name = psiFile.name
    if (name.length != DenoUtil.HASH_FILE_NAME_LENGTH) return null
    val virtualFile = psiFile.getOriginalFile().getViewProvider().getVirtualFile()
    val project = psiFile.project
    val denoCache = DenoSettings.getService(project).getDenoCache()
    if (!virtualFile.path.contains(denoCache)) return null

    val model = project.service<DenoModel>()
    return model.findOwnUrlForFile(virtualFile)
  }
}