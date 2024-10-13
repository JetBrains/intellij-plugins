// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.qodana.php

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.php.PhpBundle
import com.jetbrains.php.config.library.PhpIncludePathManager
import org.jetbrains.qodana.staticAnalysis.projectDescription.QodanaProjectDescriber

class PhpIncludedPathsProjectDescriber : QodanaProjectDescriber {
  override val id: String = PhpBundle.message("PhpProjectConfigurable.include.path")

  override suspend fun description(project: Project): IncludedPathDescription {
    val libraries = PhpIncludePathManager.getInstance(project).allIncludedRoots.map { LibraryDescription(it) }
    return IncludedPathDescription(libraries)
  }

  class IncludedPathDescription(val libraries: List<LibraryDescription>)

  class LibraryDescription(data: VirtualFile) {
    val paths: String = data.path
  }
}