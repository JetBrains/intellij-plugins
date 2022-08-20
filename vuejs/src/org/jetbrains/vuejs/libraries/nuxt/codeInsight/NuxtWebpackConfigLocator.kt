// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.nuxt.codeInsight

import com.intellij.javascript.nodejs.NodeModuleSearchUtil
import com.intellij.javascript.nodejs.PackageJsonData
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.buildTools.webpack.WebpackConfigLocator
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiUtilCore
import org.jetbrains.vuejs.libraries.nuxt.NUXT_PKG

class NuxtWebpackConfigLocator : WebpackConfigLocator {
  override fun detectConfig(project: Project, context: PsiElement): VirtualFile? {
    var config: VirtualFile? = null
    val virtualFile = PsiUtilCore.getVirtualFile(context) ?: return null

    PackageJsonUtil.processUpPackageJsonFiles(project, virtualFile) {
      if (it.isValid && PackageJsonData.getOrCreateWithPreferredProject(project, it).isDependencyOfAnyType(NUXT_PKG)) {
        val moduleInfo = NodeModuleSearchUtil.resolveModule(NUXT_PKG, it.parent, emptyList(), project)
        config = moduleInfo?.moduleSourceRoot?.findChild("webpack.config.js")
        if (config != null) {
          return@processUpPackageJsonFiles false
        }
      }
      return@processUpPackageJsonFiles true
    }
    return config
  }
}