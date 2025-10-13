// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs

import com.intellij.lang.typescript.tsconfig.TypeScriptConfigServiceImpl.Companion.getNearestParentTsConfigsSequence
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.vuejs.model.VueGlobalImpl
import org.jetbrains.vuejs.model.VueMode

@ApiStatus.Internal
object VueProjects {
  fun isTypeScriptProjectDirectory(
    directory: PsiDirectory,
  ): Boolean =
    getNearestParentTsConfigsSequence(
      project = directory.project,
      fileOrDirectory = directory.virtualFile,
      checkCurrentDirectoryOnly = false,
    ).any()

  fun isTypeScriptProjectFile(
    file: PsiFile?,
  ): Boolean {
    val directory = file?.parent
                    ?: return false

    return isTypeScriptProjectDirectory(directory)
  }

  fun isVaporProjectDirectory(
    directory: PsiDirectory,
  ): Boolean {
    val apps = VueGlobalImpl.get(directory).apps
    return apps.isNotEmpty() &&
           apps.all { it.mode == VueMode.VAPOR }
  }
}
