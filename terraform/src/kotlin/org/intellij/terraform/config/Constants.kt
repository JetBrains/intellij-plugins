// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config

import com.intellij.openapi.options.advanced.AdvancedSettings

object Constants {
  const val HAS_DYNAMIC_ATTRIBUTES = "__has_dynamic_attributes"
  const val TIMEOUTS = "__timeouts__"

  internal val shouldDownloadDocs: Boolean
    get() = AdvancedSettings.getBoolean("org.intellij.terraform.config.documentation.download")

}