// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight.completion.vuex

import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.roots.ProjectRootModificationTracker
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
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

  fun hasVuex(context: PsiElement): Boolean {
    val project = context.project
    if (!hasVue(project)) return false
    val vFile = context.containingFile?.originalFile?.virtualFile ?: return false
    val projectFileIndex = ProjectFileIndex.getInstance(project)
    val module = projectFileIndex.getModuleForFile(vFile) ?: return false
    return CachedValuesManager.getManager(project).getCachedValue(module) {
      val packageJson = PackageJsonUtil.findUpPackageJson(vFile)
      val packageJsonData = if (packageJson != null) PackageJsonUtil.getOrCreateData(packageJson) else null
      val hasVuex = packageJsonData != null && packageJsonData.isDependencyOfAnyType(VUEX)
      CachedValueProvider.Result.create(hasVuex, VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS,
                                        ProjectRootModificationTracker.getInstance(project))
    }
  }

  fun normalizeName(name: String): String {
    return name.substringAfter("$DELIMITER$DELIMITER", name)
  }
}