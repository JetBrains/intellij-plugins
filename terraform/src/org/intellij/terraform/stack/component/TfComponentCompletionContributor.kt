// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.stack.component

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiFile
import org.intellij.terraform.hcl.codeinsight.HclBlockPropertiesCompletionProvider
import org.intellij.terraform.hcl.codeinsight.HclRequiredProvidersCompletion
import org.intellij.terraform.hcl.codeinsight.HclRootBlockCompletionProvider
import org.intellij.terraform.stack.component.TfComponentPsiPatterns.TfComponentFile

internal class TfComponentCompletionContributor : CompletionContributor(), DumbAware {
  init {
    HclRootBlockCompletionProvider.registerTo(this, TfComponentFile)
    HclBlockPropertiesCompletionProvider.registerTo(this, TfComponentFile)

    HclRequiredProvidersCompletion.registerTo(this, TfComponentPsiPatterns.TfComponentRequiredProviders)

    TfComponentObjectKeyCompletionProvider.registerTo(this, TfComponentFile)
  }
}

internal fun isTfComponentPsiFile(file: PsiFile?): Boolean = file?.fileType == TfComponentFileType