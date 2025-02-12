// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.opentofu.patterns

import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.util.ProcessingContext
import org.intellij.terraform.config.patterns.TfPsiPatterns
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLIdentifier
import org.intellij.terraform.hcl.psi.HCLProperty
import org.intellij.terraform.hcl.psi.HCLSelectExpression
import org.intellij.terraform.hcl.psi.common.SelectExpression
import org.intellij.terraform.hil.patterns.HILPatterns
import org.intellij.terraform.opentofu.OpenTofuConstants

object OpenTofuPatterns {

  val EncryptionBlock: PsiElementPattern.Capture<HCLBlock> =
    PlatformPatterns.psiElement(HCLBlock::class.java)
      .with(TfPsiPatterns.createBlockPattern(OpenTofuConstants.TOFU_ENCRYPTION))
      .withSuperParent(2, TfPsiPatterns.TerraformRootBlock)

  val KeyProviderBlock: PsiElementPattern.Capture<HCLBlock> =
    PlatformPatterns.psiElement(HCLBlock::class.java)
      .with(TfPsiPatterns.createBlockPattern(OpenTofuConstants.TOFU_KEY_PROVIDER))
      .withSuperParent(2, EncryptionBlock)

  val EncryptionMethodBlock: PsiElementPattern.Capture<HCLBlock> =
    PlatformPatterns.psiElement(HCLBlock::class.java)
      .with(TfPsiPatterns.createBlockPattern(OpenTofuConstants.TOFU_ENCRYPTION_METHOD_BLOCK))
      .withSuperParent(2, EncryptionBlock)

  val EncryptionMethodKeysProperty: PsiElementPattern.Capture<HCLProperty> = PlatformPatterns.psiElement(HCLProperty::class.java)
    .withSuperParent(2, EncryptionMethodBlock)
    .withName(OpenTofuConstants.TOFU_KEYS_PROPERTY)

  val EncryptionMethodKeysPropertyValue: PsiElementPattern.Capture<HCLIdentifier> =
    PlatformPatterns.psiElement(HCLIdentifier::class.java)
      .withParent(HCLSelectExpression::class.java)
      .withSuperParent(2, EncryptionMethodKeysProperty)

  val EncryptionMethodKeysEmptyValue: PsiElementPattern.Capture<LeafPsiElement> =
    PlatformPatterns.psiElement(LeafPsiElement::class.java)
      .withParent(HCLIdentifier::class.java)
      .withSuperParent(2, EncryptionMethodKeysProperty)

  val EncryptionStateBlock: PsiElementPattern.Capture<HCLBlock> =
    PlatformPatterns.psiElement(HCLBlock::class.java)
      .with(TfPsiPatterns.createBlockPattern(OpenTofuConstants.TOFU_STATE_BLOCK))
      .withSuperParent(2, EncryptionBlock)

  val EncryptionPlanBlock: PsiElementPattern.Capture<HCLBlock> =
    PlatformPatterns.psiElement(HCLBlock::class.java)
      .with(TfPsiPatterns.createBlockPattern(OpenTofuConstants.TOFU_PLAN_BLOCK))
      .withSuperParent(2, EncryptionBlock)

  val EncryptionFallbackMethodBlock: PsiElementPattern.Capture<HCLBlock> =
    PlatformPatterns.psiElement(HCLBlock::class.java)
      .with(TfPsiPatterns.createBlockPattern(OpenTofuConstants.TOFU_FALLBACK_BLOCK))
      .andOr(
        PlatformPatterns.psiElement(HCLBlock::class.java).withSuperParent(2, EncryptionStateBlock),
        PlatformPatterns.psiElement(HCLBlock::class.java).withSuperParent(2, EncryptionPlanBlock))

  val EncryptionMethodProperty: PsiElementPattern.Capture<HCLProperty> = PlatformPatterns.psiElement(HCLProperty::class.java)
    .andOr(PlatformPatterns.psiElement(HCLProperty::class.java).withSuperParent(2, EncryptionStateBlock),
           PlatformPatterns.psiElement(HCLProperty::class.java).withSuperParent(2, EncryptionPlanBlock),
           PlatformPatterns.psiElement(HCLProperty::class.java).withSuperParent(2, EncryptionFallbackMethodBlock))
    .withName(OpenTofuConstants.TOFU_ENCRYPTION_METHOD_PROPERTY)

  val EncryptionMethodPropertyValue: PsiElementPattern.Capture<HCLIdentifier> =
    PlatformPatterns.psiElement(HCLIdentifier::class.java)
      .withParent(HCLSelectExpression::class.java)
      .withSuperParent(2, EncryptionMethodProperty)

  val EncryptionMethodEmptyValue: PsiElementPattern.Capture<LeafPsiElement> =
    PlatformPatterns.psiElement(LeafPsiElement::class.java)
      .withParent(HCLIdentifier::class.java)
      .withSuperParent(2, EncryptionMethodProperty)


  private val IlseFromKeyProvider: PsiElementPattern.Capture<SelectExpression<*>> = PlatformPatterns.psiElement(SelectExpression::class.java)
    .with(HILPatterns.getScopeSelectPatternCondition(setOf(OpenTofuConstants.TOFU_KEY_PROVIDER)))

  internal val IlseOpenTofuKeyProvider: PsiElementPattern.Capture<SelectExpression<*>> = PlatformPatterns.psiElement(SelectExpression::class.java)
    .with(object : PatternCondition<SelectExpression<*>?>(" SE_Key_Provider()") {
      override fun accepts(t: SelectExpression<*>, context: ProcessingContext): Boolean {
        val from = t.from as? SelectExpression<*> ?: return false
        return IlseFromKeyProvider.accepts(from)
      }
    })

  private val IlseFromEncryptionMethod: PsiElementPattern.Capture<SelectExpression<*>> = PlatformPatterns.psiElement(SelectExpression::class.java)
    .with(HILPatterns.getScopeSelectPatternCondition(setOf(OpenTofuConstants.TOFU_ENCRYPTION_METHOD_PROPERTY)))

  internal val IlseOpenTofuEncryptionMethod: PsiElementPattern.Capture<SelectExpression<*>> = PlatformPatterns.psiElement(SelectExpression::class.java)
    .with(object : PatternCondition<SelectExpression<*>?>(" SE_Encryption_Method()") {
      override fun accepts(t: SelectExpression<*>, context: ProcessingContext): Boolean {
        val from = t.from as? SelectExpression<*> ?: return false
        return IlseFromEncryptionMethod.accepts(from)
      }
    })


}