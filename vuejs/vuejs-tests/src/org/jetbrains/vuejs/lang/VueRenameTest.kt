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
    checkSymbolRename("newName", dir = false)

  fun testComponentFieldFromStringUsageInTemplate() =
    checkSymbolRename("newName", dir = false)

  fun testTemplateLocalVariable() =
    checkSymbolRename("newName", dir = false)

  fun testDestructuringInVFor() =
    checkSymbolRename("newName", dir = false)

  fun testSlotProps() =
    checkSymbolRename("newName", dir = false)

  fun testQualifiedWatchProperty() =
    checkSymbolRename("newName", dir = false)

  fun testWatchProperty() =
    checkSymbolRename("newName", dir = false)

  fun testInlineFieldRename() =
    doConfiguredTest(checkResult = true) {
      CodeInsightTestUtil.doInlineRename(VariableInplaceRenameHandler(), "foo", myFixture)
    }

  fun testComponentNameFromDeclaration() =
    checkSymbolRename("componentNameFromDeclaration1.vue", "AfterComponent")

  fun testComponentNameFromPropertyName() =
    checkSymbolRename("AfterComponent")

  fun testCssVBind() =
    checkSymbolRename("newColor", dir = false)

  fun testCssVBindScriptSetup() =
    checkSymbolRename("newColor", dir = false)

  fun testCreateAppComponent() =
    checkSymbolRename("main.ts", "NewCar", VueTestModule.VUE_3_2_2)

  fun testCreateAppComponentFromUsage() =
    checkSymbolRename("App.vue", "NewCar", VueTestModule.VUE_3_2_2)

  fun testCreateAppDirective() =
    checkSymbolRename("main.ts", "bar", VueTestModule.VUE_3_2_2)

  fun testCreateAppDirectiveFromUsage() =
    checkSymbolRename("TheComponent.vue", "bar", VueTestModule.VUE_3_2_2)

  fun testNamespacedComponents() =
    checkSymbolRename("scriptSetup.vue", "NewName", VueTestModule.VUE_3_2_2)

  fun testCompositionApiLocalDirective() =
    checkSymbolRename("scriptSetup.vue", "vNewName", VueTestModule.VUE_3_2_2)

  fun testModelDeclaration() =
    checkSymbolRename("alignment", VueTestModule.VUE_3_3_4, dir = false)

  fun testModelDeclarationWithVar() =
    checkSymbolRename("alignment", VueTestModule.VUE_3_3_4, dir = false)

  fun testModelDeclarationProp() =
    checkSymbolRename("ModelDeclarationProp.vue", "count", VueTestModule.VUE_3_2_2)

  fun testModelDeclarationEvent() =
    checkSymbolRename("ModelDeclarationEvent.vue", "count", VueTestModule.VUE_3_2_2)

  fun testInjectLiteral() =
    checkSymbolRename("InjectLiteral.vue", "newName", VueTestModule.VUE_3_3_4)

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
    checkSymbolRename("newName", dir = false)

  fun testPropsOptionsFromUsage1() =
    checkSymbolRename("newName23", dir = false)

  fun testPropsOptionsFromUsage2() =
    checkSymbolRename("newName23", dir = false)

  fun testPropsOptionsFromUsage3() =
    checkSymbolRename("newName23", dir = false)

  fun testPropsOptionsUpperCaseFromDefinition() =
    checkSymbolRename("NewName", dir = false)

  fun testPropsOptionsNumberFromDefinition() =
    checkSymbolRename("newName23", dir = false)

  fun testPropsOptionsExtUsageFromDefinition() =
    checkSymbolRename("MyComponent.vue", "newName")

  fun testPropsOptionsExtUsageFromUsage() =
    checkSymbolRename("MyUsage.vue", "newName23")

  fun testPropsStringsFromDefinition() =
    checkSymbolRename("newName", dir = false)

  fun testPropsStringsFromUsage1() =
    checkSymbolRename("newName", dir = false)

  fun testPropsStringsFromUsage2() =
    checkSymbolRename("newName", dir = false)

  fun testPropsStringsExtUsageFromDefinition() =
    checkSymbolRename("MyComponent.vue", "newName")

  fun testPropsStringsExtUsageFromUsage() =
    checkSymbolRename("MyUsage.vue", "newName")

  fun testDefinePropsRecordTypeFromDefinition() =
    checkSymbolRename("newName", VueTestModule.VUE_3_3_4, dir = false)

  fun testDefinePropsRecordTypeFromUsage1() =
    checkSymbolRename("newName", VueTestModule.VUE_3_3_4, dir = false)

  fun testDefinePropsRecordTypeFromUsage2() =
    checkSymbolRename("newName", VueTestModule.VUE_3_3_4, dir = false)

  fun testDefinePropsRecordTypeExtUsageFromDefinition() =
    checkSymbolRename("MyComponent.vue", "newName", VueTestModule.VUE_3_3_4)

  fun testDefinePropsRecordTypeExtUsageFromUsage() =
    checkSymbolRename("MyUsage.vue", "newName", VueTestModule.VUE_3_3_4)

  fun testDefinePropsArrayLiteralFromDefinition() =
    checkSymbolRename("newName", VueTestModule.VUE_3_3_4, dir = false)

  fun testDefinePropsArrayLiteralFromUsage1() =
    checkSymbolRename("newName", VueTestModule.VUE_3_3_4, dir = false)

  fun testDefinePropsArrayLiteralFromUsage2() =
    checkSymbolRename("newName", VueTestModule.VUE_3_3_4, dir = false)

  fun testDefinePropsArrayLiteralExtUsageFromDefinition() =
    checkSymbolRename("MyComponent.vue", "newName", VueTestModule.VUE_3_3_4)

  fun testDefinePropsArrayLiteralExtUsageFromUsage() =
    checkSymbolRename("MyUsage.vue", "newName", VueTestModule.VUE_3_3_4)

  fun testDefinePropsObjectLiteralFromDefinition() =
    checkSymbolRename("newName", VueTestModule.VUE_3_3_4, dir = false)

  fun testDefinePropsObjectLiteralFromUsage1() =
    checkSymbolRename("newName", VueTestModule.VUE_3_3_4, dir = false)

  fun testDefinePropsObjectLiteralFromUsage2() =
    checkSymbolRename("newName", VueTestModule.VUE_3_3_4, dir = false)

  fun testDefinePropsObjectLiteralExtUsageFromDefinition() =
    checkSymbolRename("MyComponent.vue", "newName", VueTestModule.VUE_3_3_4)

  fun testDefinePropsObjectLiteralExtUsageFromUsage() =
    checkSymbolRename("MyUsage.vue", "newName", VueTestModule.VUE_3_3_4)

  fun testDefinePropsInterfaceFromDefinition() =
    checkSymbolRename("newName", VueTestModule.VUE_3_3_4, dir = false)

  fun testDefinePropsInterfaceFromUsage1() =
    checkSymbolRename("newName", VueTestModule.VUE_3_3_4, dir = false)

  fun testDefinePropsInterfaceFromUsage2() =
    checkSymbolRename("newName", VueTestModule.VUE_3_3_4, dir = false)

  fun testDefinePropsInterfaceExtUsageFromDefinition() =
    checkSymbolRename("MyComponent.vue", "newName", VueTestModule.VUE_3_3_4)

  fun testDefinePropsInterfaceExtUsageFromUsage() =
    checkSymbolRename("MyUsage.vue", "newName", VueTestModule.VUE_3_3_4)

  fun testDefinePropsExtInterfaceFromDefinition() =
    checkSymbolRename("fooProps.ts", "newName", VueTestModule.VUE_3_3_4)

  fun testDefinePropsExtInterfaceFromUsage1() =
    checkSymbolRename("definePropsInterface.vue", "newName", VueTestModule.VUE_3_3_4)

  fun testDefinePropsExtInterfaceFromUsage2() =
    checkSymbolRename("definePropsInterface.vue", "newName", VueTestModule.VUE_3_3_4)

  fun testDefinePropsExtInterfaceExtUsageFromDefinition() =
    checkSymbolRename("fooProps.ts", "newName", VueTestModule.VUE_3_3_4)

  fun testDefinePropsExtInterfaceExtUsageFromUsage() =
    checkSymbolRename("MyUsage.vue", "newName", VueTestModule.VUE_3_3_4)
  fun testComponentFromFunctionPlugin_renameFromDeclaration() {
    checkSymbolRename(
      mainFile = "global-components.ts",
      newName = "OtherButtonFromPlugin",
      modules = arrayOf(VueTestModule.VUE_3_4_0),
    )
  }

  fun testComponentFromFunctionPlugin_renameFromUsage() {
    checkSymbolRename(
      mainFile = "App.vue",
      newName = "OtherButtonFromPlugin",
      modules = arrayOf(VueTestModule.VUE_3_4_0),
    )
  }

  fun testComponentFromNestedFunctionPlugin_renameFromDeclaration() {
    checkSymbolRename(
      mainFile = "other-global-components.js",
      newName = "OtherLabelFromPlugin",
      modules = arrayOf(VueTestModule.VUE_3_4_0),
    )
  }

  fun testComponentFromNestedFunctionPlugin_renameFromUsage() {
    checkSymbolRename(
      mainFile = "App.vue",
      newName = "OtherLabelFromPlugin",
      modules = arrayOf(VueTestModule.VUE_3_4_0),
    )
  }

  fun testComponentFromNestedFunctionPluginWithCycle_renameFromDeclaration() {
    checkSymbolRename(
      mainFile = "other-global-components.js",
      newName = "OtherLabelFromPlugin",
      modules = arrayOf(VueTestModule.VUE_3_4_0),
    )
  }

  fun testComponentFromNestedFunctionPluginWithCycle_renameFromUsage() {
    checkSymbolRename(
      mainFile = "App.vue",
      newName = "OtherLabelFromPlugin",
      modules = arrayOf(VueTestModule.VUE_3_4_0),
    )
  }

  fun testComponentFromObjectPlugin_renameFromDeclaration() {
    checkSymbolRename(
      mainFile = "global-components.ts",
      newName = "OtherButtonFromPlugin",
      modules = arrayOf(VueTestModule.VUE_3_4_0),
    )
  }

  fun testComponentFromObjectPlugin_renameFromUsage() {
    checkSymbolRename(
      mainFile = "App.vue",
      newName = "OtherButtonFromPlugin",
      modules = arrayOf(VueTestModule.VUE_3_4_0),
    )
  }

  fun testComponentFromNestedObjectPlugin_renameFromDeclaration() {
    checkSymbolRename(
      mainFile = "other-global-components.js",
      newName = "OtherLabelFromPlugin",
      modules = arrayOf(VueTestModule.VUE_3_4_0),
    )
  }

  fun testComponentFromNestedObjectPlugin_renameFromUsage() {
    checkSymbolRename(
      mainFile = "App.vue",
      newName = "OtherLabelFromPlugin",
      modules = arrayOf(VueTestModule.VUE_3_4_0),
    )
  }

  fun testComponentFromNestedObjectPluginWithCycle_renameFromDeclaration() {
    checkSymbolRename(
      mainFile = "other-global-components.js",
      newName = "OtherLabelFromPlugin",
      modules = arrayOf(VueTestModule.VUE_3_4_0),
    )
  }

  fun testComponentFromNestedObjectPluginWithCycle_renameFromUsage() {
    checkSymbolRename(
      mainFile = "App.vue",
      newName = "OtherLabelFromPlugin",
      modules = arrayOf(VueTestModule.VUE_3_4_0),
    )
  }

}
