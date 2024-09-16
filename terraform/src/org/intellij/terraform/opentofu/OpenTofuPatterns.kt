// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.opentofu

import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import org.intellij.terraform.config.patterns.TerraformPatterns.TerraformRootBlock
import org.intellij.terraform.config.patterns.TerraformPatterns.createBlockPattern
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLIdentifier
import org.intellij.terraform.hcl.psi.HCLProperty
import org.intellij.terraform.hcl.psi.HCLSelectExpression
import org.intellij.terraform.opentofu.OpenTofuConstants.TOFU_ENCRYPTION
import org.intellij.terraform.opentofu.OpenTofuConstants.TOFU_ENCRYPTION_METHOD_BLOCK
import org.intellij.terraform.opentofu.OpenTofuConstants.TOFU_ENCRYPTION_METHOD_PROPERTY
import org.intellij.terraform.opentofu.OpenTofuConstants.TOFU_FALLBACK_BLOCK
import org.intellij.terraform.opentofu.OpenTofuConstants.TOFU_KEYS_PROPERTY
import org.intellij.terraform.opentofu.OpenTofuConstants.TOFU_KEY_PROVIDER
import org.intellij.terraform.opentofu.OpenTofuConstants.TOFU_PLAN_BLOCK
import org.intellij.terraform.opentofu.OpenTofuConstants.TOFU_STATE_BLOCK

object OpenTofuPatterns {

  val EncryptionBlock: PsiElementPattern.Capture<HCLBlock> =
    PlatformPatterns.psiElement(HCLBlock::class.java)
      .with(createBlockPattern(TOFU_ENCRYPTION))
      .withSuperParent(2, TerraformRootBlock)

  val KeyProviderBlock: PsiElementPattern.Capture<HCLBlock> =
    PlatformPatterns.psiElement(HCLBlock::class.java)
      .with(createBlockPattern(TOFU_KEY_PROVIDER))
      .withSuperParent(2, EncryptionBlock)

  val EncryptionMethodBlock: PsiElementPattern.Capture<HCLBlock> =
    PlatformPatterns.psiElement(HCLBlock::class.java)
      .with(createBlockPattern(TOFU_ENCRYPTION_METHOD_BLOCK))
      .withSuperParent(2, EncryptionBlock)

  val EncryptionMethodKeysProperty: PsiElementPattern.Capture<HCLProperty> = PlatformPatterns.psiElement(HCLProperty::class.java)
    .withSuperParent(2, EncryptionMethodBlock)
    .withName(TOFU_KEYS_PROPERTY)

  val EncryptionMethodKeysPropertyValue: PsiElementPattern.Capture<HCLIdentifier> =
    PlatformPatterns.psiElement(HCLIdentifier::class.java)
      .withParent(HCLSelectExpression::class.java)
      .withSuperParent(2, EncryptionMethodKeysProperty)

  val EncryptionStateBlock: PsiElementPattern.Capture<HCLBlock> =
    PlatformPatterns.psiElement(HCLBlock::class.java)
      .with(createBlockPattern(TOFU_STATE_BLOCK))
      .withSuperParent(2, EncryptionBlock)

  val EncryptionPlanBlock: PsiElementPattern.Capture<HCLBlock> =
    PlatformPatterns.psiElement(HCLBlock::class.java)
      .with(createBlockPattern(TOFU_PLAN_BLOCK))
      .withSuperParent(2, EncryptionBlock)

  val EncryptionFallbackMethodBlock: PsiElementPattern.Capture<HCLBlock> =
    PlatformPatterns.psiElement(HCLBlock::class.java)
      .with(createBlockPattern(TOFU_FALLBACK_BLOCK))
      .andOr(
        PlatformPatterns.psiElement(HCLBlock::class.java).withSuperParent(2, EncryptionStateBlock),
        PlatformPatterns.psiElement(HCLBlock::class.java).withSuperParent(2, EncryptionPlanBlock))

  val EncryptionMethodProperty: PsiElementPattern.Capture<HCLProperty> = PlatformPatterns.psiElement(HCLProperty::class.java)
    .andOr(PlatformPatterns.psiElement(HCLProperty::class.java).withSuperParent(2, EncryptionStateBlock),
           PlatformPatterns.psiElement(HCLProperty::class.java).withSuperParent(2, EncryptionPlanBlock),
           PlatformPatterns.psiElement(HCLProperty::class.java).withSuperParent(2, EncryptionFallbackMethodBlock))
    .withName(TOFU_ENCRYPTION_METHOD_PROPERTY)

  val EncryptionMethodPropertyValue: PsiElementPattern.Capture<HCLIdentifier> =
    PlatformPatterns.psiElement(HCLIdentifier::class.java)
      .withParent(HCLSelectExpression::class.java)
      .withSuperParent(2, EncryptionMethodProperty)

}