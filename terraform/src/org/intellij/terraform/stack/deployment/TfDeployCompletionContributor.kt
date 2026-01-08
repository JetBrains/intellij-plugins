// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.stack.deployment

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.openapi.project.DumbAware
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiFilePattern
import com.intellij.patterns.StandardPatterns
import com.intellij.psi.PsiFile
import org.intellij.terraform.hcl.codeinsight.HclRootBlockCompletionProvider
import org.intellij.terraform.hcl.psi.HCLFile

internal class TfDeployCompletionContributor : CompletionContributor(), DumbAware {
  init {
    HclRootBlockCompletionProvider.registerTo(this, TfDeployFile)
  }
}

internal val TfDeployFile: PsiFilePattern.Capture<HCLFile> = PlatformPatterns.psiFile(HCLFile::class.java)
  .withFileType(StandardPatterns.instanceOf(TfDeployFileType::class.java))

internal fun isTfDeployPsiFile(file: PsiFile?): Boolean = file?.fileType == TfDeployFileType