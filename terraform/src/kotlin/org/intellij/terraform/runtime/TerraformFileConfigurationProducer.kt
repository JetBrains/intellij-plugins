/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.terraform.runtime

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.ConfigurationFromContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import org.intellij.terraform.config.model.getTerraformModule
import org.intellij.terraform.config.patterns.TerraformPatterns
import org.intellij.terraform.firstOrNull
import org.intellij.terraform.hcl.psi.HCLElement

sealed class TerraformFileConfigurationProducer(private val type: Type) : LazyRunConfigurationProducer<TerraformRunConfiguration>() {
  enum class Type(val title: String, val factory: () -> ConfigurationFactory) {
    PLAN("Plan", { TerraformConfigurationType.getInstance().planFactory }),
    APPLY("Apply", { TerraformConfigurationType.getInstance().applyFactory }),
  }

  override fun getConfigurationFactory(): ConfigurationFactory {
    return type.factory()
  }

  override fun setupConfigurationFromContext(configuration: TerraformRunConfiguration, context: ConfigurationContext, sourceElement: Ref<PsiElement>): Boolean {
    val target = getModuleTarget(context) ?: return false
    configuration.workingDirectory = target.first
    configuration.name = type.title + " " + target.second
    configuration.setNameChangedByUser(false)
    return true
  }

  override fun isConfigurationFromContext(configuration: TerraformRunConfiguration, context: ConfigurationContext): Boolean {
    val target = getModuleTarget(context) ?: return false
    val wd = configuration.workingDirectory
    if (target.first != wd) return false
    if (configuration.name != type.title + " " + target.second) return false

    val parameters = configuration.programParameters ?: return true
    return !parameters.contains("-target")
  }

  override fun isPreferredConfiguration(self: ConfigurationFromContext?, other: ConfigurationFromContext?): Boolean {
    if (other == null) return true
    val configuration = other.configuration as? TerraformRunConfiguration ?: return true
    return configuration.programParameters?.contains("-target=") != true
  }

  companion object {
    class Plan : TerraformFileConfigurationProducer(Type.PLAN)
    class Apply : TerraformFileConfigurationProducer(Type.APPLY)

    fun getModuleTarget(context: ConfigurationContext): Pair<String, String>? {
      val location = context.location ?: return null
      val element = location.getAncestors(HCLElement::class.java, false).firstOrNull()?.psiElement ?: return null
      return getModuleTarget(element)
    }

    private fun getModuleTarget(element: PsiElement): Pair<String, String>? {
      if (element !is HCLElement) return null
      val file = element.containingFile.originalFile
      if (!TerraformPatterns.TerraformConfigFile.accepts(file)) return null

      val module = element.getTerraformModule()
      if (!module.item.isDirectory) {
        return null
      }

      val virtualFile = module.item.virtualFile
      return virtualFile.path to "directory ${virtualFile.name}"
    }
  }
}