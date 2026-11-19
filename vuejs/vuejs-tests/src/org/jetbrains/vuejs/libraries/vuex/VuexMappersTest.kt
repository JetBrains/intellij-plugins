// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.vuex

import com.intellij.lang.javascript.JSTestUtils
import com.intellij.openapi.project.Project
import com.intellij.polySymbols.testFramework.hybridFixture
import com.intellij.testFramework.junit5.TestApplication
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.vuejs.lang.configureVueDependencies
import org.jetbrains.vuejs.lang.getVueTestDataPath
import org.junit.jupiter.api.Test

@TestApplication
class VuexMappersTest {
  private val hybrid by hybridFixture(getVueTestDataPath() + "/libraries/vuex/mappers")
  private val myFixture get() = hybrid.fixture
  private val project get() = myFixture.project

  @Test
  fun testBasicVue() {
    myFixture.configureVueDependencies()
    myFixture.configureByFile("basicVue.vue")
    checkBasic()
  }

  @Test
  fun testBasicExternal() {
    myFixture.configureVueDependencies()
    myFixture.configureByFiles("basicExternal.vue", "basicExternal.js")
    checkBasic()
  }

  @Test
  fun testBasicInline() {
    myFixture.configureVueDependencies()
    myFixture.configureByFile("basicInline.js")
    checkBasic()
  }

  @Test
  fun testDecoratedVue() {
    myFixture.configureVueDependencies()
    myFixture.configureByFile("decoratedVue.vue")
    checkDecorated()
  }

  @Test
  fun testDecoratedExternal() {
    myFixture.configureVueDependencies()
    myFixture.configureByFiles("decoratedExternal.vue", "decoratedExternal.js")
    checkDecorated()
  }

  private fun checkBasic() {
    prepareFiles(project)
    myFixture.completeBasic()
    assertThat(myFixture.lookupElementStrings!!).doesNotContain("foo", "bar", "doneTodosCount2", "count2", "increment2")
    assertThat(myFixture.lookupElementStrings!!).contains(
                           "comp1", "a", "b", "c", "count", "countAlias", "countPlusLocalState",
                           "doneTodosCount", "anotherGetter", "doneCount", "increment", "incrementBy",
                           "add", "incrementAction", "incrementByAction", "addAction")
  }

  private fun checkDecorated() {
    prepareFiles(project)
    myFixture.completeBasic()
    assertThat(myFixture.lookupElementStrings!!).doesNotContain("foo")
    assertThat(myFixture.lookupElementStrings!!).contains(
                           "propFoo", "a", "b", "increment", "incrementBy",
                           "stateFoo", "stateBar", "getterFoo", "actionFoo", "mutationFoo",
                           "moduleGetterFoo", "fru", "bar", "baz", "qux")
  }

}

internal fun prepareFiles(project: Project) {
  JSTestUtils.buildJSFileGists(project)
  // TODO tests which use this method work now only on ast and no-green-stub trees. Need to fix them for trees with stubs or green stubs
}
