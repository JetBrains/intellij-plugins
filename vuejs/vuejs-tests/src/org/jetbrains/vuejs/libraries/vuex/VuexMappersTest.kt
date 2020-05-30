// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex

import com.intellij.openapi.application.PathManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jetbrains.vuejs.lang.createPackageJsonWithVueDependency

class VuexMappersTest : BasePlatformTestCase() {

  override fun getTestDataPath(): String = PathManager.getHomePath() + "/contrib/vuejs/vuejs-tests/testData/libraries/vuex/mappers"

  fun testBasicVue() {
    createPackageJsonWithVueDependency(myFixture)
    myFixture.configureByFile("basicVue.vue")
    checkBasic()
  }

  fun testBasicExternal() {
    createPackageJsonWithVueDependency(myFixture)
    myFixture.configureByFiles("basicExternal.vue", "basicExternal.js")
    checkBasic()
  }

  fun testBasicInline() {
    createPackageJsonWithVueDependency(myFixture)
    myFixture.configureByFile("basicInline.js")
    checkBasic()
  }

  fun testDecoratedVue() {
    createPackageJsonWithVueDependency(myFixture)
    myFixture.configureByFile("decoratedVue.vue")
    checkDecorated()
  }

  fun testDecoratedExternal() {
    createPackageJsonWithVueDependency(myFixture)
    myFixture.configureByFiles("decoratedExternal.vue", "decoratedExternal.js")
    checkDecorated()
  }

  private fun checkBasic() {
    myFixture.completeBasic()
    assertDoesntContain(myFixture.lookupElementStrings!!, "foo", "bar", "doneTodosCount2", "count2", "increment2")
    assertContainsElements(myFixture.lookupElementStrings!!,
                           "comp1", "a", "b", "c", "count", "countAlias", "countPlusLocalState",
                           "doneTodosCount", "anotherGetter", "doneCount", "increment", "incrementBy",
                           "add", "incrementAction", "incrementByAction", "addAction")
  }

  private fun checkDecorated() {
    myFixture.completeBasic()
    assertDoesntContain(myFixture.lookupElementStrings!!, "foo")
    assertContainsElements(myFixture.lookupElementStrings!!,
                           "propFoo", "a", "b", "increment", "incrementBy",
                           "stateFoo", "stateBar", "getterFoo", "actionFoo", "mutationFoo",
                           "moduleGetterFoo", "fru", "bar", "baz", "qux")
  }

}
