// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.refactoring

import com.intellij.lang.javascript.JSChangeSignatureTestBase
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList
import com.intellij.lang.javascript.refactoring.changeSignature.JSParameterInfo
import com.intellij.refactoring.changeSignature.ParameterInfo
import org.angularjs.AngularTestUtil

class Angular2ChangeSignatureTest : JSChangeSignatureTestBase() {
  override fun getTestDataPath(): String {
    return AngularTestUtil.getBaseTestDataPath() + "refactoring/"
  }

  override fun getActiveFileNames(): Array<String> {
    return arrayOf("component.ts")
  }

  override fun getTestRoot(): String {
    return "changeSignature/"
  }

  fun testAddParamAndRenameAndSetReturnType() {
    doTest("bar", JSAttributeList.AccessType.PUBLIC, "number",
           JSParameterInfo("stringParam", "string", "", "\"def\"", ParameterInfo.NEW_PARAMETER))
  }
}
