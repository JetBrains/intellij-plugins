// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.psi.impl

import com.intellij.lang.ASTNode
import org.intellij.terraform.hcl.psi.HCLLiteral
import org.intellij.terraform.hcl.psi.HCLStringLiteral

abstract class HCLLiteralMixin(node: ASTNode) : HCLValueWithReferencesMixin(node), HCLLiteral {
  override val unquotedText: String
    get() {
      if (this is HCLStringLiteral) {
        return this.value
      }
      return this.text
    }
}
