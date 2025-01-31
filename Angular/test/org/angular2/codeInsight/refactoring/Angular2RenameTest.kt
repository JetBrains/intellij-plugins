// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.refactoring

import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.typescript.formatter.TypeScriptCodeStyleSettings
import com.intellij.openapi.ui.TestDialog
import com.intellij.psi.codeStyle.CodeStyleSettings
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule

class Angular2RenameTest : Angular2TestCase("refactoring/rename", false) {

  fun testRenameComponentFromStringUsage() =
    checkSymbolRename("test.component.ts", "newName", searchCommentsAndText = true)

  fun testComponentFieldFromTemplate() =
    checkSymbolRename("test.component.html", "newName")

  fun testI18nAttribute() =
    checkSymbolRename("directive.ts", "new-name")

  fun testLocalInTemplate() =
    checkSymbolRename("test.component.html", "newName")

  fun testReferenceFromTS() =
    checkSymbolRename("test.component.ts", "newReference")

  fun testReferenceFromHTML() =
    checkSymbolRename("test.component.html", "newReference")

  fun testReferenceFromTSNoStrings() =
    checkSymbolRename("test.component.ts", "newReference")

  fun testReferenceFromHTMLNoStrings() =
    checkSymbolRename("test.component.html", "newReference")

  fun testPipeFromHTML() =
    checkSymbolRename("test.component.html", "bar", searchCommentsAndText = true)

  fun testPipeFromHTMLNoStrings() =
    checkSymbolRename("test.component.html", "bar")

  fun testPipeFromTS() =
    checkSymbolRename("foo.pipe.ts", "bar", searchCommentsAndText = true)

  fun testPipeFromTS2() =
    checkSymbolRename("foo.pipe.ts", "bar", searchCommentsAndText = true)

  fun testPipeFromTS2NoStrings() =
    checkSymbolRename("foo.pipe.ts", "bar")

  fun testComponentWithRelatedFiles() =
    withTempCodeStyleSettings { t: CodeStyleSettings ->
      t.getCustomSettings(TypeScriptCodeStyleSettings::class.java).FILE_NAME_STYLE = JSCodeStyleSettings.JSFileNameStyle.PASCAL_CASE
      checkSymbolRename("foo-bar.component.ts", "NewNameComponent", testDialog = TestDialog.OK)
    }

  fun testComponentFile() =
    checkFileRename("new-name.component.ts", "foo-bar.component.ts", testDialog = TestDialog.OK)

  fun testComponentToNonComponentName() =
    checkSymbolRename("foo-bar.component.ts", "NewNameSomething", testDialog = TestDialog.OK)

  fun testModuleToNameWithoutPrefix() =
    checkSymbolRename("foo.module.ts", "Module", testDialog = TestDialog.OK)

  fun testInjectionReparse() =
    checkSymbolRename("foo.component.html", "product", testDialog = TestDialog.OK)

  fun testNgContentSelector() =
    checkSymbolRename("slots.component.ts", "new-tag")

  fun testDirectiveTag() =
    checkSymbolRename("tag.html", "foo-bar2")

  fun testDirectiveTagNormalized() =
    checkSymbolRename("tag.html", "fooBar2")

  fun testDirectiveAttribute() =
    checkSymbolRename("attribute2.html", "foo-bar2")

  fun testDirectiveAttributeNormalized() =
    checkSymbolRename("attribute2.html", "fooBar2")

  fun testDirectiveBinding() =
    checkSymbolRename("binding.html", "model2")

  fun testDirective() =
    checkSymbolRename("directive2.ts", "foo-bar2")

  fun testDirectiveEventHandler() =
    checkSymbolRename("event.html", "complete2")

  fun testExportAs() =
    checkSymbolRename("directives/bold.directive.ts", "bolder")

  fun testExportAsFromUsage() =
    checkSymbolRename("app.component.html", "bolder")

  fun testDirectiveInputFieldDecoratorObject() = checkSymbolRename("newInput", dir = false)

  fun testDirectiveInputMappedObject() = checkSymbolRename("newInput", dir = false)

  fun testDirectiveInputMappedObjectFromUsage() = checkSymbolRename("newInput", dir = false)

  fun testDirectiveInputForwardedString() = checkSymbolRename("newInput", dir = false)

  fun testDirectiveInputMappedStringNoField() = checkSymbolRename("newInput", dir = false)

  fun testHostDirectiveInputForwarded() = checkSymbolRename("newInput", dir = false)

  fun testHostDirectiveInputForwardedFromUsage() = checkSymbolRename("newInput", dir = false)

  fun testHostDirectiveInputMappedSource() = checkSymbolRename("newInput", dir = false)

  fun testHostDirectiveInputMappedTarget() = checkSymbolRename("newInput", dir = false)

  fun testHostDirectiveInputMappedTargetBadSource() = checkSymbolRename("newInput", dir = false)

  fun testHostDirectiveOneTimeBinding() = checkSymbolRename("newInput", dir = false)

  fun testHostDirectiveOutputForwarded() = checkSymbolRename("newOutput", dir = false)

  fun testHostDirectiveOutputMappedSource() = checkSymbolRename("newOutput", dir = false)

  fun testHostDirectiveOutputMappedSourceFromUsage() = checkSymbolRename("newOutput", dir = false)

  fun testHostDirectiveOutputMappedTarget() = checkSymbolRename("newOutput", dir = false)

  fun testHostDirectiveOutputMappedTargetFromUsage() = checkSymbolRename("newOutput", dir = false)

