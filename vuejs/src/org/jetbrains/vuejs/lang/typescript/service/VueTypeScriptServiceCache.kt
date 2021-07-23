// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang.typescript.service

import com.intellij.lang.typescript.compiler.languageService.protocol.TypeScriptLanguageServiceCache
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.vuejs.lang.html.VueFileType

class VueTypeScriptServiceCache(project: Project) : TypeScriptLanguageServiceCache(project) {

  override fun addFileIfInvalid(file: VirtualFile,
                                filesToClose: Set<VirtualFile>,
                                toCloseByChangedType: MutableSet<VirtualFile>) {
    if ((!file.isValid || file.fileType != VueFileType.INSTANCE) && !filesToClose.contains(file)) toCloseByChangedType.add(file)
  }
}
