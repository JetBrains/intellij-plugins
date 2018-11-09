// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.completion.vuex

import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootModificationTracker
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import org.jetbrains.vuejs.index.DELIMITER
import org.jetbrains.vuejs.index.hasVue

object VueStoreUtils {
  const val VUEX = "vuex"
  const val ACTION = "actions"
  const val MUTATION = "mutations"
  const val STATE = "state"
  const val GETTER = "getters"

  fun hasVuex(project: Project): Boolean {
    if (!hasVue(project)) return false
    return CachedValuesManager.getManager(project).getCachedValue(project) {
      var hasVuex = false
      val packageJsonData = PackageJsonUtil.getTopLevelPackageJsonData(project)
      if (packageJsonData != null && packageJsonData.isDependencyOfAnyType(VUEX)) hasVuex = true
      CachedValueProvider.Result.create(hasVuex, VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS,
                                        ProjectRootModificationTracker.getInstance(project))
    }
  }

  fun normalizeName(name: String): String {
    return name.substringAfter("$DELIMITER$DELIMITER", name)
  }
}