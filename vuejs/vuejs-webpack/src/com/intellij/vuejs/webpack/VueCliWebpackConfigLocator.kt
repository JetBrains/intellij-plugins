// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.vuejs.webpack

import com.intellij.javascript.nodejs.NodeModuleSearchUtil
import com.intellij.javascript.nodejs.PackageJsonData
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiUtilCore
import com.intellij.webpack.WebpackConfigLocator

class VueCliWebpackConfigLocator : WebpackConfigLocator {
  override fun detectConfig(project: Project, context: PsiElement): VirtualFile? {
    var config: VirtualFile? = null
    val virtualFile = PsiUtilCore.getVirtualFile(context) ?: return null

    PackageJsonUtil.processUpPackageJsonFiles(project, virtualFile) {
      if (it.isValid && PackageJsonData.getOrCreateWithPreferredProject(project, it).isDependencyOfAnyType(VUE_CLI_SERVICE_PKG)) {
        val moduleInfo = NodeModuleSearchUtil.resolveModule(VUE_CLI_SERVICE_PKG, it.parent, emptyList(), project)
        config = moduleInfo?.moduleSourceRoot?.findChild("webpack.config.js")
        if (config != null) {
          return@processUpPackageJsonFiles false
        }
      }
      return@processUpPackageJsonFiles true
    }
    return config
  }

  companion object {
    const val VUE_CLI_SERVICE_PKG: String = "@vue/cli-service"
  }
}