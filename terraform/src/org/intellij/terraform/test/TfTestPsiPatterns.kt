// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.test

import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiFilePattern
import com.intellij.patterns.StandardPatterns
import org.intellij.terraform.hcl.psi.HCLFile

internal object TfTestPsiPatterns {
  val TfTestFile: PsiFilePattern.Capture<HCLFile> = PlatformPatterns.psiFile(HCLFile::class.java)
    .withFileType(StandardPatterns.instanceOf(TfTestFileType::class.java))
}