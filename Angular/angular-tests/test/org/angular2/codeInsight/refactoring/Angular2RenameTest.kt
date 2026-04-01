// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.refactoring

import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.typescript.formatter.TypeScriptCodeStyleSettings
import com.intellij.openapi.ui.TestDialog
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.css.CssBundle
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule

class Angular2RenameTest : Angular2TestCase("refactoring/rename", false) {

  fun testRenameComponentFromStringUsage() =
    doSymbolRenameTest("test.component.ts", "newName", searchCommentsAndText = true)

  fun testComponentFieldFromTemplate() =
    doSymbolRenameTest("test.component.html", "newName")

  fun testI18nAttribute() =
    doSymbolRenameTest("directive.ts", "new-name")

  fun testLocalInTemplate() =
    doSymbolRenameTest("test.component.html", "newName")

  fun testReferenceFromTS() =
    doSymbolRenameTest("test.component.ts", "newReference")

  fun testReferenceFromHTML() =
    doSymbolRenameTest("test.component.html", "newReference")

  fun testReferenceFromTSNoStrings() =
    doSymbolRenameTest("test.component.ts", "newReference")

  fun testReferenceFromHTMLNoStrings() =
    doSymbolRenameTest("test.component.html", "newReference")

  fun testPipeFromHTML() =
    doSymbolRenameTest("test.component.html", "bar", searchCommentsAndText = true)

  fun testPipeFromHTMLNoStrings() =
    doSymbolRenameTest("test.component.html", "bar")

  fun testPipeFromTS() =
    doSymbolRenameTest("foo.pipe.ts", "bar", searchCommentsAndText = true)

  fun testPipeFromTS2() =
    doSymbolRenameTest("foo.pipe.ts", "bar", searchCommentsAndText = true)

  fun testPipeFromTS2NoStrings() =
    doSymbolRenameTest("foo.pipe.ts", "bar")

  fun testComponentWithRelatedFiles() =
    withTempCodeStyleSettings { t: CodeStyleSettings ->
      t.getCustomSettings(TypeScriptCodeStyleSettings::class.java).FILE_NAME_STYLE = JSCodeStyleSettings.JSFileNameStyle.PASCAL_CASE
      doSymbolRenameTest("foo-bar.component.ts", "NewNameComponent", testDialog = TestDialog.OK)
    }

  fun testComponentFile() =
    doFileRenameTest("new-name.component.ts", "foo-bar.component.ts", testDialog = TestDialog.OK)

  fun testComponentToNonComponentName() =
    doSymbolRenameTest("foo-bar.component.ts", "NewNameSomething", testDialog = TestDialog.OK)

  fun testModuleToNameWithoutPrefix() =
    doSymbolRenameTest("foo.module.ts", "Module", testDialog = TestDialog.OK)

  fun testInjectionReparse() =
    doSymbolRenameTest("foo.component.html", "product", testDialog = TestDialog.OK)

  fun testNgContentSelector() =
    doSymbolRenameTest("slots.component.ts", "new-tag")

  fun testDirectiveTag() =
    doSymbolRenameTest("tag.html", "foo-bar2")

  fun testDirectiveTagNormalized() =
    doSymbolRenameTest("tag.html", "fooBar2")

  fun testDirectiveAttribute() =
    doSymbolRenameTest("attribute2.html", "foo-bar2")

  fun testDirectiveAttributeNormalized() =
    doSymbolRenameTest("attribute2.html", "fooBar2")

  fun testDirectiveBinding() =
    doSymbolRenameTest("binding.html", "model2")

  fun testDirective() =
    doSymbolRenameTest("directive2.ts", "foo-bar2")

  fun testDirectiveEventHandler() =
    doSymbolRenameTest("event.html", "complete2")

  fun testExportAs() =
    doSymbolRenameTest("directives/bold.directive.ts", "bolder")

  fun testExportAsFromUsage() =
    doSymbolRenameTest("app.component.html", "bolder")

  fun testDirectiveInputFieldDecoratorObject() = doSymbolRenameTest("newInput", dir = false)

  fun testDirectiveInputMappedObject() = doSymbolRenameTest("newInput", dir = false)

  fun testDirectiveInputMappedObjectFromUsage() = doSymbolRenameTest("newInput", dir = false)

  fun testDirectiveInputForwardedString() = doSymbolRenameTest("newInput", dir = false)

  fun testDirectiveInputMappedStringNoField() = doSymbolRenameTest("newInput", dir = false)

  fun testHostDirectiveInputForwarded() = doSymbolRenameTest("newInput", dir = false)

