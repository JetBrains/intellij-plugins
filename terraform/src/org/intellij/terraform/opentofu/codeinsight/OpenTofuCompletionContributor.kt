// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.opentofu.codeinsight

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.DumbAware
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import org.intellij.terraform.TerraformIcons
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.common.Identifier
import org.intellij.terraform.hcl.psi.getNameElementUnquoted
import org.intellij.terraform.hil.codeinsight.HILCompletionContributor.SelectFromScopeCompletionProvider
import org.intellij.terraform.opentofu.OpenTofuConstants.TOFU_ENCRYPTION_METHOD_BLOCK
import org.intellij.terraform.opentofu.OpenTofuConstants.TOFU_ENCRYPTION_METHOD_PROPERTY
import org.intellij.terraform.opentofu.OpenTofuConstants.TOFU_KEY_PROVIDER
import org.intellij.terraform.opentofu.patterns.OpenTofuPatterns.EncryptionMethodBlock
import org.intellij.terraform.opentofu.patterns.OpenTofuPatterns.EncryptionMethodEmptyValue
import org.intellij.terraform.opentofu.patterns.OpenTofuPatterns.EncryptionMethodKeysEmptyValue
import org.intellij.terraform.opentofu.patterns.OpenTofuPatterns.KeyProviderBlock

class OpenTofuCompletionContributor : CompletionContributor(), DumbAware {
  init {
    extend(CompletionType.BASIC, EncryptionMethodKeysEmptyValue, object: CompletionProvider<CompletionParameters>() {
      override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        result.addElement(LookupElementBuilder.create(TOFU_KEY_PROVIDER))
      }
    })
    extend(CompletionType.BASIC, EncryptionMethodEmptyValue, object: CompletionProvider<CompletionParameters>() {
      override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        result.addElement(LookupElementBuilder.create(TOFU_ENCRYPTION_METHOD_PROPERTY))
      }
    })
  }
}

internal object KeyProvidersCompletionProvider : SelectFromScopeCompletionProvider(TOFU_KEY_PROVIDER) {
  override fun doAddCompletions(variable: Identifier, parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
    result.addAllElements(getEncryptionElementsTypeNames(parameters.position, KeyProviderBlock))
  }
}

internal object EncryptionMethodsCompletionProvider : SelectFromScopeCompletionProvider(TOFU_ENCRYPTION_METHOD_BLOCK) {
  override fun doAddCompletions(variable: Identifier, parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
    result.addAllElements(getEncryptionElementsTypeNames(parameters.position, EncryptionMethodBlock))
  }
}

private fun getEncryptionElementsTypeNames(element: PsiElement, pattern: PsiElementPattern.Capture<HCLBlock>): List<LookupElement> {
  return findEncryptionBlockElements(element, pattern).map {
    LookupElementBuilder.create(it.getNameElementUnquoted(1)!!).withIcon(TerraformIcons.CollectionKey)
  }
}
