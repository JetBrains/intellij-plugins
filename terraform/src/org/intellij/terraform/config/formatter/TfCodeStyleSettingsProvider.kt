// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.formatter

import org.intellij.terraform.config.TerraformLanguage
import org.intellij.terraform.hcl.formatter.HCLCodeStyleSettingsProvider

class TfCodeStyleSettingsProvider : HCLCodeStyleSettingsProvider(TerraformLanguage)

