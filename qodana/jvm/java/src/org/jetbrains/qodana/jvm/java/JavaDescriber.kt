// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.qodana.jvm.java

import com.intellij.openapi.application.readAction
import com.intellij.openapi.module.LanguageLevelUtil
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import org.jetbrains.qodana.staticAnalysis.projectDescription.QodanaProjectDescriber

class JavaDescriber : QodanaProjectDescriber {
  override val id: String = "Java"

  override suspend fun description(project: Project): ModulesDescriptionList {
    val modules = ModuleManager.getInstance(project).modules
    return ModulesDescriptionList(modules.map { computeModulesDescription(it) })
  }

  class ModulesDescriptionList(val modules: List<ModulesDescription>)

  @Suppress("unused")
  class ModulesDescription(val name: String, val languageLevel: Int)

  private suspend fun computeModulesDescription(module: Module): ModulesDescription {
    val languageLevel = readAction {
      LanguageLevelUtil.getEffectiveLanguageLevel(module).feature()
    }
    return ModulesDescription(module.name, languageLevel)
  }
}
