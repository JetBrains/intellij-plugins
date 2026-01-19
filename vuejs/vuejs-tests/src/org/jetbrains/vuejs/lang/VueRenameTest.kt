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

class VueRenameTest : VueTestCase("rename") {

  fun testComponentFieldFromTemplate() =
    doRenameTest(newName = "newName", dir = false)

  fun testComponentFieldFromStringUsageInTemplate() =
    doRenameTest(newName = "newName", dir = false)

  fun testTemplateLocalVariable() =
    doRenameTest(newName = "newName", dir = false)

  fun testDestructuringInVFor() =
    doRenameTest(newName = "newName", dir = false)

  fun testSlotProps() =
    doRenameTest(newName = "newName", dir = false)

  fun testQualifiedWatchProperty() =
    doRenameTest(newName = "newName", dir = false)

  fun testWatchProperty() =
    doRenameTest(newName = "newName", dir = false)

  fun testInlineFieldRename() =
    doConfiguredTest(checkResult = true) {
      CodeInsightTestUtil.doInlineRename(VariableInplaceRenameHandler(), "foo", myFixture)
    }

  fun testComponentNameFromDeclaration() =
    doRenameTest(mainFile = "componentNameFromDeclaration1.vue", newName = "AfterComponent")

  fun testComponentNameFromPropertyName() =
    doRenameTest(newName = "AfterComponent")

  fun testCssVBind() =
    doRenameTest(newName = "newColor", dir = false)

  fun testCssVBindScriptSetup() =
    doRenameTest(newName = "newColor", dir = false)

  fun testCreateAppComponent() =
    doRenameTest(mainFile = "main.ts", newName = "NewCar")

  fun testCreateAppComponentFromUsage() =
    doRenameTest(mainFile = "App.vue", newName = "NewCar")

  fun testCreateAppDirective() =
    doRenameTest(mainFile = "main.ts", newName = "bar")

  fun testCreateAppDirectiveFromUsage() =
    doRenameTest(mainFile = "TheComponent.vue", newName = "bar")

  fun testNamespacedComponents() =
    doRenameTest(mainFile = "scriptSetup.vue", newName = "NewName")

  fun testCompositionApiLocalDirective() =
    doRenameTest(mainFile = "scriptSetup.vue", newName = "vNewName")

  fun testModelDeclaration() =
    doRenameTest(newName = "alignment", dir = false)

  fun testModelDeclarationWithVar() =
    doRenameTest(newName = "alignment", dir = false)

  fun testModelDeclarationProp() =
    doRenameTest(mainFile = "ModelDeclarationProp.vue", newName = "count")

  fun testModelDeclarationEvent() =
    doRenameTest(mainFile = "ModelDeclarationEvent.vue", newName = "count")

  fun testInjectLiteral() =
    doRenameTest(mainFile = "InjectLiteral.vue", newName = "newName")

  fun testComponentFile() =
    withRenameUsages(false) {
      checkFileRename("OrdersListView.vue", "SomeComponent.vue", searchCommentsAndText = false)
    }

  fun testComponentFileWithUsages() =
    withRenameUsages(true) {
      checkFileRename("OrdersListView.vue", "SomeComponent.vue", searchCommentsAndText = false)
    }

  fun testComponentFileWithReexports() =
    withRenameUsages(true) {
      checkFileRename("OrdersListView.vue", "SomeComponent.vue", searchCommentsAndText = false)
    }

  fun testPropsOptionsFromDefinition() =
    doRenameTest(newName = "newName", dir = false)

  fun testPropsOptionsFromUsage1() =
    doRenameTest(newName = "newName23", dir = false)

  fun testPropsOptionsFromUsage2() =
    doRenameTest(newName = "newName23", dir = false)

  fun testPropsOptionsFromUsage3() =
    doRenameTest(newName = "newName23", dir = false)

  fun testPropsOptionsUpperCaseFromDefinition() =
    doRenameTest(newName = "NewName", dir = false)

  fun testPropsOptionsNumberFromDefinition() =
    doRenameTest(newName = "newName23", dir = false)

  fun testPropsOptionsExtUsageFromDefinition() =
    doRenameTest(mainFile = "MyComponent.vue", newName = "newName")

  fun testPropsOptionsExtUsageFromUsage() =
    doRenameTest(mainFile = "MyUsage.vue", newName = "newName23")

  fun testPropsStringsFromDefinition() =
    doRenameTest(newName = "newName", dir = false)

  fun testPropsStringsFromUsage1() =
    doRenameTest(newName = "newName", dir = false)

  fun testPropsStringsFromUsage2() =
    doRenameTest(newName = "newName", dir = false)

  fun testPropsStringsExtUsageFromDefinition() =
    doRenameTest(mainFile = "MyComponent.vue", newName = "newName")

  fun testPropsStringsExtUsageFromUsage() =
    doRenameTest(mainFile = "MyUsage.vue", newName = "newName")

  fun testDefinePropsRecordTypeFromDefinition() =
    doRenameTest(newName = "newName", dir = false)

  fun testDefinePropsRecordTypeFromUsage1() =
    doRenameTest(newName = "newName", dir = false)

  fun testDefinePropsRecordTypeFromUsage2() =
    doRenameTest(newName = "newName", dir = false)

