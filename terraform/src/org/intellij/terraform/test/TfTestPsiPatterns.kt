// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.test

import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.patterns.PsiFilePattern
import com.intellij.patterns.StandardPatterns
import com.intellij.patterns.StandardPatterns.or
import org.intellij.terraform.config.patterns.TfPsiPatterns.createBlockPattern
import org.intellij.terraform.config.patterns.TfPsiPatterns.propertyWithName
import org.intellij.terraform.hcl.patterns.HCLPatterns
import org.intellij.terraform.hcl.psi.HCLFile
import org.intellij.terraform.hcl.psi.HCLObject
import org.intellij.terraform.hcl.psi.HCLProperty

internal object TfTestPsiPatterns {
  val TfTestFile: PsiFilePattern.Capture<HCLFile> = PlatformPatterns.psiFile(HCLFile::class.java)
    .withFileType(StandardPatterns.instanceOf(TfTestFileType::class.java))

  val TfMockProviderBlock = createBlockPattern(HCL_MOCK_PROVIDER_IDENTIFIER).withParent(TfTestFile)

  val TfMockResourceBlock = createBlockPattern(HCL_MOCK_RESOURCE_IDENTIFIER)
    .withParent(or(TfTestFile, HCLPatterns.Object.withParent(TfMockProviderBlock)))

  val TfMockDataBlock = createBlockPattern(HCL_MOCK_DATA_IDENTIFIER)
    .withParent(or(TfTestFile, HCLPatterns.Object.withParent(TfMockProviderBlock)))

  val TfMockDefaultsProperty: PsiElementPattern.Capture<HCLProperty> = propertyWithName(HCL_DEFAULTS_IDENTIFIER)
    .withSuperParent(1, HCLObject::class.java)
    .withSuperParent(2, or(TfMockResourceBlock, TfMockDataBlock))
}