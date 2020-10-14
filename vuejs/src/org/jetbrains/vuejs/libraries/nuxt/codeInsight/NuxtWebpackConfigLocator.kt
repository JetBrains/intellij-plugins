// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.nuxt.codeInsight

import com.intellij.javascript.nodejs.NodeModuleDirectorySearchProcessor
import com.intellij.javascript.nodejs.NodeModuleSearchUtil
import com.intellij.javascript.nodejs.PackageJsonData
import com.intellij.javascript.nodejs.packageJson.PackageJsonFileManager
import com.intellij.lang.javascript.buildTools.webpack.WebPackConfigLocator
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.libraries.nuxt.NUXT_PKG

class NuxtWebpackConfigLocator : WebPackConfigLocator {
  override fun detectConfig(project: Project, context: PsiElement): VirtualFile? =
    PackageJsonFileManager.getInstance(project).validPackageJsonFiles
      .asSequence()
      .filter { it.isValid && PackageJsonData.getOrCreateWithPreferredProject(project, it).isDependencyOfAnyType(NUXT_PKG) }
      .mapNotNull {
        NodeModuleSearchUtil.resolveModuleFromNodeModulesDir(
          it.parent, NUXT_PKG, NodeModuleDirectorySearchProcessor.PROCESSOR)
      }
      .mapNotNull { it.moduleSourceRoot.findChild("webpack.config.js") }
      .firstOrNull()
}