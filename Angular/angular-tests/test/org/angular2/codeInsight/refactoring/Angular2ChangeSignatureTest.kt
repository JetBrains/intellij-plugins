// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.refactoring

import com.intellij.lang.javascript.JSChangeSignatureFixture.jsChangeSignatureFixture
import com.intellij.lang.javascript.JSFixtureOptions
import com.intellij.lang.javascript.jsProjectFixture
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList
import com.intellij.lang.javascript.refactoring.changeSignature.JSParameterInfo
import com.intellij.refactoring.changeSignature.ParameterInfo
import com.intellij.testFramework.junit5.TestApplication
import org.angular2.Angular2TestUtil
import org.junit.jupiter.api.Test

@TestApplication
class Angular2ChangeSignatureTest {
  private val projectFixture = jsProjectFixture()
  private val js by jsChangeSignatureFixture(
    projectFixture,
    JSFixtureOptions(Angular2TestUtil.getBaseTestDataPath() + "refactoring/").withBasePath("changeSignature/"),
    "component.ts",
  )

  @Test
  fun testAddParamAndRenameAndSetReturnType() {
    js.doTest("bar", JSAttributeList.AccessType.PUBLIC, "number",
              JSParameterInfo("stringParam", "string", "", "\"def\"", ParameterInfo.NEW_PARAMETER))
  }
}
