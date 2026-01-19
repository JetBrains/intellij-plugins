// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.stack.component

import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.patterns.PsiFilePattern
import com.intellij.patterns.StandardPatterns
import org.intellij.terraform.config.Constants.HCL_COMPONENT_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_INPUTS_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_PROVIDERS_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_STACK_IDENTIFIER
import org.intellij.terraform.config.Constants.HCL_TERRAFORM_REQUIRED_PROVIDERS
import org.intellij.terraform.config.patterns.TfPsiPatterns
import org.intellij.terraform.config.patterns.TfPsiPatterns.propertyWithName
import org.intellij.terraform.hcl.patterns.HCLPatterns
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLFile
import org.intellij.terraform.hcl.psi.HCLProperty

internal object TfComponentPsiPatterns {
  val TfComponentFile: PsiFilePattern.Capture<HCLFile> = PlatformPatterns.psiFile(HCLFile::class.java)
    .withFileType(StandardPatterns.instanceOf(TfComponentFileType::class.java))

  val TfComponentRequiredProviders: PsiElementPattern.Capture<HCLBlock> = PlatformPatterns.psiElement(HCLBlock::class.java)
    .with(TfPsiPatterns.createBlockPattern(HCL_TERRAFORM_REQUIRED_PROVIDERS))
    .withParent(TfComponentFile)

  val TfComponentBlock: PsiElementPattern.Capture<HCLBlock> = PlatformPatterns.psiElement(HCLBlock::class.java)
    .with(TfPsiPatterns.createBlockPattern(HCL_COMPONENT_IDENTIFIER))
    .withParent(TfComponentFile)

  val TfStackBlock: PsiElementPattern.Capture<HCLBlock> = PlatformPatterns.psiElement(HCLBlock::class.java)
    .with(TfPsiPatterns.createBlockPattern(HCL_STACK_IDENTIFIER))
    .withParent(TfComponentFile)

  val TfComponentOrStackBlock: PsiElementPattern.Capture<HCLBlock> = PlatformPatterns.psiElement(HCLBlock::class.java)
    .andOr(TfComponentBlock, TfStackBlock)

  val InputsPropertyOfComponent: PsiElementPattern.Capture<HCLProperty> = propertyWithName(HCL_INPUTS_IDENTIFIER)
    .withParent(HCLPatterns.Object)
    .withSuperParent(2, TfComponentBlock)

  val ProvidersPropertyOfComponent: PsiElementPattern.Capture<HCLProperty> = propertyWithName(HCL_PROVIDERS_IDENTIFIER)
    .withParent(HCLPatterns.Object)
    .withSuperParent(2, TfComponentBlock)
}