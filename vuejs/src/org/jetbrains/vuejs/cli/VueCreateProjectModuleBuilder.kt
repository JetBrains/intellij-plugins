// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.jetbrains.vuejs.cli

import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.SettingsStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.lang.javascript.boilerplate.NpmPackageProjectGenerator
import com.intellij.openapi.module.*
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import icons.VuejsIcons
import org.jetbrains.vuejs.VueBundle
import java.nio.file.Files
import java.nio.file.Paths
import javax.swing.Icon
import javax.swing.JComponent

class VueCreateProjectModuleBuilder(generator: VueCliProjectGenerator) : WebModuleBuilder<NpmPackageProjectGenerator.Settings>(generator) {
  private var generatorStep: VueRunningGeneratorStep? = null
  private var callOnProjectCreated: ((Project) -> Unit)? = null

  override fun commit(project: Project, model: ModifiableModuleModel?, modulesProvider: ModulesProvider?): MutableList<Module>? {
    callOnProjectCreated?.invoke(project)
    return super.commit(project, model, modulesProvider)
  }

  override fun createFinishingSteps(wizardContext: WizardContext, modulesProvider: ModulesProvider): Array<ModuleWizardStep> {
    generatorStep = VueRunningGeneratorStep(wizardContext.wizard, this)
    return arrayOf(generatorStep!!)
  }

  override fun modifySettingsStep(settingsStep: SettingsStep): ModuleWizardStep? {
    val peer = myGeneratorPeerLazyValue.value
    peer.buildUI(settingsStep)

    val context = settingsStep.context
    context.wizard.updateButtons(false, true, true)

    return object : ModuleWizardStep() {
      override fun getComponent(): JComponent? {
        return null
      }

      override fun updateDataModel() {
        val projectPath = context.projectFileDirectory
        val modulePath = settingsStep.moduleNameLocationSettings?.moduleContentRoot ?: projectPath
        generatorStep?.startGeneration(modulePath.trim(), peer.settings)
      }

      @Throws(ConfigurationException::class)
      override fun validate(): Boolean {
        val info = myGeneratorPeerLazyValue.value.validate()
        if (info != null) throw ConfigurationException(info.message)

        val text = settingsStep.moduleNameLocationSettings?.moduleContentRoot
        if (text != null) {
          val path = Paths.get(text)
          if (Files.exists(path) && path.toFile().list().isNotEmpty()) {
            throw ConfigurationException(
              VueBundle.message("vue.project.generator.module.location.already.exists"))
          }
        }
        return true
      }
    }
  }

  fun registerProjectCreatedCallback(callback: (Project) -> Unit) {
    callOnProjectCreated = callback
  }

  override fun getName(): String {
    return "Vue.js"
  }

  override fun getPresentableName(): String {
    return "Vue.js"
  }

  override fun getDescription(): String {
    return VueBundle.message("vue.project.generator.description")
  }

  override fun getBuilderId(): String? {
    return "Vue.js"
  }

  override fun getNodeIcon(): Icon {
    return VuejsIcons.Vue
  }

  override fun getModuleType(): ModuleType<*> {
    return WebModuleType.getInstance()
  }

  override fun getParentGroup(): String {
    return WebModuleBuilder.GROUP_NAME
  }
}