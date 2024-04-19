// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.text.StringUtil
import org.intellij.terraform.hcl.psi.HCLElement

/**
 * @author Mikhail Golubev
 */
open class HCLElementImpl(node: ASTNode) : ASTWrapperPsiElement(node), HCLElement {

  override fun toString(): String {
    val className = javaClass.simpleName
    return StringUtil.trimEnd(className, "Impl")
  }
}
