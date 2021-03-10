// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.typescript.service

import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.typescript.compiler.languageService.protocol.TypeScriptLanguageServiceCache
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import org.jetbrains.vuejs.index.findModule
import org.jetbrains.vuejs.lang.html.VueFileType

class VueTypeScriptServiceCache(project: Project) : TypeScriptLanguageServiceCache(project) {
  
  override fun addFileIfInvalid(file: VirtualFile,
                               filesToClose: Set<VirtualFile>,
                               toCloseByChangedType: MutableSet<VirtualFile>) {
    if (isInvalidVueTypeScriptFile(file) && !filesToClose.contains(file)) toCloseByChangedType.add(file)
  }

  private fun isInvalidVueTypeScriptFile(file: VirtualFile): Boolean {
    if (file.isValid) {
      val fileType = file.fileType
      if (fileType == VueFileType.INSTANCE) {
        val findFile = PsiManager.getInstance(myProject).findFile(file)
        if (findFile == null) return false

        val module = findModule(findFile)
        return module == null || !DialectDetector.isTypeScript(module)
      }
    }
    return false
  }
}
