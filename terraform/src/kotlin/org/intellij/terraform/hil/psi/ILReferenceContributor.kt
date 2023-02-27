/*
 * Copyright 2000-2016 JetBrains s.r.o.
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
package org.intellij.terraform.hil.psi

import com.intellij.patterns.PlatformPatterns.or
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import org.intellij.terraform.hcl.psi.common.Identifier
import org.intellij.terraform.hcl.psi.withHCLHost
import org.intellij.terraform.config.patterns.TerraformPatterns
import org.intellij.terraform.hil.codeinsight.HILCompletionContributor

class ILReferenceContributor : PsiReferenceContributor() {
  override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
    registrar.registerReferenceProvider(psiElement(Identifier::class.java)
        .withParent(HILCompletionContributor.ILSE_FROM_KNOWN_SCOPE), ILSelectFromScopeReferenceProvider)

    registrar.registerReferenceProvider(psiElement(Identifier::class.java)
        .withParent(HILCompletionContributor.ILSE_NOT_FROM_KNOWN_SCOPE), ILSelectFromSomethingReferenceProvider)

    registrar.registerReferenceProvider(psiElement(Identifier::class.java)
        .withParent(HILCompletionContributor.ILSE_FROM_KNOWN_SCOPE), ILScopeReferenceProvider)

    // ForExpression variable
    registrar.registerReferenceProvider(psiElement(Identifier::class.java)
        .and(HILCompletionContributor.INSIDE_FOR_EXPRESSION_BODY), ForVariableReferenceProvider2())

    // 'dynamic' 'content' block reference
    registrar.registerReferenceProvider(psiElement(Identifier::class.java)
        .with(HILCompletionContributor.IS_SE_FROM_CONDITION)
        .withHCLHost(psiElement().inside(TerraformPatterns.DynamicBlock)), DynamicBlockVariableReferenceProvider)

    // 'each' in resource or data source
    registrar.registerReferenceProvider(psiElement(Identifier::class.java)
        .withText("each")
        .with(HILCompletionContributor.IS_SE_FROM_CONDITION)
        .withHCLHost(psiElement().inside(true, or(TerraformPatterns.ResourceRootBlock, TerraformPatterns.DataSourceRootBlock, TerraformPatterns.ModuleRootBlock))), ResourceEachVariableReferenceProvider)
  }
}
