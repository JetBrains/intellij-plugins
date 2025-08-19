// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.copyright

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import com.maddyhome.idea.copyright.CopyrightProfile
import com.maddyhome.idea.copyright.options.LanguageOptions
import com.maddyhome.idea.copyright.psi.UpdateCopyright
import com.maddyhome.idea.copyright.psi.UpdateCopyrightsProvider
import com.maddyhome.idea.copyright.psi.UpdatePsiFileCopyright
import org.intellij.terraform.isTerraformCompatiblePsiFile

internal class TfUpdateCopyrightsProvider : UpdateCopyrightsProvider() {
  override fun createInstance(
    project: Project?,
    module: Module?,
    virtualFile: VirtualFile?,
    fileType: FileType?,
    options: CopyrightProfile?,
  ): UpdateCopyright = object : UpdatePsiFileCopyright(project, module, virtualFile, options) {
    override fun accept(): Boolean = isTerraformCompatiblePsiFile(file)

    override fun scanFile() {
      val firstChild = file.getFirstChild()
      checkComments(firstChild, PsiTreeUtil.skipSiblingsForward(firstChild, PsiComment::class.java, PsiWhiteSpace::class.java), true)
    }
  }

  override fun getDefaultOptions(): LanguageOptions {
    val options = LanguageOptions()
    options.fileTypeOverride = LanguageOptions.USE_TEXT
    options.isBlock = false
    options.isPrefixLines = false
    return options
  }
}