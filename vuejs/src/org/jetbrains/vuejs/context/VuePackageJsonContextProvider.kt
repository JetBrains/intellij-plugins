// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.context

import com.intellij.javascript.nodejs.PackageJsonData
import com.intellij.javascript.nodejs.packageJson.PackageJsonFileManager
import com.intellij.psi.PsiDirectory
import com.intellij.psi.util.CachedValueProvider
import org.jetbrains.vuejs.index.VUE_CLI_SERVICE_MODULE
import org.jetbrains.vuejs.index.VUE_MODULE

private class VuePackageJsonContextProvider : VueContextProvider {
  override fun isVueContext(directory: PsiDirectory): CachedValueProvider.Result<Boolean> {
    val manager = PackageJsonFileManager.getInstance(directory.project)
    val dirPath = directory.virtualFile.path + "/"
    var result = false
    for (config in manager.validPackageJsonFiles) {
      if (dirPath.startsWith(config.parent.path + "/")) {
        val data = PackageJsonData.getOrCreate(config)
        if (data.isDependencyOfAnyType(VUE_MODULE)
            || data.isDependencyOfAnyType(VUE_CLI_SERVICE_MODULE)) {
          result = true
          break
        }
      }
    }
    return CachedValueProvider.Result.create(result, manager.modificationTracker)
  }
}
