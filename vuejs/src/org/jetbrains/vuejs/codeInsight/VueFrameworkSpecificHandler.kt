// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.codeInsight

import com.intellij.json.psi.JsonFile
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.frameworks.JSFrameworkSpecificHandler
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager

private const val PINIA_PACKAGE_NAME = "pinia"

class VueFrameworkSpecificHandler : JSFrameworkSpecificHandler {
  override fun useMoreAccurateEvaluation(context: PsiElement): Boolean {
    val virtualFile = context.containingFile.virtualFile ?: return false
    val packageJson: VirtualFile = PackageJsonUtil.findUpPackageJson(virtualFile) ?: return false
    val jsonFile: JsonFile = PsiManager.getInstance(context.project).findFile(packageJson) as? JsonFile ?: return false
    return PackageJsonUtil.findDependencyByName(jsonFile, PINIA_PACKAGE_NAME) != null
  }
}