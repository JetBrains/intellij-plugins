// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.lang

import com.intellij.psi.search.GlobalSearchScopesCore
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import org.jetbrains.vuejs.VueTestCase
import org.jetbrains.vuejs.VueTestMode
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@Ignore
class VueFindUsagesTest :
  VueFindUsagesTestBase() {

  @Ignore
  class WithLegacyPluginTest :
    VueFindUsagesTestBase(testMode = VueTestMode.LEGACY_PLUGIN)

  class WithoutServiceTest :
    VueFindUsagesTestBase(testMode = VueTestMode.NO_PLUGIN)
}

@RunWith(JUnit4::class)
abstract class VueFindUsagesTestBase(
  testMode: VueTestMode = VueTestMode.DEFAULT,
) : VueTestCase("findUsages", testMode = testMode) {

  override val dirModeByDefault: Boolean =
    true

  private fun CodeInsightTestFixture.useSeparateTestDataPath() {
    testDataPath += "/" + getTestName(true)
  }

  @Test
  fun testPrivateComponentGetter() {
    doFindUsagesTest(configureFileName = "App.vue")
  }

  @Test
  fun testPrivateComponentSetter() {
    doFindUsagesTest(configureFileName = "App.vue")
  }

  @Test
  fun testPrivateComponentMethod() {
    doFindUsagesTest(configureFileName = "App.vue")
  }

  @Test
  fun testPrivateConstructorField() {
    doFindUsagesTest(configureFileName = "App.vue")
  }

  @Test
  fun testScriptSetupRef() {
    doConfiguredTest {
      useSeparateTestDataPath()

      sequenceOf(
        "ref='f<caret>oo2'",
        "ref='fo<caret>o'",
        "\$refs.fo<caret>o2 ",
        "\$refs.fo<caret>o ",
        "const fo<caret>o2",
        "const fo<caret>o3",
      ).forEachIndexed { index, signature ->
        checkUsages(signature, "usages.$index")
      }
    }
  }

  @Test
  fun testScriptSetupImportedDirective() {
    doFindUsagesTest(configureFileName = "vFocus.js")
  }

  @Test
  fun testTypedComponents() {
    doConfiguredTest(
      VueTestModule.HEADLESS_UI_1_4_1,
      VueTestModule.NAIVE_UI_2_19_11,
      configureFileName = "typedComponentsClassic.vue",
    ) {
      useSeparateTestDataPath()

      checkUsages(" Dia<caret>log,", "usages.classic.headlessui")
      checkUsages(" N<caret>Affix,", "usages.classic.naive-ui")
      checkUsages(" Fo<caret>o:", "usages.classic.foo")
      checkUsages(" Ba<caret>r:", "usages.classic.bar")

      configureFromTempProjectFile("node_modules/@headlessui/vue/dist/components/dialog/dialog.d.ts")
      checkUsages("export declare let Dia<caret>log:", "usages.headlessui")

      configureFromTempProjectFile("node_modules/naive-ui/lib/affix/src/Affix.d.ts")
      checkUsages(
        "declare const _defa<caret>ult:",
        "usages.naive-ui",
        scope = GlobalSearchScopesCore.directoryScope(project, tempDirFixture.findOrCreateDir("."), true),
      )
    }
  }

  @Test
  fun testCreateApp() {
    doConfiguredTest(
      configureFileName = "main.ts",
      dirName = "../common/createApp",
    ) {
      useSeparateTestDataPath()

      checkUsages("\"f<caret>oo", "createApp.foo")
      checkUsages("\"B<caret>ar", "createApp.bar")
      checkUsages("\"C<caret>ar", "createApp.car")
      checkUsages("\"Foo<caret>Bar", "createApp.foo-bar")
    }
  }

  @Test
  fun testComponentEmitsDefinitions() {
    doConfiguredTest(
      configureFile = false,
    ) {
      useSeparateTestDataPath()

      configureFromTempProjectFile("defineComponent.vue")
      checkUsages("\"test<caret>-event", "$testName.test-event")

      configureFromTempProjectFile("defineEmits.vue")
      checkUsages("'f<caret>oo", "$testName.foo")
    }
  }

  @Test
  fun testComponentFile() {
    doFileUsagesTest(fileName = "SomeComponent.vue")
  }
}
