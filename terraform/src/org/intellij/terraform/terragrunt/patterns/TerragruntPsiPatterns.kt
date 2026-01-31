// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.terragrunt.patterns

import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern.Capture
import com.intellij.patterns.PsiFilePattern
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.PsiElement
import org.intellij.terraform.config.patterns.TfPsiPatterns.createBlockPattern
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLElement
import org.intellij.terraform.hcl.psi.HCLFile
import org.intellij.terraform.hil.patterns.HILPatterns.getMethodIdentifierPattern
import org.intellij.terraform.terragrunt.TERRAGRUNT_DEPENDENCY
import org.intellij.terraform.terragrunt.TERRAGRUNT_FEATURE
import org.intellij.terraform.terragrunt.TERRAGRUNT_GENERATE
import org.intellij.terraform.terragrunt.TERRAGRUNT_INCLUDE
import org.intellij.terraform.terragrunt.TERRAGRUNT_STACK
import org.intellij.terraform.terragrunt.TERRAGRUNT_STACK_FILE
import org.intellij.terraform.terragrunt.TERRAGRUNT_UNIT
import org.intellij.terraform.terragrunt.TerragruntFileType

internal object TerragruntPsiPatterns {
  val TerragruntFile: PsiFilePattern.Capture<HCLFile> = PlatformPatterns.psiFile(HCLFile::class.java)
    .withFileType(StandardPatterns.instanceOf(TerragruntFileType::class.java))

  val StackFile: PsiFilePattern.Capture<HCLFile> = TerragruntFile.withName(StandardPatterns.string().equalTo(TERRAGRUNT_STACK_FILE))

  val TerragruntMethodPosition: Capture<PsiElement> = getMethodIdentifierPattern(
    PlatformPatterns.psiElement(HCLElement::class.java)
      .inFile(TerragruntFile)
  )

  val IncludeBlockPattern: Capture<HCLBlock> = getTerragruntRootBlockPattern(TERRAGRUNT_INCLUDE)
  val DependencyBlockPattern: Capture<HCLBlock> = getTerragruntRootBlockPattern(TERRAGRUNT_DEPENDENCY)
  val GenerateBlockPattern: Capture<HCLBlock> = getTerragruntRootBlockPattern(TERRAGRUNT_GENERATE)
  val FeatureBlockPattern: Capture<HCLBlock> = getTerragruntRootBlockPattern(TERRAGRUNT_FEATURE)

  val StackBlockPattern: Capture<HCLBlock> = getStackRootBlockPattern(TERRAGRUNT_STACK)
  val UnitBlockPattern: Capture<HCLBlock> = getStackRootBlockPattern(TERRAGRUNT_UNIT)

  fun getTerragruntRootBlockPattern(identifier: String) = PlatformPatterns.psiElement(HCLBlock::class.java)
    .withParent(TerragruntFile)
    .with(createBlockPattern(identifier))

  fun getStackRootBlockPattern(identifier: String) = PlatformPatterns.psiElement(HCLBlock::class.java)
    .withParent(StackFile)
    .with(createBlockPattern(identifier))
}