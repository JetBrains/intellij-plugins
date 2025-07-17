// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs

import com.intellij.lang.typescript.tsconfig.TypeScriptConfigServiceImpl.Companion.getNearestParentTsConfigsSequence
import com.intellij.psi.PsiDirectory
import org.jetbrains.annotations.ApiStatus

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
}
