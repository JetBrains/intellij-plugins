// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro

import com.intellij.javascript.nodejs.library.node_modules.NodeModulesDirectoryManager
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.openapi.project.Project
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import org.jetbrains.astro.codeInsight.ASTRO_PKG

fun CodeInsightTestFixture.createPackageJsonWithAstroDependency(additionalDependencies: String = "") {
  configureByText(PackageJsonUtil.FILE_NAME, """
    {
      "name": "test",
      "version": "0.0.1",
      "dependencies": {
        "astro": "1.9.0" ${if (additionalDependencies.isBlank()) "" else ", $additionalDependencies"}
      }
    }
  """.trimIndent())
}

fun CodeInsightTestFixture.configureAstroDependencies(vararg modules: AstroTestModule) {
  createPackageJsonWithAstroDependency(
    modules.asSequence()
      .flatMap { it.packageNames.asSequence() }
      .filter { it != ASTRO_PKG }
      .joinToString { "\"${it}\": \"*\"" })
  for (module in modules) {
    tempDirFixture.copyAll("${getAstroTestDataPath()}/node_modules/${module.folder}/", "node_modules")
  }
  forceReloadProjectRoots(project)
}

internal fun forceReloadProjectRoots(project: Project) {
  // TODO - this shouldn't be needed, something's wrong with how roots are set within tests - check RootIndex#myRootInfos
  NodeModulesDirectoryManager.getInstance(project).requestLibrariesUpdate()
}

enum class AstroTestModule(val folder: String, vararg packageNames: String) {
  ASTRO_1_9_0("astro/1.9.0", ASTRO_PKG),
  ;

  val packageNames: List<String>

  init {
    if (packageNames.isEmpty()) {
      this.packageNames = listOf(folder)
    }
    else {
      this.packageNames = packageNames.toList()
    }
  }
}
