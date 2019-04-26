// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.typescript.service

import com.intellij.lang.javascript.service.JSLanguageService
import com.intellij.lang.javascript.service.highlighting.JSLanguageServiceHighlightingPassFactory
import com.intellij.lang.typescript.compiler.TypeScriptServiceHighlightingPassFactory
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.jetbrains.vuejs.VueFileType

internal class VueTypeScriptServiceHighlightingPassFactory(project: Project) : JSLanguageServiceHighlightingPassFactory(project) {
  override fun getService(file: PsiFile): JSLanguageService? {
    val service = TypeScriptServiceHighlightingPassFactory.getService(myProject, file)
    return if (service is VueTypeScriptService) service else null
  }

  override fun isAcceptablePsiFile(file: PsiFile): Boolean {
    val fileType = file.fileType
    return fileType == VueFileType.INSTANCE
  }
}
