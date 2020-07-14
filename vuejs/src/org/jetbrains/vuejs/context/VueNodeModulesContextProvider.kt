// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.context

import com.intellij.javascript.nodejs.library.NodeModulesDirectoryManager
import com.intellij.psi.PsiDirectory
import com.intellij.psi.util.CachedValueProvider
import org.jetbrains.vuejs.index.VUE_MODULE

class VueNodeModulesContextProvider : VueContextProvider {
  override fun isVueContext(directory: PsiDirectory): CachedValueProvider.Result<Boolean> {
    val manager = NodeModulesDirectoryManager.getInstance(directory.project)
    val dirPath = directory.virtualFile.path + "/"
    var result = false
    for (dir in manager.nodeModulesDirectories) {
      val nodeModules = dir.nodeModulesDir
      if (dirPath.startsWith(nodeModules.parent.path + "/")) {
        val child = nodeModules.findFileByRelativePath(VUE_MODULE)
        if (child != null && child.isValid && child.isDirectory) {
          result = true
          break
        }
      }
    }
    return CachedValueProvider.Result.create(result, manager.nodeModulesDirChangeTracker)
  }
}
