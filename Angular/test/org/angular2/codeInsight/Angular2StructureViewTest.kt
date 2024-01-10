// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight

import com.intellij.lang.javascript.JSAbstractStructureViewTest
import com.intellij.lang.javascript.StructureViewTestUtil
import org.angular2.Angular2TestUtil

class Angular2StructureViewTest : JSAbstractStructureViewTest() {
  override fun getBasePath(): String {
    return Angular2TestUtil.getBaseTestDataPath() + "structureView"
  }

  override fun getTestDataPath(): String {
    return Angular2TestUtil.getBaseTestDataPath() + "structureView"
  }

  fun testStructure() {
    myFixture.configureByFiles("Structure.ts", "package.json")
    StructureViewTestUtil.checkStructureView(myFixture.getEditor())
  }
}
