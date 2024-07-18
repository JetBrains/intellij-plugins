// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.inspection

import org.intellij.terraform.hcl.psi.HCLElementVisitor

internal object TfInspectionUtils {

  val EMPTY_HCL_ELEMENT_VISITOR: HCLElementVisitor = HCLElementVisitor()

}