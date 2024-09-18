// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.opentofu.codeinsight

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import org.intellij.terraform.TerraformIcons
import org.intellij.terraform.hcl.psi.common.Identifier
import org.intellij.terraform.hcl.psi.getNameElementUnquoted
import org.intellij.terraform.hil.codeinsight.HILCompletionContributor.SelectFromScopeCompletionProvider
import org.intellij.terraform.opentofu.OpenTofuConstants.TOFU_KEY_PROVIDER

internal object KeyProvidersCompletionProvider : SelectFromScopeCompletionProvider(TOFU_KEY_PROVIDER) {
  override fun doAddCompletions(variable: Identifier,
                                parameters: CompletionParameters,
                                context: ProcessingContext,
                                result: CompletionResultSet) {
    result.addAllElements(getKeyProvidersTypeNames(parameters.position))
  }

  private fun getKeyProvidersTypeNames(element: PsiElement): List<LookupElement> {
    return OpenTofuCompletionUtil.findEncryptionKeyProviders(element)?.map {
      LookupElementBuilder.create(it.getNameElementUnquoted(1)!!).withIcon(TerraformIcons.CollectionKey)
    } ?: return emptyList()
  }
}


