// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.context

import com.intellij.javascript.nodejs.packageJson.PackageJsonFileManager
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.psi.PsiDirectory
import com.intellij.psi.util.CachedValueProvider
import org.jetbrains.vuejs.index.VUE_MODULE

class VuePackageJsonContextProvider : VueContextProvider {
  override fun isVueContext(directory: PsiDirectory): CachedValueProvider.Result<Boolean> {
    val manager = PackageJsonFileManager.getInstance(directory.project)
    val dirPath = directory.virtualFile.path + "/"
    var result = false
    for (config in manager.validPackageJsonFiles) {
      if (dirPath.startsWith(config.parent.path + "/")) {
        val data = PackageJsonUtil.getOrCreateData(config)
        if (data.isDependencyOfAnyType(VUE_MODULE)) {
          result = true
          break
        }
      }
    }
    return CachedValueProvider.Result.create(result, manager.modificationTracker)
  }
}
