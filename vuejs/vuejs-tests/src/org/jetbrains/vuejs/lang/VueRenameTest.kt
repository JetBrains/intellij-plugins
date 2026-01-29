// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.jetbrains.vuejs.lang

import com.intellij.refactoring.rename.inplace.VariableInplaceRenameHandler
import com.intellij.testFramework.fixtures.CodeInsightTestUtil
import org.jetbrains.vuejs.VueTestCase
import org.jetbrains.vuejs.VueTestMode
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@Ignore
class VueRenameTest :
  VueRenameTestBase() {

  @Ignore
  class WithLegacyPluginTest :
    VueRenameTestBase(testMode = VueTestMode.LEGACY_PLUGIN)

  class WithoutServiceTest :
    VueRenameTestBase(testMode = VueTestMode.NO_PLUGIN)
}

@RunWith(JUnit4::class)
abstract class VueRenameTestBase(
  testMode: VueTestMode = VueTestMode.DEFAULT,
) : VueTestCase("rename", testMode = testMode) {

  @Test
  fun testComponentFieldFromTemplate() =
    doRenameTest(newName = "newName", dir = false)

  @Test
  fun testComponentFieldFromStringUsageInTemplate() =
    doRenameTest(newName = "newName", dir = false)

  @Test
  fun testTemplateLocalVariable() =
    doRenameTest(newName = "newName", dir = false)

  @Test
  fun testDestructuringInVFor() =
    doRenameTest(newName = "newName", dir = false)

  @Test
  fun testSlotProps() =
    doRenameTest(newName = "newName", dir = false)

  @Test
  fun testQualifiedWatchProperty() =
    doRenameTest(newName = "newName", dir = false)

  @Test
  fun testWatchProperty() =
    doRenameTest(newName = "newName", dir = false)

  @Test
  fun testInlineFieldRename() =
    doConfiguredTest(checkResult = true) {
      CodeInsightTestUtil.doInlineRename(VariableInplaceRenameHandler(), "foo", myFixture)
    }

  @Test
  fun testComponentNameFromDeclaration() =
    doRenameTest(mainFile = "componentNameFromDeclaration1.vue", newName = "AfterComponent")

  @Test
  fun testComponentNameFromPropertyName() =
    doRenameTest(newName = "AfterComponent")

  @Test
  fun testCssVBind() =
    doRenameTest(newName = "newColor", dir = false)

  @Test
  fun testCssVBindScriptSetup() =
    doRenameTest(newName = "newColor", dir = false)

  @Test
  fun testCreateAppComponent() =
    doRenameTest(mainFile = "main.ts", newName = "NewCar")

  @Test
  fun testCreateAppComponentFromUsage() =
    doRenameTest(mainFile = "App.vue", newName = "NewCar")

  @Test
  fun testCreateAppDirective() =
    doRenameTest(mainFile = "main.ts", newName = "bar")

  @Test
  fun testCreateAppDirectiveFromUsage() =
    doRenameTest(mainFile = "TheComponent.vue", newName = "bar")

  @Test
  fun testNamespacedComponents() =
    doRenameTest(mainFile = "scriptSetup.vue", newName = "NewName")

  @Test
  fun testCompositionApiLocalDirective() =
    doRenameTest(mainFile = "scriptSetup.vue", newName = "vNewName")

  @Test
  fun testModelDeclaration() =
    doRenameTest(newName = "alignment", dir = false)

  @Test
  fun testModelDeclarationWithVar() =
    doRenameTest(newName = "alignment", dir = false)

  @Test
  fun testModelDeclarationProp() =
    doRenameTest(mainFile = "ModelDeclarationProp.vue", newName = "count")

  @Test
  fun testModelDeclarationEvent() =
    doRenameTest(mainFile = "ModelDeclarationEvent.vue", newName = "count")

  @Test
  fun testInjectLiteral() =
    doRenameTest(mainFile = "InjectLiteral.vue", newName = "newName")

  @Test
  fun testComponentFile() =
    withRenameUsages(false) {
      checkFileRename("OrdersListView.vue", "SomeComponent.vue", searchCommentsAndText = false)
    }

  @Test
  fun testComponentFileWithUsages() =
    withRenameUsages(true) {
      checkFileRename("OrdersListView.vue", "SomeComponent.vue", searchCommentsAndText = false)
    }

  @Test
  fun testComponentFileWithReexports() =
    withRenameUsages(true) {
      checkFileRename("OrdersListView.vue", "SomeComponent.vue", searchCommentsAndText = false)
    }

  @Test
  fun testPropsOptionsFromDefinition() =
    doRenameTest(newName = "newName", dir = false)

  @Test
  fun testPropsOptionsFromUsage1() =
    doRenameTest(newName = "newName23", dir = false)

  @Test
  fun testPropsOptionsFromUsage2() =
    doRenameTest(newName = "newName23", dir = false)

  @Test
  fun testPropsOptionsFromUsage3() =
    doRenameTest(newName = "newName23", dir = false)

  @Test
  fun testPropsOptionsUpperCaseFromDefinition() =
    doRenameTest(newName = "NewName", dir = false)

  @Test
  fun testPropsOptionsNumberFromDefinition() =
    doRenameTest(newName = "newName23", dir = false)

  @Test
  fun testPropsOptionsExtUsageFromDefinition() =
    doRenameTest(mainFile = "MyComponent.vue", newName = "newName")

  @Test
  fun testPropsOptionsExtUsageFromUsage() =
    doRenameTest(mainFile = "MyUsage.vue", newName = "newName23")

  @Test
  fun testPropsStringsFromDefinition() =
    doRenameTest(newName = "newName", dir = false)

  @Test
  fun testPropsStringsFromUsage1() =
    doRenameTest(newName = "newName", dir = false)

  @Test
  fun testPropsStringsFromUsage2() =
    doRenameTest(newName = "newName", dir = false)

  @Test
  fun testPropsStringsExtUsageFromDefinition() =
    doRenameTest(mainFile = "MyComponent.vue", newName = "newName")

  @Test
  fun testPropsStringsExtUsageFromUsage() =
    doRenameTest(mainFile = "MyUsage.vue", newName = "newName")

  @Test
  fun testDefinePropsRecordTypeFromDefinition() =
    doRenameTest(newName = "newName", dir = false)

  @Test
  fun testDefinePropsRecordTypeFromUsage1() =
    doRenameTest(newName = "newName", dir = false)

  @Test
  fun testDefinePropsRecordTypeFromUsage2() =
    doRenameTest(newName = "newName", dir = false)

  @Test
  fun testDefinePropsRecordTypeExtUsageFromDefinition() =
    doRenameTest(mainFile = "MyComponent.vue", newName = "newName")

  @Test
  fun testDefinePropsRecordTypeExtUsageFromUsage() =
    doRenameTest(mainFile = "MyUsage.vue", newName = "newName")

  @Test
  fun testDefinePropsArrayLiteralFromDefinition() =
    doRenameTest(newName = "newName", dir = false)

  @Test
  fun testDefinePropsArrayLiteralFromUsage1() =
    doRenameTest(newName = "newName", dir = false)

  @Test
  fun testDefinePropsArrayLiteralFromUsage2() =
    doRenameTest(newName = "newName", dir = false)

  @Test
  fun testDefinePropsArrayLiteralExtUsageFromDefinition() =
    doRenameTest(mainFile = "MyComponent.vue", newName = "newName")

  @Test
  fun testDefinePropsArrayLiteralExtUsageFromUsage() =
    doRenameTest(mainFile = "MyUsage.vue", newName = "newName")

  @Test
  fun testDefinePropsObjectLiteralFromDefinition() =
    doRenameTest(newName = "newName", dir = false)

  @Test
  fun testDefinePropsObjectLiteralFromUsage1() =
    doRenameTest(newName = "newName", dir = false)

  @Test
  fun testDefinePropsObjectLiteralFromUsage2() =
    doRenameTest(newName = "newName", dir = false)

  @Test
  fun testDefinePropsObjectLiteralExtUsageFromDefinition() =
    doRenameTest(mainFile = "MyComponent.vue", newName = "newName")

  @Test
  fun testDefinePropsObjectLiteralExtUsageFromUsage() =
    doRenameTest(mainFile = "MyUsage.vue", newName = "newName")

  @Test
  fun testDefinePropsInterfaceFromDefinition() =
    doRenameTest(newName = "newName", dir = false)

  @Test
  fun testDefinePropsInterfaceFromUsage1() =
    doRenameTest(newName = "newName", dir = false)

  @Test
  fun testDefinePropsInterfaceFromUsage2() =
    doRenameTest(newName = "newName", dir = false)

  @Test
  fun testDefinePropsInterfaceExtUsageFromDefinition() =
    doRenameTest(mainFile = "MyComponent.vue", newName = "newName")

  @Test
  fun testDefinePropsInterfaceExtUsageFromUsage() =
    doRenameTest(mainFile = "MyUsage.vue", newName = "newName")

  @Test
  fun testDefinePropsExtInterfaceFromDefinition() =
    doRenameTest(mainFile = "fooProps.ts", newName = "newName")

  @Test
  fun testDefinePropsExtInterfaceFromUsage1() =
    doRenameTest(mainFile = "definePropsInterface.vue", newName = "newName")

  @Test
  fun testDefinePropsExtInterfaceFromUsage2() =
    doRenameTest(mainFile = "definePropsInterface.vue", newName = "newName")

  @Test
  fun testDefinePropsExtInterfaceExtUsageFromDefinition() =
    doRenameTest(mainFile = "fooProps.ts", newName = "newName")

  @Test
  fun testDefinePropsExtInterfaceExtUsageFromUsage() =
    doRenameTest(mainFile = "MyUsage.vue", newName = "newName")

  @Test
  fun testComponentFromFunctionPlugin_renameFromDeclaration() {
    doRenameTest(
      mainFile = "global-components.ts",
      newName = "OtherButtonFromPlugin",
    )
  }

  @Test
  fun testComponentFromFunctionPlugin_renameFromUsage() {
    doRenameTest(
      mainFile = "App.vue",
      newName = "OtherButtonFromPlugin",
    )
  }

  @Test
  fun testComponentFromNestedFunctionPlugin_renameFromDeclaration() {
    doRenameTest(
      mainFile = "other-global-components.js",
      newName = "OtherLabelFromPlugin",
    )
  }

  @Test
  fun testComponentFromNestedFunctionPlugin_renameFromUsage() {
    doRenameTest(
      mainFile = "App.vue",
      newName = "OtherLabelFromPlugin",
    )
  }

  @Test
  fun testComponentFromNestedFunctionPluginWithCycle_renameFromDeclaration() {
    doRenameTest(
      mainFile = "other-global-components.js",
      newName = "OtherLabelFromPlugin",
    )
  }

  @Test
  fun testComponentFromNestedFunctionPluginWithCycle_renameFromUsage() {
    doRenameTest(
      mainFile = "App.vue",
      newName = "OtherLabelFromPlugin",
    )
  }

  @Test
  fun testComponentFromObjectPlugin_renameFromDeclaration() {
    doRenameTest(
      mainFile = "global-components.ts",
      newName = "OtherButtonFromPlugin",
    )
  }

  @Test
  fun testComponentFromObjectPlugin_renameFromUsage() {
    doRenameTest(
      mainFile = "App.vue",
      newName = "OtherButtonFromPlugin",
    )
  }

  @Test
  fun testComponentFromNestedObjectPlugin_renameFromDeclaration() {
    doRenameTest(
      mainFile = "other-global-components.js",
      newName = "OtherLabelFromPlugin",
    )
  }

  @Test
  fun testComponentFromNestedObjectPlugin_renameFromUsage() {
    doRenameTest(
      mainFile = "App.vue",
      newName = "OtherLabelFromPlugin",
    )
  }

  @Test
  fun testComponentFromNestedObjectPluginWithCycle_renameFromDeclaration() {
    doRenameTest(
      mainFile = "other-global-components.js",
      newName = "OtherLabelFromPlugin",
    )
  }

  @Test
  fun testComponentFromNestedObjectPluginWithCycle_renameFromUsage() {
    doRenameTest(
      mainFile = "App.vue",
      newName = "OtherLabelFromPlugin",
    )
  }

  private fun doRenameTest(
    mainFile: String = "$testName.$defaultExtension",
    newName: String,
    dir: Boolean = true,
  ) {
    checkSymbolRename(
      mainFile = mainFile,
      newName = newName,
      dir = dir,
      modules = arrayOf(
        VueTestModule.VUE_3_5_0,
      ),
    )
  }
}
