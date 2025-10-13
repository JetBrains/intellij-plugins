// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.terragrunt.codeinsight

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.openapi.project.DumbAware
import org.intellij.terraform.hcl.codeinsight.AfterCommaOrBracketPattern
import org.intellij.terraform.hcl.codeinsight.HclBlockPropertiesCompletionProvider
import org.intellij.terraform.hcl.codeinsight.HclBlockPropertiesCompletionProvider.createBlockPropertyKeyPattern
import org.intellij.terraform.hcl.codeinsight.HclBlockPropertiesCompletionProvider.createNestedBlockPropertyPattern
import org.intellij.terraform.hcl.codeinsight.HclBlockPropertiesCompletionProvider.createPropertyInBlockPattern
import org.intellij.terraform.hcl.codeinsight.HclKeywordsCompletionProvider
import org.intellij.terraform.hcl.codeinsight.HclRootBlockCompletionProvider
import org.intellij.terraform.hcl.codeinsight.HclRootBlockCompletionProvider.createBlockHeaderPattern
import org.intellij.terraform.hcl.codeinsight.HclRootBlockCompletionProvider.createRootBlockPattern
import org.intellij.terraform.terragrunt.patterns.TerragruntPsiPatterns.TerragruntFile
import org.intellij.terraform.terragrunt.patterns.TerragruntPsiPatterns.TerragruntMethodPosition

internal class TerragruntCompletionContributor : CompletionContributor(), DumbAware {
  init {
    extend(CompletionType.BASIC, AfterCommaOrBracketPattern, HclKeywordsCompletionProvider)

    extend(CompletionType.BASIC, createRootBlockPattern(TerragruntFile), HclRootBlockCompletionProvider)
    extend(CompletionType.BASIC, createBlockHeaderPattern(TerragruntFile), HclRootBlockCompletionProvider)

    extend(CompletionType.BASIC, createBlockPropertyKeyPattern(TerragruntFile), HclBlockPropertiesCompletionProvider)
    extend(CompletionType.BASIC, createPropertyInBlockPattern(TerragruntFile), HclBlockPropertiesCompletionProvider)
    extend(CompletionType.BASIC, createNestedBlockPropertyPattern(TerragruntFile), HclBlockPropertiesCompletionProvider)

    extend(CompletionType.BASIC, TerragruntMethodPosition, TerragruntMethodCompletionProvider)
  }
}