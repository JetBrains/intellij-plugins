// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.stack.component

import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.patterns.PsiFilePattern
import com.intellij.patterns.StandardPatterns
import org.intellij.terraform.config.Constants
import org.intellij.terraform.config.patterns.TfPsiPatterns
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLFile

internal object TfComponentPsiPatterns {
  val TfComponentFile: PsiFilePattern.Capture<HCLFile> = PlatformPatterns.psiFile(HCLFile::class.java)
    .withFileType(StandardPatterns.instanceOf(TfComponentFileType::class.java))

  val TfComponentRequiredProviders: PsiElementPattern.Capture<HCLBlock> = PlatformPatterns.psiElement(HCLBlock::class.java)
    .with(TfPsiPatterns.createBlockPattern(Constants.HCL_TERRAFORM_REQUIRED_PROVIDERS))
    .withParent(TfComponentFile)

  val TfComponentBlock: PsiElementPattern.Capture<HCLBlock> = PlatformPatterns.psiElement(HCLBlock::class.java)
    .with(TfPsiPatterns.createBlockPattern(COMPONENT_IDENTIFIER))
    .withParent(TfComponentFile)

  val TfStackBlock: PsiElementPattern.Capture<HCLBlock> = PlatformPatterns.psiElement(HCLBlock::class.java)
    .with(TfPsiPatterns.createBlockPattern(Constants.HCL_STACK_IDENTIFIER))
    .withParent(TfComponentFile)

  val TfComponentOrStackBlock: PsiElementPattern.Capture<HCLBlock> = PlatformPatterns.psiElement(HCLBlock::class.java)
    .andOr(TfComponentBlock, TfStackBlock)
}