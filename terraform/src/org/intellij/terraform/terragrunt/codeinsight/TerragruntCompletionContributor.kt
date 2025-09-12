// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.terragrunt.codeinsight

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.openapi.project.DumbAware
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.StandardPatterns
import org.intellij.terraform.hcl.codeinsight.AfterCommaOrBracketPattern
import org.intellij.terraform.hcl.codeinsight.HclKeywordsCompletionProvider
import org.intellij.terraform.hcl.codeinsight.HclRootBlockCompletionProvider
import org.intellij.terraform.hcl.codeinsight.HclRootBlockCompletionProvider.createBlockHeaderPattern
import org.intellij.terraform.hcl.codeinsight.HclRootBlockCompletionProvider.createRootBlockPattern
import org.intellij.terraform.hcl.psi.HCLFile
import org.intellij.terraform.terragrunt.TerragruntFileType

internal class TerragruntCompletionContributor : CompletionContributor(), DumbAware {
  init {
    extend(CompletionType.BASIC, AfterCommaOrBracketPattern, HclKeywordsCompletionProvider)

    extend(CompletionType.BASIC, createRootBlockPattern(TerragruntConfigFile), HclRootBlockCompletionProvider)
    extend(CompletionType.BASIC, createBlockHeaderPattern(TerragruntConfigFile), HclRootBlockCompletionProvider)
  }
}

private val TerragruntConfigFile = PlatformPatterns.psiFile(HCLFile::class.java)
  .withFileType(StandardPatterns.instanceOf(TerragruntFileType::class.java))