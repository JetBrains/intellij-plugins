// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.psi.search.GlobalSearchScopesCore
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.junit5.TestApplication
import org.jetbrains.vuejs.VueFixture
import org.jetbrains.vuejs.VueTestMode
import org.jetbrains.vuejs.VueTsConfigFile
import org.jetbrains.vuejs.config.VueCompilerOptions
import org.jetbrains.vuejs.vueFixture
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@TestApplication
@Disabled
class VueFindUsagesTest :
  VueFindUsagesTestBase() {

  @TestApplication
  class WithTsGoProxyTest :
    VueFindUsagesTestBase(testMode = VueTestMode.TS_GO_PROXY)

  @TestApplication
  class WithoutServiceTest :
    VueFindUsagesTestBase(testMode = VueTestMode.NO_PLUGIN)
}

abstract class VueFindUsagesTestBase(
  testMode: VueTestMode = VueTestMode.DEFAULT,
) {

  protected val vue: VueFixture by vueFixture("findUsages", testMode = testMode, dirModeByDefault = true)

  private fun CodeInsightTestFixture.useSeparateTestDataPath() {
    testDataPath += "/${vue.defaultDirName}"
  }

  @Test
  fun testPrivateComponentGetter() {
    vue.doFindUsagesTest(configureFileName = "App.vue")
  }

  @Test
  fun testPrivateComponentSetter() {
    vue.doFindUsagesTest(configureFileName = "App.vue")
  }

  @Test
  fun testPrivateComponentMethod() {
    vue.doFindUsagesTest(configureFileName = "App.vue")
  }

  @Test
  fun testPrivateConstructorField() {
    vue.doFindUsagesTest(configureFileName = "App.vue")
  }

  @Test
  fun testScriptSetupRef() {
    vue.doConfiguredTest(
      configurators = listOf(
        VueTsConfigFile(
          vueCompilerOptions = VueCompilerOptions(
            inferTemplateDollarRefs = true,
          ),
        ),
      ),
    ) {
      useSeparateTestDataPath()

      sequenceOf(
        "ref='f<caret>oo2'",
        "ref='fo<caret>o'",
        "\$refs.fo<caret>o2 ",
        "\$refs.fo<caret>o ",
        "const fo<caret>o2",
        "const fo<caret>o3",
      ).forEachIndexed { index, signature ->
        vue.checkUsages(signature, "usages.$index")
      }
    }
  }

  @Test
  fun testScriptSetupImportedDirective() {
    vue.doFindUsagesTest(configureFileName = "vFocus.js")
  }

  @Test
  fun testTypedComponents() {
    vue.doConfiguredTest(
      VueTestModule.HEADLESS_UI_1_4_1,
      VueTestModule.NAIVE_UI_2_19_11,
      configureFileName = "typedComponentsClassic.vue",
    ) {
      useSeparateTestDataPath()

      vue.checkUsages(" Dia<caret>log,", "usages.classic.headlessui")
      vue.checkUsages(" N<caret>Affix,", "usages.classic.naive-ui")
      vue.checkUsages(" Fo<caret>o:", "usages.classic.foo")
      vue.checkUsages(" Ba<caret>r:", "usages.classic.bar")

      configureFromTempProjectFile("node_modules/@headlessui/vue/dist/components/dialog/dialog.d.ts")
      vue.checkUsages("export declare let Dia<caret>log:", "usages.headlessui")

      configureFromTempProjectFile("node_modules/naive-ui/lib/affix/src/Affix.d.ts")
      vue.checkUsages(
        "declare const _defa<caret>ult:",
        "usages.naive-ui",
        scope = GlobalSearchScopesCore.directoryScope(vue.project, tempDirFixture.findOrCreateDir("."), true),
      )
    }
  }

  @Test
  fun testCreateApp() {
    vue.doConfiguredTest(
      configureFileName = "main.ts",
      dirName = "../common/createApp",
    ) {
      useSeparateTestDataPath()

      vue.checkUsages("\"f<caret>oo", "createApp.foo")
      vue.checkUsages("\"B<caret>ar", "createApp.bar")
      vue.checkUsages("\"C<caret>ar", "createApp.car")
      vue.checkUsages("\"Foo<caret>Bar", "createApp.foo-bar")
    }
  }

  @Test
  fun testComponentEmitsDefinitions() {
    vue.doConfiguredTest(
      configureFile = false,
    ) {
      useSeparateTestDataPath()

      configureFromTempProjectFile("defineComponent.vue")
      vue.checkUsages("\"test<caret>-event", "${vue.testName}.test-event")

      configureFromTempProjectFile("defineEmits.vue")
      vue.checkUsages("'f<caret>oo", "${vue.testName}.foo")
    }
  }

  @Test
  fun testComponentFile() {
    vue.doFileUsagesTest(fileName = "SomeComponent.vue")
  }
}
