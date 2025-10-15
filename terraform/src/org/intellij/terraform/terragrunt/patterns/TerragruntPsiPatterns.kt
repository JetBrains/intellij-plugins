// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.terragrunt.patterns

import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern.Capture
import com.intellij.patterns.PsiFilePattern
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.PsiElement
import org.intellij.terraform.hcl.psi.HCLElement
import org.intellij.terraform.hcl.psi.HCLFile
import org.intellij.terraform.hil.patterns.HILPatterns.getMethodIdentifierPattern
import org.intellij.terraform.terragrunt.TerragruntFileType

internal object TerragruntPsiPatterns {
  val TerragruntFile: PsiFilePattern.Capture<HCLFile> = PlatformPatterns.psiFile(HCLFile::class.java)
    .withFileType(StandardPatterns.instanceOf(TerragruntFileType::class.java))

  val TerragruntMethodPosition: Capture<PsiElement> = getMethodIdentifierPattern(
    PlatformPatterns.psiElement(HCLElement::class.java)
      .inFile(TerragruntFile)
  )
}