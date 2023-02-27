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

import com.intellij.execution.lineMarker.ExecutorAction
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement
import com.intellij.util.Function
import org.intellij.terraform.hcl.HCLTokenTypes
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.config.patterns.TerraformPatterns

class TerraformRunLineMarkerContributor : RunLineMarkerContributor() {
  override fun getInfo(leaf: PsiElement): Info? {
    if (!HCLTokenTypes.IDENTIFYING_LITERALS.contains(leaf.node?.elementType)) return null

    val identifier = leaf.parent ?: return null

    val block = identifier.parent as? HCLBlock ?: return null

    if (block.nameIdentifier !== identifier) return null

    if (!TerraformPatterns.ResourceRootBlock.accepts(block)) return null

    TerraformResourceConfigurationProducer.getResourceTarget(block) ?: return null

    val actions = ExecutorAction.getActions(0)
    val tooltipProvider: Function<PsiElement, String> = Function { psiElement ->
      @Suppress("UselessCallOnCollection")
      actions.filterNotNull().mapNotNull { getText(it, psiElement) }.joinToString("\n")
    }
    return Info(AllIcons.RunConfigurations.TestState.Run, tooltipProvider, *actions)
  }
}