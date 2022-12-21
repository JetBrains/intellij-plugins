// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.expr.parser

import com.intellij.lang.javascript.psi.JSStubElementType
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.lang.javascript.psi.stubs.JSVariableStub

interface Angular2StubElementTypes {
  @Suppress("MayBeConstant")
  companion object {
    @JvmField
    val STUB_VERSION = 4

    @JvmField
    val EXTERNAL_ID_PREFIX = "NG:"

    @JvmField
    val TEMPLATE_VARIABLE: JSStubElementType<JSVariableStub<JSVariable>, JSVariable> = Angular2TemplateVariableElementType()
  }
}