// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.test

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.openapi.project.DumbAware
import org.intellij.terraform.hcl.codeinsight.HclBlockPropertiesCompletionProvider
import org.intellij.terraform.hcl.codeinsight.HclBlockTypeNameCompletionProvider
import org.intellij.terraform.hcl.codeinsight.HclRootBlockCompletionProvider
import org.intellij.terraform.test.TfTestPsiPatterns.TfTestFile

internal class TfTestCompletionContributor : CompletionContributor(), DumbAware {
  init {
    HclRootBlockCompletionProvider.registerTo(this, TfTestFile)
    HclBlockPropertiesCompletionProvider.registerTo(this, TfTestFile)

    HclBlockTypeNameCompletionProvider.registerTo(this, TfTestFile)
  }
}