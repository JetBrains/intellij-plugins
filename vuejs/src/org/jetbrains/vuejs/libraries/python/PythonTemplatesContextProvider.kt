// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.python

import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.python.templateLanguages.TemplatesService
import org.jetbrains.vuejs.context.VueContextProvider

class PythonTemplatesContextProvider : VueContextProvider {

  override fun isVueContextForbidden(contextFile: VirtualFile,
                                     project: Project): Boolean =
    ModuleManager.getInstance(project)
      .modules
      .asSequence()
      .filter { it.moduleContentScope.contains(contextFile) }
      .flatMap { TemplatesService.getInstance(it)?.templateFolders ?: emptyList() }
      .any { VfsUtil.isAncestor(it, contextFile, false) }
}
