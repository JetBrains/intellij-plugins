// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.typescript.service

import com.intellij.codeHighlighting.TextEditorHighlightingPassFactoryRegistrar
import com.intellij.codeHighlighting.TextEditorHighlightingPassRegistrar
import com.intellij.lang.javascript.service.JSLanguageService
import com.intellij.lang.javascript.service.highlighting.JSLanguageServiceHighlightingPassFactory
import com.intellij.lang.typescript.compiler.TypeScriptServiceHighlightingPassFactory
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.jetbrains.vuejs.lang.html.VueFileType

internal class VueTypeScriptServiceHighlightingPassFactoryRegistrar : TextEditorHighlightingPassFactoryRegistrar {
  override fun registerHighlightingPassFactory(registrar: TextEditorHighlightingPassRegistrar, project: Project) {
    class MyFactory : JSLanguageServiceHighlightingPassFactory(project, registrar) {
      override fun getService(file: PsiFile): JSLanguageService? {
        val service = TypeScriptServiceHighlightingPassFactory.getService(myProject, file)
        return if (service is VueTypeScriptService) service else null
      }

      override fun isAcceptablePsiFile(file: PsiFile): Boolean {
        return file.fileType == VueFileType.INSTANCE
      }
    }
    MyFactory()
  }
}
