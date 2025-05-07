// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.runtime

import com.intellij.application.options.CodeStyle
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import org.intellij.terraform.hcl.formatter.HclCodeStyleSettings
import org.intellij.terraform.install.TfToolType

internal class TfProjectActivity : ProjectActivity {
  override suspend fun execute(project: Project) {
    if (ApplicationManager.getApplication().isUnitTestMode)
      return

    val hclSettings = CodeStyle.getSettings(project).getCustomSettings(HclCodeStyleSettings::class.java)
    if (hclSettings.RUN_TF_FMT_ON_REFORMAT) {
      return
    }

    val isTfConfigured = TfToolPathDetector.getInstance(project).detectAndVerifyTool(TfToolType.TERRAFORM, overrideExistingValue = false)
    if (isTfConfigured) {
      hclSettings.RUN_TF_FMT_ON_REFORMAT = true
    }
  }
}