// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.stub

import com.intellij.lang.javascript.psi.JSStubElementType
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.lang.javascript.psi.stubs.JSVariableStub
import org.angular2.lang.html.psi.Angular2HtmlAttrVariable

interface Angular2HtmlStubElementTypes {
  @Suppress("MayBeConstant")
  companion object {
    @JvmField
    val STUB_VERSION = 1

    @JvmField
    val EXTERNAL_ID_PREFIX = "NG-HTML:"

    @JvmField
    val REFERENCE_VARIABLE: JSStubElementType<JSVariableStub<JSVariable>, JSVariable> =
      Angular2HtmlVariableElementType(Angular2HtmlAttrVariable.Kind.REFERENCE)

    @JvmField
    val LET_VARIABLE: JSStubElementType<JSVariableStub<JSVariable>, JSVariable> =
      Angular2HtmlVariableElementType(Angular2HtmlAttrVariable.Kind.LET)

    @JvmField
    val NG_CONTENT_SELECTOR = Angular2HtmlNgContentSelectorElementType()
  }
}