// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.documentation

import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.modules.NodeModuleUtil
import com.intellij.psi.PsiElement


private val moduleDocUrlPrefix = setOf(
  "@angular/core", "@angular/common", "@angular/router", "@angular/forms", "@angular/platform-browser",
  "@angular/platform-browser-dynamic"
).associateWith { "https://angular.io/" }

fun getExternalDocRelativeUrlPrefix(element: PsiElement): String? =
  element.containingFile.virtualFile
    .let { PackageJsonUtil.findUpPackageJson(it) }
    ?.let { NodeModuleUtil.inferNodeModulePackageName(it) }
    ?.let { moduleDocUrlPrefix[it] }