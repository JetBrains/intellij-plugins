// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl

import com.intellij.icons.AllIcons
import com.intellij.util.PlatformIcons
import javax.swing.Icon

object Icons {
  object FileTypes {
    // TODO: Create icons
    val HCL: Icon by lazy { AllIcons.FileTypes.Text }
    val HIL: Icon by lazy { AllIcons.FileTypes.Custom }
  }

  val Property: Icon by lazy { PlatformIcons.PROPERTY_ICON }
  val Array: Icon by lazy { AllIcons.Json.Array}
  val Object: Icon by lazy { AllIcons.Json.Object}
}
