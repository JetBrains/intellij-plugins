// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.service

import com.intellij.platform.lsp.tests.checkLspHighlighting
import org.jetbrains.vuejs.lang.VueInspectionsProvider
import org.jetbrains.vuejs.lang.VueTestModule
import org.jetbrains.vuejs.lang.configureVueDependencies
import org.junit.Test

class VolarServiceTest : VolarServiceTestBase() {

  @Test
  fun testSimpleVue() {
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.configureVueDependencies(VueTestModule.VUE_3_0_0)

    myFixture.configureByText("tsconfig.json", tsconfig)
    myFixture.configureByText("Simple.vue", """
      <script setup lang="ts">
      let <error descr="Volar: Type 'number' is not assignable to type 'string'.">a</error>: string = 1;
      
      function acceptNumber(num: number): number { return num; }
      
      acceptNumber(<error descr="Volar: Argument of type 'boolean' is not assignable to parameter of type 'number'.">true</error>);
      </script>
      
      <template>
        <div v-text="acceptNumber(<error descr="Volar: Argument of type 'boolean' is not assignable to parameter of type 'number'.">true</error>)" />
        <!-- todo remove duplicate internal warning -->
        <div>{{acceptNumber(<error descr="Argument type  true  is not assignable to parameter type  number "><error descr="Volar: Argument of type 'boolean' is not assignable to parameter of type 'number'.">true</error></error>)}}</div>
      </template>
    """)
    myFixture.checkLspHighlighting()
    assertCorrectService()
  }
}