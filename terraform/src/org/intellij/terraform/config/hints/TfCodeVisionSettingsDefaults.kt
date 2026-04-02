// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.hints

import com.intellij.codeInsight.codeVision.CodeVisionAnchorKind
import com.intellij.codeInsight.codeVision.settings.CodeVisionSettingsDefaults

internal class TfCodeVisionDefaultSettings : CodeVisionSettingsDefaults {
  override val defaultPosition: CodeVisionAnchorKind = CodeVisionAnchorKind.Right
}
