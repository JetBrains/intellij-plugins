// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.stack.component

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.openapi.project.DumbAware
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiFilePattern
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.PsiFile
import org.intellij.terraform.hcl.codeinsight.HclBlockPropertiesCompletionProvider
import org.intellij.terraform.hcl.codeinsight.HclRootBlockCompletionProvider
import org.intellij.terraform.hcl.psi.HCLFile

internal class TfComponentCompletionContributor : CompletionContributor(), DumbAware {
  init {
    HclRootBlockCompletionProvider.registerTo(this, TfComponentFile)
    HclBlockPropertiesCompletionProvider.registerTo(this, TfComponentFile)
  }
}

internal val TfComponentFile: PsiFilePattern.Capture<HCLFile> = PlatformPatterns.psiFile(HCLFile::class.java)
  .withFileType(StandardPatterns.instanceOf(TfComponentFileType::class.java))

internal fun isTfComponentPsiFile(file: PsiFile?): Boolean = file?.fileType == TfComponentFileType