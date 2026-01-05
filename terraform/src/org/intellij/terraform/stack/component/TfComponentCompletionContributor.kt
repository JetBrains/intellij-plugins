// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.stack.component

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.openapi.project.DumbAware
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.patterns.PsiFilePattern
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.PsiFile
import org.intellij.terraform.config.Constants.HCL_TERRAFORM_REQUIRED_PROVIDERS
import org.intellij.terraform.config.patterns.TfPsiPatterns.createBlockPattern
import org.intellij.terraform.hcl.codeinsight.HclBlockPropertiesCompletionProvider
import org.intellij.terraform.hcl.codeinsight.HclRequiredProvidersCompletion
import org.intellij.terraform.hcl.codeinsight.HclRootBlockCompletionProvider
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLFile

internal class TfComponentCompletionContributor : CompletionContributor(), DumbAware {
  init {
    HclRootBlockCompletionProvider.registerTo(this, TfComponentFile)
    HclBlockPropertiesCompletionProvider.registerTo(this, TfComponentFile)

    HclRequiredProvidersCompletion.registerTo(this, TfComponentRequiredProviders)
  }
}

internal val TfComponentFile: PsiFilePattern.Capture<HCLFile> = PlatformPatterns.psiFile(HCLFile::class.java)
  .withFileType(StandardPatterns.instanceOf(TfComponentFileType::class.java))

internal val TfComponentRequiredProviders: PsiElementPattern.Capture<HCLBlock> = PlatformPatterns.psiElement(HCLBlock::class.java)
  .with(createBlockPattern(HCL_TERRAFORM_REQUIRED_PROVIDERS))
  .withParent(TfComponentFile)

internal fun isTfComponentPsiFile(file: PsiFile?): Boolean = file?.fileType == TfComponentFileType