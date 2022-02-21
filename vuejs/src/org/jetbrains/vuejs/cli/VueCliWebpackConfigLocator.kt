// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.cli

import com.intellij.javascript.nodejs.NodeModuleSearchUtil
import com.intellij.javascript.nodejs.PackageJsonData
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.buildTools.webpack.WebPackConfigLocator
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiUtilCore

class VueCliWebpackConfigLocator : WebPackConfigLocator {
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
    const val VUE_CLI_SERVICE_PKG = "@vue/cli-service"
  }
}