// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.test.patterns

import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.patterns.PsiFilePattern
import com.intellij.patterns.StandardPatterns
import org.intellij.terraform.config.Constants.HCL_MODULE_IDENTIFIER
import org.intellij.terraform.config.patterns.TfPsiPatterns.createBlockPattern
import org.intellij.terraform.config.patterns.TfPsiPatterns.propertyWithName
import org.intellij.terraform.hcl.patterns.HCLPatterns
import org.intellij.terraform.hcl.psi.HCLBlock
import org.intellij.terraform.hcl.psi.HCLFile
import org.intellij.terraform.hcl.psi.HCLObject
import org.intellij.terraform.hcl.psi.HCLProperty
import org.intellij.terraform.test.HCL_DEFAULTS_IDENTIFIER
import org.intellij.terraform.test.HCL_MOCK_DATA_IDENTIFIER
import org.intellij.terraform.test.HCL_MOCK_PROVIDER_IDENTIFIER
import org.intellij.terraform.test.HCL_MOCK_RESOURCE_IDENTIFIER
import org.intellij.terraform.test.HCL_RUN_IDENTIFIER
import org.intellij.terraform.test.HCL_VARIABLES_IDENTIFIER
import org.intellij.terraform.test.TfTestFileType

internal object TfTestPsiPatterns {
  val TfTestFile: PsiFilePattern.Capture<HCLFile> = PlatformPatterns.psiFile(HCLFile::class.java)
    .withFileType(StandardPatterns.instanceOf(TfTestFileType::class.java))

  val TfRunBlock: PsiElementPattern.Capture<HCLBlock> = createBlockPattern(HCL_RUN_IDENTIFIER).withParent(TfTestFile)

  val TfMockProviderBlock: PsiElementPattern.Capture<HCLBlock> = createBlockPattern(HCL_MOCK_PROVIDER_IDENTIFIER).withParent(TfTestFile)

  val TfMockResourceBlock: PsiElementPattern.Capture<HCLBlock> = createBlockPattern(HCL_MOCK_RESOURCE_IDENTIFIER)
    .withParent(StandardPatterns.or(TfTestFile, HCLPatterns.Object.withParent(TfMockProviderBlock)))

  val TfMockDataBlock: PsiElementPattern.Capture<HCLBlock> = createBlockPattern(HCL_MOCK_DATA_IDENTIFIER)
    .withParent(StandardPatterns.or(TfTestFile, HCLPatterns.Object.withParent(TfMockProviderBlock)))

  val TfMockDefaultsProperty: PsiElementPattern.Capture<HCLProperty> = propertyWithName(HCL_DEFAULTS_IDENTIFIER)
    .withSuperParent(1, HCLObject::class.java)
    .withSuperParent(2, StandardPatterns.or(TfMockResourceBlock, TfMockDataBlock))

  val TfModuleBlock: PsiElementPattern.Capture<HCLBlock> = createBlockPattern(HCL_MODULE_IDENTIFIER)
    .withParent(HCLPatterns.Object.withParent(TfRunBlock))

  val TfVariablesBlock: PsiElementPattern.Capture<HCLBlock> = createBlockPattern(HCL_VARIABLES_IDENTIFIER)
    .withParent(StandardPatterns.or(TfTestFile, HCLPatterns.Object.withParent(TfRunBlock)))
}