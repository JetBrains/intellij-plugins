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
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.intellij.terraform.config.patterns.TerraformPatterns
import org.intellij.terraform.firstOrNull
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLElement
import org.intellij.terraform.hcl.psi.getNameElementUnquoted

sealed class TerraformResourceConfigurationProducer(private val type: Type) : LazyRunConfigurationProducer<TerraformRunConfiguration>() {
  enum class Type(val title: String, val factory: () -> ConfigurationFactory) {
    PLAN("Plan", { TerraformConfigurationType.getInstance().planFactory }),
    APPLY("Apply", { TerraformConfigurationType.getInstance().applyFactory }),
  }

  override fun getConfigurationFactory(): ConfigurationFactory {
    return type.factory()
  }

  override fun setupConfigurationFromContext(configuration: TerraformRunConfiguration, context: ConfigurationContext, sourceElement: Ref<PsiElement>): Boolean {
    val target = getResourceTarget(context) ?: return false
    if (configuration.programParameters == null) {
      configuration.programParameters = "-target=$target"
    } else {
      configuration.programParameters += " -target=$target"
    }
    configuration.workingDirectory = context.location?.virtualFile?.parent?.path ?: context.project.basePath
    configuration.name = type.title + " " + target
    configuration.setNameChangedByUser(false)
    return true
  }

  override fun isConfigurationFromContext(configuration: TerraformRunConfiguration, context: ConfigurationContext): Boolean {
    val parameters = configuration.programParameters ?: return false
    if (!parameters.contains("-target=")) return false
    val target = getResourceTarget(context) ?: return false

    val targets = parameters.split(' ').filter { it.startsWith("-target=") }.map { it.removePrefix("-target=") }.filter { !it.isBlank() }
    if (targets.isEmpty()) return false

    return targets.contains(target) && configuration.name == type.title + " " + target
  }

  companion object {
    class Plan : TerraformResourceConfigurationProducer(Type.PLAN)
    class Apply : TerraformResourceConfigurationProducer(Type.APPLY)

    fun getResourceTarget(context: ConfigurationContext): String? {
      val location = context.location ?: return null
      val element = location.getAncestors(HCLElement::class.java, false).firstOrNull()?.psiElement ?: return null
      return getResourceTarget(element)
    }

    fun getResourceTarget(element: PsiElement): String? {
      val block = PsiTreeUtil.getTopmostParentOfType(element, HCLBlock::class.java) ?: return null

      if (!TerraformPatterns.RootBlock.accepts(block)) return null

      // Only 'resource' blocks supported for now
      if (!TerraformPatterns.ResourceRootBlock.accepts(block)) return null

      return block.getNameElementUnquoted(1) + "." + block.name
    }
  }
}

