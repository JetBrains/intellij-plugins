// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.test.patterns

import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.patterns.PsiFilePattern
import com.intellij.patterns.StandardPatterns
import org.intellij.terraform.config.patterns.TfPsiPatterns
import org.intellij.terraform.hcl.patterns.HCLPatterns
import org.intellij.terraform.hcl.psi.HCLFile
import org.intellij.terraform.hcl.psi.HCLObject
import org.intellij.terraform.hcl.psi.HCLProperty
import org.intellij.terraform.test.HCL_DEFAULTS_IDENTIFIER
import org.intellij.terraform.test.HCL_MOCK_DATA_IDENTIFIER
import org.intellij.terraform.test.HCL_MOCK_PROVIDER_IDENTIFIER
import org.intellij.terraform.test.HCL_MOCK_RESOURCE_IDENTIFIER
import org.intellij.terraform.test.TfTestFileType

internal object TfTestPsiPatterns {
  val TfTestFile: PsiFilePattern.Capture<HCLFile> = PlatformPatterns.psiFile(HCLFile::class.java)
    .withFileType(StandardPatterns.instanceOf(TfTestFileType::class.java))

  val TfMockProviderBlock = TfPsiPatterns.createBlockPattern(HCL_MOCK_PROVIDER_IDENTIFIER).withParent(TfTestFile)

  val TfMockResourceBlock = TfPsiPatterns.createBlockPattern(HCL_MOCK_RESOURCE_IDENTIFIER)
    .withParent(StandardPatterns.or(TfTestFile, HCLPatterns.Object.withParent(TfMockProviderBlock)))

  val TfMockDataBlock = TfPsiPatterns.createBlockPattern(HCL_MOCK_DATA_IDENTIFIER)
    .withParent(StandardPatterns.or(TfTestFile, HCLPatterns.Object.withParent(TfMockProviderBlock)))

  val TfMockDefaultsProperty: PsiElementPattern.Capture<HCLProperty> = TfPsiPatterns.propertyWithName(HCL_DEFAULTS_IDENTIFIER)
    .withSuperParent(1, HCLObject::class.java)
    .withSuperParent(2, StandardPatterns.or(TfMockResourceBlock, TfMockDataBlock))
}