  fun testHostDirectiveOutputWithJsTextRefToFilterOut() =
    checkSymbolRename("data-source.directive.ts", "newOutput", Angular2TestModule.ANGULAR_CORE_15_1_5)

  fun testStructuralDirectiveWithNgTemplateSelector1() =
    checkSymbolRename("appFoo", dir = false)

  fun testStructuralDirectiveWithNgTemplateSelector2() =
    checkSymbolRename("appFoo", dir = false)

  fun testSignalInputFromDeclaration() =
    checkSymbolRename("newInput", dir = false)

  fun testSignalInputFromUsage() =
    checkSymbolRename("newInput", dir = false)

  fun testSignalInputRequiredFromDeclaration() =
    checkSymbolRename("newInput", dir = false)

  fun testSignalInputAliasedFromDeclaration() =
    checkSymbolRename("newAliasedInput", dir = false)

  fun testSignalInputAliasedFromUsage() =
    checkSymbolRename("newAliasedInput", dir = false)

  fun testSignalInputAliasedRequiredFromDeclaration() =
    checkSymbolRename("newAliasedInput", dir = false)

  fun testSignalInputAliasedRequiredFromUsage() =
    checkSymbolRename("newAliasedInput", dir = false)

  fun testSignalOutputFromDeclaration() =
    checkSymbolRename("newOutput", dir = false)

  fun testSignalOutputFromUsage() =
    checkSymbolRename("newOutput", dir = false)

  fun testSignalOutputAliasedFromDeclaration() =
    checkSymbolRename("newAliasedOutput", dir = false)

  fun testSignalOutputAliasedFromUsage() =
    checkSymbolRename("newAliasedOutput", dir = false)

  fun testSignalOutputFromObservableFromDeclaration() =
    checkSymbolRename("newOutput", dir = false)

  fun testSignalOutputFromObservableFromUsage() =
    checkSymbolRename("newOutput", dir = false)

  fun testSignalOutputFromObservableAliasedFromDeclaration() =
    checkSymbolRename("newAliasedOutput", dir = false)

  fun testSignalOutputFromObservableAliasedFromUsage() =
    checkSymbolRename("newAliasedOutput", dir = false)

  fun testSignalModelFromDeclaration() =
    checkSymbolRename("newModel", dir = false)

  fun testSignalModelRequiredFromDeclaration() =
    checkSymbolRename("newModel", dir = false)

  fun testSignalModelFromUsage1() =
    checkSymbolRename("newModel", dir = false)

  fun testSignalModelFromUsage2() =
    checkSymbolRename("newModel", dir = false)

  fun testSignalModelFromUsage3() =
    checkSymbolRename("newModel", dir = false)

  fun testSignalModelAliasedFromDeclaration() =
    checkSymbolRename("newAliasedModel", dir = false)

  fun testSignalModelAliasedFromUsage1() =
    checkSymbolRename("newAliasedModel", dir = false)

  fun testSignalModelAliasedFromUsage2() =
    checkSymbolRename("newAliasedModel", dir = false)

  fun testSignalModelAliasedFromUsage3() =
    checkSymbolRename("newAliasedModel", dir = false)

  fun testSignalModelAliasedRequiredFromUsage() =
    checkSymbolRename("newAliasedModel", dir = false)

  fun testInputAndSelectorFromSelector() =
    checkSymbolRename("my-foo", dir = false)

  fun testInputAndSelectorFromInput() =
    checkSymbolRename("my-foo", dir = false)

  fun testInputAndSelectorFromUsage() =
    checkSymbolRename("my-foo", dir = false)

  fun testViewChildrenDecoratorHtml() =
    checkSymbolRename("viewChildrenDecoratorHtml.html", "myFoo", dir = true)

  fun testViewChildDecorator() =
    checkSymbolRename("myFoo", dir = false)

  // TODO - requires renaming several symbols at once
  fun _testViewChildrenDecorator() =
    checkSymbolRename("my-foo", dir = false)

  fun testTemplateBindingKeyFromFieldSameFileExternalTemplate() =
    checkSymbolRename("appClicksFoo", dir = true)

  fun testTemplateBindingKeyFromFieldSameFileInlineTemplate() =
    checkSymbolRename("appClicksFoo", dir = false)

  fun testTemplateBindingKeyFromFieldDifferentFile() =
    checkSymbolRename("appClicksFoo", dir = true)

  fun testTemplateBindingKeyFromTemplateBindingSameFileExternalTemplate() =
    checkSymbolRename("appClicksFoo", dir = true, extension = "html")

  fun testTemplateBindingKeyFromTemplateBindingSameFileInlineTemplate() =
    checkSymbolRename("appClicksFoo", dir = false)

  fun testTemplateBindingKeyFromInputBindingSameFileExternalTemplate() =
    checkSymbolRename("appClicksFoo", dir = true, extension = "html")

  fun testTemplateBindingKeyFromInputBindingSameFileInlineTemplate() =
    checkSymbolRename("appClicksFoo", dir = false)

  fun testTemplateBindingKeyFromTemplateBindingDifferentFile() =
    checkSymbolRename("component.ts", "appClicksFoo", dir = true)

  fun testTemplateBindingKeyFromLiteralSameFileInlineTemplate() =
    checkSymbolRename("appClicksFoo", dir = false)

  fun testTemplateBindingKeyFromInputMappingSameFileInlineTemplate() =
    checkSymbolRename("appClicksFoo", dir = false)
}