  fun testDefinePropsRecordTypeExtUsageFromDefinition() =
    doRenameTest(mainFile = "MyComponent.vue", newName = "newName")

  fun testDefinePropsRecordTypeExtUsageFromUsage() =
    doRenameTest(mainFile = "MyUsage.vue", newName = "newName")

  fun testDefinePropsArrayLiteralFromDefinition() =
    doRenameTest(newName = "newName", dir = false)

  fun testDefinePropsArrayLiteralFromUsage1() =
    doRenameTest(newName = "newName", dir = false)

  fun testDefinePropsArrayLiteralFromUsage2() =
    doRenameTest(newName = "newName", dir = false)

  fun testDefinePropsArrayLiteralExtUsageFromDefinition() =
    doRenameTest(mainFile = "MyComponent.vue", newName = "newName")

  fun testDefinePropsArrayLiteralExtUsageFromUsage() =
    doRenameTest(mainFile = "MyUsage.vue", newName = "newName")

  fun testDefinePropsObjectLiteralFromDefinition() =
    doRenameTest(newName = "newName", dir = false)

  fun testDefinePropsObjectLiteralFromUsage1() =
    doRenameTest(newName = "newName", dir = false)

  fun testDefinePropsObjectLiteralFromUsage2() =
    doRenameTest(newName = "newName", dir = false)

  fun testDefinePropsObjectLiteralExtUsageFromDefinition() =
    doRenameTest(mainFile = "MyComponent.vue", newName = "newName")

  fun testDefinePropsObjectLiteralExtUsageFromUsage() =
    doRenameTest(mainFile = "MyUsage.vue", newName = "newName")

  fun testDefinePropsInterfaceFromDefinition() =
    doRenameTest(newName = "newName", dir = false)

  fun testDefinePropsInterfaceFromUsage1() =
    doRenameTest(newName = "newName", dir = false)

  fun testDefinePropsInterfaceFromUsage2() =
    doRenameTest(newName = "newName", dir = false)

  fun testDefinePropsInterfaceExtUsageFromDefinition() =
    doRenameTest(mainFile = "MyComponent.vue", newName = "newName")

  fun testDefinePropsInterfaceExtUsageFromUsage() =
    doRenameTest(mainFile = "MyUsage.vue", newName = "newName")

  fun testDefinePropsExtInterfaceFromDefinition() =
    doRenameTest(mainFile = "fooProps.ts", newName = "newName")

  fun testDefinePropsExtInterfaceFromUsage1() =
    doRenameTest(mainFile = "definePropsInterface.vue", newName = "newName")

  fun testDefinePropsExtInterfaceFromUsage2() =
    doRenameTest(mainFile = "definePropsInterface.vue", newName = "newName")

  fun testDefinePropsExtInterfaceExtUsageFromDefinition() =
    doRenameTest(mainFile = "fooProps.ts", newName = "newName")

  fun testDefinePropsExtInterfaceExtUsageFromUsage() =
    doRenameTest(mainFile = "MyUsage.vue", newName = "newName")

  fun testComponentFromFunctionPlugin_renameFromDeclaration() {
    doRenameTest(
      mainFile = "global-components.ts",
      newName = "OtherButtonFromPlugin",
    )
  }

  fun testComponentFromFunctionPlugin_renameFromUsage() {
    doRenameTest(
      mainFile = "App.vue",
      newName = "OtherButtonFromPlugin",
    )
  }

  fun testComponentFromNestedFunctionPlugin_renameFromDeclaration() {
    doRenameTest(
      mainFile = "other-global-components.js",
      newName = "OtherLabelFromPlugin",
    )
  }

  fun testComponentFromNestedFunctionPlugin_renameFromUsage() {
    doRenameTest(
      mainFile = "App.vue",
      newName = "OtherLabelFromPlugin",
    )
  }

  fun testComponentFromNestedFunctionPluginWithCycle_renameFromDeclaration() {
    doRenameTest(
      mainFile = "other-global-components.js",
      newName = "OtherLabelFromPlugin",
    )
  }

  fun testComponentFromNestedFunctionPluginWithCycle_renameFromUsage() {
    doRenameTest(
      mainFile = "App.vue",
      newName = "OtherLabelFromPlugin",
    )
  }

  fun testComponentFromObjectPlugin_renameFromDeclaration() {
    doRenameTest(
      mainFile = "global-components.ts",
      newName = "OtherButtonFromPlugin",
    )
  }

  fun testComponentFromObjectPlugin_renameFromUsage() {
    doRenameTest(
      mainFile = "App.vue",
      newName = "OtherButtonFromPlugin",
    )
  }

  fun testComponentFromNestedObjectPlugin_renameFromDeclaration() {
    doRenameTest(
      mainFile = "other-global-components.js",
      newName = "OtherLabelFromPlugin",
    )
  }

  fun testComponentFromNestedObjectPlugin_renameFromUsage() {
    doRenameTest(
      mainFile = "App.vue",
      newName = "OtherLabelFromPlugin",
    )
  }

  fun testComponentFromNestedObjectPluginWithCycle_renameFromDeclaration() {
    doRenameTest(
      mainFile = "other-global-components.js",
      newName = "OtherLabelFromPlugin",
    )
  }

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
