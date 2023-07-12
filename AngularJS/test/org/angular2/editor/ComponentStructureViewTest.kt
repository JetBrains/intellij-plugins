// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.editor

import com.intellij.lang.javascript.JSAbstractStructureViewTest
import com.intellij.lang.javascript.StructureViewTestUtil
import org.angularjs.AngularTestUtil

class ComponentStructureViewTest : JSAbstractStructureViewTest() {
  override fun getBasePath(): String {
    return AngularTestUtil.getBaseTestDataPath(javaClass)
  }

  override fun getTestDataPath(): String {
    return AngularTestUtil.getBaseTestDataPath(javaClass)
  }

  fun testStructure() {
    myFixture.configureByFiles("Structure.ts", "package.json")
    StructureViewTestUtil.checkStructureView(myFixture.getEditor())
  }
}
