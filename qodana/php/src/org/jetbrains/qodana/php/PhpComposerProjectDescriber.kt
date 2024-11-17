// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.qodana.php

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.jetbrains.php.composer.ComposerConfigUtils
import com.jetbrains.php.composer.ComposerDataService
import com.jetbrains.php.composer.InstalledPackageData
import kotlinx.coroutines.withContext
import org.jetbrains.qodana.staticAnalysis.StaticAnalysisDispatchers
import org.jetbrains.qodana.staticAnalysis.projectDescription.QodanaProjectDescriber

class PhpComposerProjectDescriber : QodanaProjectDescriber {
  override val id: String = "Composer"

  override suspend fun description(project: Project): ComposerPackagesDescription {
    val currentComposerConfig = ComposerDataService.getInstance(project).currentConfigFile
    val specifiedInComposerPackages = mutableMapOf<String, SpecifiedPackageData>()
    val installedComposerPackages: MutableList<InstalledPackageData>?

    withContext(StaticAnalysisDispatchers.IO) {
      installedComposerPackages = currentComposerConfig?.let { ComposerConfigUtils.getInstalledPackagesFromConfig(it) }

      if (currentComposerConfig != null) {
        val parseJson = ComposerConfigUtils.parseJson(currentComposerConfig)
        if (parseJson is JsonObject) {
          val obj: JsonObject = parseJson.getAsJsonObject()
          addPackages(specifiedInComposerPackages, obj["require"], false)
          addPackages(specifiedInComposerPackages, obj["require-dev"], true)
        }
      }
    }

    return if (installedComposerPackages != null) {
      val customRepos = readAction { ComposerConfigUtils.customRepos(currentComposerConfig) }

      ComposerPackagesDescription(
        installedComposerPackages.map { ComposerPackageDescription(it, specifiedInComposerPackages[it.name]) },
        customRepos
      )
    }
    else ComposerPackagesDescription(emptyList(), false)
  }

  @Suppress("unused")
  class ComposerPackagesDescription(val libraries: List<ComposerPackageDescription>, val customRepos: Boolean)

  @Suppress("unused")
  class ComposerPackageDescription(installed: InstalledPackageData, specified: SpecifiedPackageData?) {
    val fullName: String = installed.name
    val vendor: String? = StringUtil.substringBefore(installed.name, "/")
    val packageName: String? = StringUtil.substringAfter(installed.name, "/")
    val installedVersion: String = installed.version
    val directDependency: Boolean = specified != null
    val specifiedVersion: String? = specified?.version
    val specifiedRequired: Boolean? = specified?.required // location: require or require-dev section
  }

  class SpecifiedPackageData(val version: String, val required: Boolean)

  companion object {
    private fun addPackages(result: MutableMap<String, SpecifiedPackageData>, require: JsonElement?, isDev: Boolean) {
      if (require is JsonObject) {
        require.entrySet().forEach { (key, value) -> result.putIfAbsent(key, SpecifiedPackageData(value.toString(), isDev)) }
      }
    }
  }
}