  fun testHostDirectiveInputForwardedFromUsage() = doSymbolRenameTest("newInput", dir = false)

  fun testHostDirectiveInputMappedSource() = doSymbolRenameTest("newInput", dir = false)

  fun testHostDirectiveInputMappedTarget() = doSymbolRenameTest("newInput", dir = false)

  fun testHostDirectiveInputMappedTargetBadSource() = doSymbolRenameTest("newInput", dir = false)

  fun testHostDirectiveOneTimeBinding() = doSymbolRenameTest("newInput", dir = false)

  fun testHostDirectiveOutputForwarded() = doSymbolRenameTest("newOutput", dir = false)

  fun testHostDirectiveOutputMappedSource() = doSymbolRenameTest("newOutput", dir = false)

  fun testHostDirectiveOutputMappedSourceFromUsage() = doSymbolRenameTest("newOutput", dir = false)

  fun testHostDirectiveOutputMappedTarget() = doSymbolRenameTest("newOutput", dir = false)

  fun testHostDirectiveOutputMappedTargetFromUsage() = doSymbolRenameTest("newOutput", dir = false)

  fun testHostDirectiveOutputWithJsTextRefToFilterOut() =
    doSymbolRename("data-source.directive.ts", "newOutput", Angular2TestModule.ANGULAR_CORE_15_1_5)

  fun testStructuralDirectiveWithNgTemplateSelector1() =
    doSymbolRenameTest("appFoo", dir = false)

  fun testStructuralDirectiveWithNgTemplateSelector2() =
    doSymbolRenameTest("appFoo", dir = false)

  fun testSignalInputFromDeclaration() =
    doSymbolRenameTest("newInput", dir = false)

  fun testSignalInputFromUsage() =
    doSymbolRenameTest("newInput", dir = false)

  fun testSignalInputRequiredFromDeclaration() =
    doSymbolRenameTest("newInput", dir = false)

  fun testSignalInputAliasedFromDeclaration() =
    doSymbolRenameTest("newAliasedInput", dir = false)

  fun testSignalInputAliasedFromUsage() =
    doSymbolRenameTest("newAliasedInput", dir = false)

  fun testSignalInputAliasedRequiredFromDeclaration() =
    doSymbolRenameTest("newAliasedInput", dir = false)

  fun testSignalInputAliasedRequiredFromUsage() =
    doSymbolRenameTest("newAliasedInput", dir = false)

  fun testSignalOutputFromDeclaration() =
    doSymbolRenameTest("newOutput", dir = false)

  fun testSignalOutputFromUsage() =
    doSymbolRenameTest("newOutput", dir = false)

  fun testSignalOutputAliasedFromDeclaration() =
    doSymbolRenameTest("newAliasedOutput", dir = false)

  fun testSignalOutputAliasedFromUsage() =
    doSymbolRenameTest("newAliasedOutput", dir = false)

  fun testSignalOutputFromObservableFromDeclaration() =
    doSymbolRenameTest("newOutput", dir = false)

  fun testSignalOutputFromObservableFromUsage() =
    doSymbolRenameTest("newOutput", dir = false)

  fun testSignalOutputFromObservableAliasedFromDeclaration() =
    doSymbolRenameTest("newAliasedOutput", dir = false)

  fun testSignalOutputFromObservableAliasedFromUsage() =
    doSymbolRenameTest("newAliasedOutput", dir = false)

  fun testSignalModelFromDeclaration() =
    doSymbolRenameTest("newModel", dir = false)

  fun testSignalModelRequiredFromDeclaration() =
    doSymbolRenameTest("newModel", dir = false)

  fun testSignalModelFromUsage1() =
    doSymbolRenameTest("newModel", dir = false)

  fun testSignalModelFromUsage2() =
    doSymbolRenameTest("newModel", dir = false)

  fun testSignalModelFromUsage3() =
    doSymbolRenameTest("newModel", dir = false)

  fun testSignalModelAliasedFromDeclaration() =
    doSymbolRenameTest("newAliasedModel", dir = false)

  fun testSignalModelAliasedFromUsage1() =
    doSymbolRenameTest("newAliasedModel", dir = false)

  fun testSignalModelAliasedFromUsage2() =
    doSymbolRenameTest("newAliasedModel", dir = false)

  fun testSignalModelAliasedFromUsage3() =
    doSymbolRenameTest("newAliasedModel", dir = false)

  fun testSignalModelAliasedRequiredFromUsage() =
    doSymbolRenameTest("newAliasedModel", dir = false)

