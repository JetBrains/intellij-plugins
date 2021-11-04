// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.javascript.web.checkUsages
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class VueFindUsagesTest : BasePlatformTestCase() {

  override fun getTestDataPath(): String = getVueTestDataPath() + "/findUsages"

  fun testPrivateComponentGetter() {
    myFixture.configureByFiles("privateFields.vue")
    myFixture.checkUsages("get f<caret>oo", getTestName(true))
  }

  fun testPrivateComponentSetter() {
    myFixture.configureByFiles("privateFields.vue")
    myFixture.checkUsages("set f<caret>oo", getTestName(true))
  }

  fun testPrivateComponentMethod() {
    myFixture.configureByFiles("privateFields.vue")
    myFixture.checkUsages("private on<caret>Scrolled", getTestName(true))
  }

  fun testPrivateConstructorField() {
    myFixture.configureByFiles("privateFields.vue")
    myFixture.checkUsages("private ba<caret>r", getTestName(true))
  }

  fun testScriptSetupRef() {
    myFixture.configureByFiles("scriptSetupRef.vue")
    listOf("ref='f<caret>oo2'", "ref='fo<caret>o'",
           "\$refs.fo<caret>o2 ", "\$refs.fo<caret>o ",
           "const fo<caret>o2", "const fo<caret>o3")
      .forEachIndexed { index, signature ->
        myFixture.checkUsages(signature, getTestName(true) + "." + index)
      }
  }

}
