// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.service

import com.intellij.javascript.debugger.com.intellij.lang.javascript.waitCoroutinesBlocking
import com.intellij.lang.typescript.compiler.TypeScriptCompilerSettings
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.platform.lsp.tests.checkLspHighlighting
import org.jetbrains.vuejs.lang.VueInspectionsProvider
import org.jetbrains.vuejs.lang.VueTestModule
import org.jetbrains.vuejs.lang.configureVueDependencies
import org.jetbrains.vuejs.lang.typescript.service.volar.VolarCoroutineScope
import org.jetbrains.vuejs.lang.typescript.service.volar.VolarRestartService
import org.junit.Test

class VolarServiceTest : VolarServiceTestBase() {

  @Test
  fun testSimpleVue() {
    myFixture.enableInspections(VueInspectionsProvider())
    myFixture.configureVueDependencies(VueTestModule.VUE_3_0_0)

    myFixture.configureByText("tsconfig.json", tsconfig)
    myFixture.configureByText("Simple.vue", """
      <script setup lang="ts">
      let <error descr="Vue: Type 'number' is not assignable to type 'string'.">a</error>: string = 1;
      
      function acceptNumber(num: number): number { return num; }
      
      acceptNumber(<error descr="Vue: Argument of type 'boolean' is not assignable to parameter of type 'number'.">true</error>);
      </script>
      
      <template>
        <div v-text="acceptNumber(<error descr="Vue: Argument of type 'boolean' is not assignable to parameter of type 'number'.">true</error>)" />
        <!-- todo remove duplicate internal warning -->
        <div>{{acceptNumber(<error descr="Argument type  true  is not assignable to parameter type  number "><error descr="Vue: Argument of type 'boolean' is not assignable to parameter of type 'number'.">true</error></error>)}}</div>
      </template>
    """)
    myFixture.doHighlighting()
    myFixture.checkLspHighlighting()
    assertCorrectService()
  }

  @Test
  fun testEnableSuggestions() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_0_0)
    myFixture.configureByText("tsconfig.json", tsconfig)
    myFixture.configureByText("Simple.vue", """
      <script setup lang="ts">
      export function hello(<weak_warning descr="Vue: 'p' is declared but its value is never read." textAttributesKey="NOT_USED_ELEMENT_ATTRIBUTES">p</weak_warning>: number, p2: number) {
          console.log(p2);
      }
      let <error descr="Vue: Type 'number' is not assignable to type 'string'.">a</error>: string = 1;
      </script>
      
      <template>
      </template>
    """)
    myFixture.doHighlighting()
    myFixture.checkLspHighlighting()
    assertCorrectService()
  }

  @Test
  fun testDisableSuggestions() {
    val settings = TypeScriptCompilerSettings.getSettings(project)
    settings.isShowSuggestions = false
    disposeOnTearDown(Disposable { settings.isShowSuggestions = true })
    myFixture.configureVueDependencies(VueTestModule.VUE_3_0_0)
    myFixture.addFileToProject("tsconfig.json", tsconfig)
    myFixture.configureByText("Simple.vue", """
      <script setup lang="ts">
      export function hello(p: number, p2: number) {
          console.log(p2);
      }
      let <error descr="Vue: Type 'number' is not assignable to type 'string'.">a</error>: string = 1;
      </script>
      
      <template>
      </template>
    """)
    myFixture.doHighlighting()
    myFixture.checkLspHighlighting()
    assertCorrectService()
  }

  @Test
  fun testSimpleRename() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_0_0)
    myFixture.addFileToProject("tsconfig.json", tsconfig)
    val fileToRename = myFixture.addFileToProject("Usage.vue", """
      <script setup lang="ts">
        console.log("test");
      </script>
      <template>text</template>
    """.trimIndent())

    myFixture.configureByText("Simple.vue", """
      <script setup lang="ts">
      import Usage from './Us<caret>age.vue';
      console.log(Usage);
      </script>
      <template>
      </template>
    """)

    myFixture.doHighlighting()
    myFixture.checkLspHighlighting()
    myFixture.renameElement(fileToRename, "Usage2.vue")

    //no errors
    myFixture.doHighlighting()
    myFixture.checkLspHighlighting()


    assertCorrectService()
  }

}