  fun testInputAndSelectorFromSelector() =
    doSymbolRenameTest("my-foo", dir = false)

  fun testInputAndSelectorFromInput() =
    doSymbolRenameTest("my-foo", dir = false)

  fun testInputAndSelectorFromUsage() =
    doSymbolRenameTest("my-foo", dir = false)

  fun testViewChildrenDecoratorHtml() =
    doSymbolRenameTest("viewChildrenDecoratorHtml.html", "myFoo", dir = true)

  fun testViewChildDecorator() =
    doSymbolRenameTest("myFoo", dir = false)

  // TODO - requires renaming several symbols at once
  fun _testViewChildrenDecorator() =
    doSymbolRenameTest("my-foo", dir = false)

  fun testTemplateBindingKeyFromFieldSameFileExternalTemplate() =
    doSymbolRenameTest("appClicksFoo", dir = true)

  fun testTemplateBindingKeyFromFieldSameFileInlineTemplate() =
    doSymbolRenameTest("appClicksFoo", dir = false)

  fun testTemplateBindingKeyFromFieldDifferentFile() =
    doSymbolRenameTest("appClicksFoo", dir = true)

  fun testTemplateBindingKeyFromTemplateBindingSameFileExternalTemplate() =
    doSymbolRenameTest("appClicksFoo", dir = true, extension = "html")

  fun testTemplateBindingKeyFromTemplateBindingSameFileInlineTemplate() =
    doSymbolRenameTest("appClicksFoo", dir = false)

  fun testTemplateBindingKeyFromInputBindingSameFileExternalTemplate() =
    doSymbolRenameTest("appClicksFoo", dir = true, extension = "html")

  fun testTemplateBindingKeyFromInputBindingSameFileInlineTemplate() =
    doSymbolRenameTest("appClicksFoo", dir = false)

  fun testTemplateBindingKeyFromTemplateBindingDifferentFile() =
    doSymbolRenameTest("component.ts", "appClicksFoo", dir = true)

  fun testTemplateBindingKeyFromLiteralSameFileInlineTemplate() =
    doSymbolRenameTest("appClicksFoo", dir = false)

  fun testTemplateBindingKeyFromInputMappingSameFileInlineTemplate() =
    doSymbolRenameTest("appClicksFoo", dir = false)

  fun testHostAttributeToken() =
    doSymbolRenameTest("test", dir = false)

  fun testConstructorAttribute() =
    doSymbolRenameTest("test", dir = false)

  fun testCssCustomProperty() =
    doSymbolRenameTest("--foo", dir = false)

  fun testCssCustomPropertyWrongName() =
    assertThrows(IllegalArgumentException::class.java,
                 CssBundle.message("css.rename.custom-property.error.must-start-with-two-dashes")) {
      doSymbolRenameTest("cssCustomProperty.ts", "foo", dir = false)
    }

  fun testCssCustomPropertyEmptyName() =
    assertThrows(IllegalArgumentException::class.java,
                 CssBundle.message("css.rename.custom-property.error..must-not-be-empty")) {
      doSymbolRenameTest("cssCustomProperty.ts", "--", dir = false)
    }

  fun testCssCustomPropertyFromCss() =
    doSymbolRenameTest("test.css", "--foo", dir = true)

  fun testCssCustomPropertyFromCssHostBinding() =
    doSymbolRenameTest("cssCustomProperty.ts", "--foo", dir = true)

  fun testNewAngularAnimationBindings() =
    doSymbolRename("newName", Angular2TestModule.ANGULAR_CORE_20_2_2, dir = false)

  fun testArrowFunctionParamInterpolationInlineFromDefinition() =
    doSymbolRenameTest("newName", dir = false)

  fun testArrowFunctionParamInterpolationInlineFromUsage() =
    doSymbolRenameTest("newName", dir = false)

  fun testArrowFunctionParamInterpolationExternalFromDefinition() =
    doSymbolRenameTest("template.html", "newName", dir = true)

  fun testArrowFunctionParamInterpolationExternalFromUsage() =
    doSymbolRenameTest("template.html", "newName", dir = true)

  fun testArrowFunctionParamHostBindingFromDefinition() =
    doSymbolRenameTest("newName", dir = false)

  fun testArrowFunctionParamHostBindingFromUsage() =
    doSymbolRenameTest("newName", dir = false)

  fun testArrowFunctionParamLetBlockFromDefinition() =
    doSymbolRenameTest("newName", dir = false)

  fun testArrowFunctionParamLetBlockFromUsage() =
    doSymbolRenameTest("newName", dir = false)

}
