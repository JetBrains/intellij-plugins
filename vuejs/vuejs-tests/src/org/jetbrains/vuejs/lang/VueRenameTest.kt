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

  fun testPropsBindings() =
    checkSymbolRename("PropsBindings.vue","aCoolMessage", VueTestModule.VUE_3_3_4)

  fun testModelDeclarationWithVar() =
    checkSymbolRename("alignment", VueTestModule.VUE_3_3_4, dir = false)

  fun testModelDeclarationProp() =
    checkSymbolRename("ModelDeclarationProp.vue", "count", VueTestModule.VUE_3_2_2)

  fun testModelDeclarationEvent() =
    checkSymbolRename("ModelDeclarationEvent.vue", "count", VueTestModule.VUE_3_2_2)

  fun testDefinePropsRecordType() =
    checkSymbolRename("alignment", VueTestModule.VUE_3_3_4, dir = false)

  fun testDefinePropsArrayLiteral() =
    checkSymbolRename("alignment", VueTestModule.VUE_3_3_4, dir = false)

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

  fun testPropsOptionsUpperCaseFromDefinition() =
    checkSymbolRename("NewName", dir = false)

  fun testPropsOptionsNumberFromDefinition() =
    checkSymbolRename("newName23", dir = false)

  fun testPropsStringsFromDefinition() =
    checkSymbolRename("newName", dir = false)

  fun testPropsStringsFromUsage() =
    checkSymbolRename("newName", dir = false)

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
