// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex

import com.intellij.lang.javascript.JSTestUtils
import com.intellij.openapi.project.Project
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jetbrains.vuejs.lang.configureVueDependencies
import org.jetbrains.vuejs.lang.getVueTestDataPath

class VuexMappersTest : BasePlatformTestCase() {

  override fun getTestDataPath(): String = getVueTestDataPath() + "/libraries/vuex/mappers"

  fun testBasicVue() {
    myFixture.configureVueDependencies()
    myFixture.configureByFile("basicVue.vue")
    checkBasic()
  }

  fun testBasicExternal() {
    myFixture.configureVueDependencies()
    myFixture.configureByFiles("basicExternal.vue", "basicExternal.js")
    checkBasic()
  }

  fun testBasicInline() {
    myFixture.configureVueDependencies()
    myFixture.configureByFile("basicInline.js")
    checkBasic()
  }

  fun testDecoratedVue() {
    myFixture.configureVueDependencies()
    myFixture.configureByFile("decoratedVue.vue")
    checkDecorated()
  }

  fun testDecoratedExternal() {
    myFixture.configureVueDependencies()
    myFixture.configureByFiles("decoratedExternal.vue", "decoratedExternal.js")
    checkDecorated()
  }

  private fun checkBasic() {
    prepareFiles(project)
    myFixture.completeBasic()
    assertDoesntContain(myFixture.lookupElementStrings!!, "foo", "bar", "doneTodosCount2", "count2", "increment2")
    assertContainsElements(myFixture.lookupElementStrings!!,
                           "comp1", "a", "b", "c", "count", "countAlias", "countPlusLocalState",
                           "doneTodosCount", "anotherGetter", "doneCount", "increment", "incrementBy",
                           "add", "incrementAction", "incrementByAction", "addAction")
  }

  private fun checkDecorated() {
    prepareFiles(project)
    myFixture.completeBasic()
    assertDoesntContain(myFixture.lookupElementStrings!!, "foo")
    assertContainsElements(myFixture.lookupElementStrings!!,
                           "propFoo", "a", "b", "increment", "incrementBy",
                           "stateFoo", "stateBar", "getterFoo", "actionFoo", "mutationFoo",
                           "moduleGetterFoo", "fru", "bar", "baz", "qux")
  }

}

internal fun prepareFiles(project: Project) {
  JSTestUtils.buildJSFileGists(project)
  // TODO tests which use this method work now only on ast and no-green-stub trees. Need to fix them for trees with stubs or green stubs
}
