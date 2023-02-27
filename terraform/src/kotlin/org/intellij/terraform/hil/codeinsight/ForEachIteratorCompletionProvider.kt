/*
 * Copyright 2000-2019 JetBrains s.r.o.
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
package org.intellij.terraform.hil.codeinsight

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parentsOfType
import com.intellij.util.ProcessingContext
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLIdentifier
import org.intellij.terraform.hcl.psi.HCLProperty
import org.intellij.terraform.hcl.psi.common.Identifier
import org.intellij.terraform.hcl.psi.common.SelectExpression
import org.intellij.terraform.config.codeinsight.TerraformConfigCompletionContributor
import org.intellij.terraform.config.patterns.TerraformPatterns
import org.intellij.terraform.hil.psi.impl.getHCLHost

object ForEachIteratorCompletionProvider : TerraformConfigCompletionContributor.OurCompletionProvider() {
  override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
    val id = parameters.position.parent as? Identifier ?: return
    val host = id.getHCLHost() ?: return

    val parent = id.parent
    if (parent is SelectExpression<*>) {
      if (parent.from !== id) return
    }

    if (PsiTreeUtil.findFirstParent(host) { TerraformPatterns.DynamicBlockContent.accepts(it) } == null) {
      // Either in 'content' or in 'labels'
      val labels = PsiTreeUtil.findFirstParent(host) { TerraformPatterns.DynamicLabels.accepts(it) } as? HCLProperty
          ?: return
      if (!PsiTreeUtil.isAncestor(labels.value, host, false)) return
    }

    val dynamics = host.parentsOfType(HCLBlock::class.java).filter { TerraformPatterns.DynamicBlock.accepts(it) }
    for (dynamic in dynamics) {
      val iteratorPropertyValue = dynamic.`object`?.findProperty("iterator")?.value as? HCLIdentifier
      val iterator = iteratorPropertyValue?.id ?: dynamic.name
      result.addElement(HILCompletionContributor.create(iterator))
    }
    return
  }
}