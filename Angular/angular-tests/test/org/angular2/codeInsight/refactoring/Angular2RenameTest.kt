// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.refactoring

import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.typescript.formatter.TypeScriptCodeStyleSettings
import com.intellij.openapi.ui.TestDialog
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.css.CssBundle
import org.angular2.Angular2TestCase
import org.angular2.Angular2TestModule
import org.angular2.TestNoService
import org.angular2.TestTsGoFork
import org.junit.Test

@TestNoService
@TestTsGoFork
class Angular2RenameTest : Angular2TestCase("refactoring/rename") {

  @Test
  fun testRenameComponentFromStringUsage() =
    doSymbolRenameTest("test.component.ts", "newName", searchCommentsAndText = true)

  @Test
  fun testComponentFieldFromTemplate() =
    doSymbolRenameTest("test.component.html", "newName")

  @Test
  fun testI18nAttribute() =
    doSymbolRenameTest("directive.ts", "new-name")

  @Test
  fun testLocalInTemplate() =
    doSymbolRenameTest("test.component.html", "newName")

  @Test
  fun testReferenceFromTS() =
    doSymbolRenameTest("test.component.ts", "newReference")

  @Test
  fun testReferenceFromHTML() =
    doSymbolRenameTest("test.component.html", "newReference")

  @Test
  fun testReferenceFromTSNoStrings() =
    doSymbolRenameTest("test.component.ts", "newReference")

  @Test
  fun testReferenceFromHTMLNoStrings() =
    doSymbolRenameTest("test.component.html", "newReference")

  @Test
  fun testPipeFromHTML() =
    doSymbolRenameTest("test.component.html", "bar", searchCommentsAndText = true)

  @Test
  fun testPipeFromHTMLNoStrings() =
    doSymbolRenameTest("test.component.html", "bar")

  @Test
  fun testPipeFromTS() =
    doSymbolRenameTest("foo.pipe.ts", "bar", searchCommentsAndText = true)

  @Test
  fun testPipeFromTS2() =
    doSymbolRenameTest("foo.pipe.ts", "bar", searchCommentsAndText = true)

  @Test
  fun testPipeFromTS2NoStrings() =
    doSymbolRenameTest("foo.pipe.ts", "bar")

  @Test
  fun testComponentWithRelatedFiles() =
    withTempCodeStyleSettings { t: CodeStyleSettings ->
      t.getCustomSettings(TypeScriptCodeStyleSettings::class.java).FILE_NAME_STYLE = JSCodeStyleSettings.JSFileNameStyle.PASCAL_CASE
      doSymbolRenameTest("foo-bar.component.ts", "NewNameComponent", testDialog = TestDialog.OK)
    }

  @Test
  fun testComponentFile() =
    doFileRenameTest("new-name.component.ts", "foo-bar.component.ts", testDialog = TestDialog.OK)

  @Test
  fun testComponentToNonComponentName() =
    doSymbolRenameTest("foo-bar.component.ts", "NewNameSomething", testDialog = TestDialog.OK)

  @Test
  fun testModuleToNameWithoutPrefix() =
    doSymbolRenameTest("foo.module.ts", "Module", testDialog = TestDialog.OK)

  @Test
  fun testInjectionReparse() =
    doSymbolRenameTest("foo.component.html", "product", testDialog = TestDialog.OK)

  @Test
  fun testNgContentSelector() =
    doSymbolRenameTest("slots.component.ts", "new-tag")

  @Test
  fun testDirectiveTag() =
    doSymbolRenameTest("tag.html", "foo-bar2")

  @Test
  fun testDirectiveTagNormalized() =
    doSymbolRenameTest("tag.html", "fooBar2")

  @Test
  fun testDirectiveAttribute() =
    doSymbolRenameTest("attribute2.html", "foo-bar2")

  @Test
  fun testDirectiveAttributeNormalized() =
    doSymbolRenameTest("attribute2.html", "fooBar2")

  @Test
  fun testDirectiveBinding() =
    doSymbolRenameTest("binding.html", "model2")

  @Test
  fun testDirective() =
    doSymbolRenameTest("directive2.ts", "foo-bar2")

  @Test
  fun testDirectiveEventHandler() =
    doSymbolRenameTest("event.html", "complete2")

  @Test
  fun testExportAs() =
    doSymbolRenameTest("directives/bold.directive.ts", "bolder")

  @Test
  fun testExportAsFromUsage() =
    doSymbolRenameTest("app.component.html", "bolder")

  @Test
  fun testDirectiveInputFieldDecoratorObject() = doSymbolRenameTest("newInput", dir = false)

  @Test
  fun testDirectiveInputMappedObject() = doSymbolRenameTest("newInput", dir = false)

  @Test
  fun testDirectiveInputMappedObjectFromUsage() = doSymbolRenameTest("newInput", dir = false)

  @Test
  fun testDirectiveInputForwardedString() = doSymbolRenameTest("newInput", dir = false)

  @Test
  fun testDirectiveInputMappedStringNoField() = doSymbolRenameTest("newInput", dir = false)

  @Test
  fun testHostDirectiveInputForwarded() = doSymbolRenameTest("newInput", dir = false)

  @Test
  fun testHostDirectiveInputForwardedFromUsage() = doSymbolRenameTest("newInput", dir = false)

  @Test
  fun testHostDirectiveInputMappedSource() = doSymbolRenameTest("newInput", dir = false)

  @Test
  fun testHostDirectiveInputMappedTarget() = doSymbolRenameTest("newInput", dir = false)

  @Test
  fun testHostDirectiveInputMappedTargetBadSource() = doSymbolRenameTest("newInput", dir = false)

  @Test
  fun testHostDirectiveOneTimeBinding() = doSymbolRenameTest("newInput", dir = false)

  @Test
  fun testHostDirectiveOutputForwarded() = doSymbolRenameTest("newOutput", dir = false)

  @Test
  fun testHostDirectiveOutputMappedSource() = doSymbolRenameTest("newOutput", dir = false)

  @Test
  fun testHostDirectiveOutputMappedSourceFromUsage() = doSymbolRenameTest("newOutput", dir = false)

  @Test
  fun testHostDirectiveOutputMappedTarget() = doSymbolRenameTest("newOutput", dir = false)

  @Test
  fun testHostDirectiveOutputMappedTargetFromUsage() = doSymbolRenameTest("newOutput", dir = false)

  @Test
  fun testHostDirectiveOutputWithJsTextRefToFilterOut() =
    doSymbolRename("data-source.directive.ts", "newOutput", Angular2TestModule.ANGULAR_CORE_15_1_5)

  @Test
  fun testStructuralDirectiveWithNgTemplateSelector1() =
    doSymbolRenameTest("appFoo", dir = false)

  @Test
  fun testStructuralDirectiveWithNgTemplateSelector2() =
    doSymbolRenameTest("appFoo", dir = false)

  @Test
  fun testSignalInputFromDeclaration() =
    doSymbolRenameTest("newInput", dir = false)

  @Test
  fun testSignalInputFromUsage() =
    doSymbolRenameTest("newInput", dir = false)

  @Test
  fun testSignalInputRequiredFromDeclaration() =
    doSymbolRenameTest("newInput", dir = false)

  @Test
  fun testSignalInputAliasedFromDeclaration() =
    doSymbolRenameTest("newAliasedInput", dir = false)

  @Test
  fun testSignalInputAliasedFromUsage() =
    doSymbolRenameTest("newAliasedInput", dir = false)

  @Test
  fun testSignalInputAliasedRequiredFromDeclaration() =
    doSymbolRenameTest("newAliasedInput", dir = false)

  @Test
  fun testSignalInputAliasedRequiredFromUsage() =
    doSymbolRenameTest("newAliasedInput", dir = false)

  @Test
  fun testSignalOutputFromDeclaration() =
    doSymbolRenameTest("newOutput", dir = false)

  @Test
  fun testSignalOutputFromUsage() =
    doSymbolRenameTest("newOutput", dir = false)

  @Test
  fun testSignalOutputAliasedFromDeclaration() =
    doSymbolRenameTest("newAliasedOutput", dir = false)

  @Test
  fun testSignalOutputAliasedFromUsage() =
    doSymbolRenameTest("newAliasedOutput", dir = false)

  @Test
  fun testSignalOutputFromObservableFromDeclaration() =
    doSymbolRenameTest("newOutput", dir = false)

  @Test
  fun testSignalOutputFromObservableFromUsage() =
    doSymbolRenameTest("newOutput", dir = false)

  @Test
  fun testSignalOutputFromObservableAliasedFromDeclaration() =
    doSymbolRenameTest("newAliasedOutput", dir = false)

  @Test
  fun testSignalOutputFromObservableAliasedFromUsage() =
    doSymbolRenameTest("newAliasedOutput", dir = false)

  @Test
  fun testSignalModelFromDeclaration() =
    doSymbolRenameTest("newModel", dir = false)

  @Test
  fun testSignalModelRequiredFromDeclaration() =
    doSymbolRenameTest("newModel", dir = false)

  @Test
  fun testSignalModelFromUsage1() =
    doSymbolRenameTest("newModel", dir = false)

  @Test
  fun testSignalModelFromUsage2() =
    doSymbolRenameTest("newModel", dir = false)

  @Test
  fun testSignalModelFromUsage3() =
    doSymbolRenameTest("newModel", dir = false)

  @Test
  fun testSignalModelAliasedFromDeclaration() =
    doSymbolRenameTest("newAliasedModel", dir = false)

  @Test
  fun testSignalModelAliasedFromUsage1() =
    doSymbolRenameTest("newAliasedModel", dir = false)

  @Test
  fun testSignalModelAliasedFromUsage2() =
    doSymbolRenameTest("newAliasedModel", dir = false)

  @Test
  fun testSignalModelAliasedFromUsage3() =
    doSymbolRenameTest("newAliasedModel", dir = false)

  @Test
  fun testSignalModelAliasedRequiredFromUsage() =
    doSymbolRenameTest("newAliasedModel", dir = false)

  @Test
  fun testInputAndSelectorFromSelector() =
    doSymbolRenameTest("my-foo", dir = false)

  @Test
  fun testInputAndSelectorFromInput() =
    doSymbolRenameTest("my-foo", dir = false)

  @Test
  fun testInputAndSelectorFromUsage() =
    doSymbolRenameTest("my-foo", dir = false)

  @Test
  fun testViewChildrenDecoratorHtml() =
    doSymbolRenameTest("viewChildrenDecoratorHtml.html", "myFoo", dir = true)

  @Test
  fun testViewChildDecorator() =
    doSymbolRenameTest("myFoo", dir = false)

  // TODO - requires renaming several symbols at once
  fun _testViewChildrenDecorator() =
    doSymbolRenameTest("my-foo", dir = false)

  @Test
  fun testTemplateBindingKeyFromFieldSameFileExternalTemplate() =
    doSymbolRenameTest("appClicksFoo", dir = true)

  @Test
  fun testTemplateBindingKeyFromFieldSameFileInlineTemplate() =
    doSymbolRenameTest("appClicksFoo", dir = false)

  @Test
  fun testTemplateBindingKeyFromFieldDifferentFile() =
    doSymbolRenameTest("appClicksFoo", dir = true)

  @Test
  fun testTemplateBindingKeyFromTemplateBindingSameFileExternalTemplate() =
    doSymbolRenameTest("appClicksFoo", dir = true, extension = "html")

  @Test
  fun testTemplateBindingKeyFromTemplateBindingSameFileInlineTemplate() =
    doSymbolRenameTest("appClicksFoo", dir = false)

  @Test
  fun testTemplateBindingKeyFromInputBindingSameFileExternalTemplate() =
    doSymbolRenameTest("appClicksFoo", dir = true, extension = "html")

  @Test
  fun testTemplateBindingKeyFromInputBindingSameFileInlineTemplate() =
    doSymbolRenameTest("appClicksFoo", dir = false)

  @Test
  fun testTemplateBindingKeyFromTemplateBindingDifferentFile() =
    doSymbolRenameTest("component.ts", "appClicksFoo", dir = true)

  @Test
  fun testTemplateBindingKeyFromLiteralSameFileInlineTemplate() =
    doSymbolRenameTest("appClicksFoo", dir = false)

  @Test
  fun testTemplateBindingKeyFromInputMappingSameFileInlineTemplate() =
    doSymbolRenameTest("appClicksFoo", dir = false)

  @Test
  fun testHostAttributeToken() =
    doSymbolRenameTest("test", dir = false)

  @Test
  fun testConstructorAttribute() =
    doSymbolRenameTest("test", dir = false)

  @Test
  fun testCssCustomProperty() =
    doSymbolRenameTest("--foo", dir = false)

  @Test
  fun testCssCustomPropertyWrongName() =
    assertThrows(IllegalArgumentException::class.java,
                 CssBundle.message("css.rename.custom-property.error.must-start-with-two-dashes")) {
      doSymbolRenameTest("cssCustomProperty.ts", "foo", dir = false)
    }

  @Test
  fun testCssCustomPropertyEmptyName() =
    assertThrows(IllegalArgumentException::class.java,
                 CssBundle.message("css.rename.custom-property.error..must-not-be-empty")) {
      doSymbolRenameTest("cssCustomProperty.ts", "--", dir = false)
    }

  @Test
  fun testCssCustomPropertyFromCss() =
    doSymbolRenameTest("test.css", "--foo", dir = true)

  @Test
  fun testCssCustomPropertyFromCssHostBinding() =
    doSymbolRenameTest("cssCustomProperty.ts", "--foo", dir = true)

  @Test
  fun testNewAngularAnimationBindings() =
    doSymbolRename("newName", Angular2TestModule.ANGULAR_CORE_20_2_2, dir = false)

  @Test
  fun testArrowFunctionParamInterpolationInlineFromDefinition() =
    doSymbolRenameTest("newName", dir = false)

  @Test
  fun testArrowFunctionParamInterpolationInlineFromUsage() =
    doSymbolRenameTest("newName", dir = false)

  @Test
  fun testArrowFunctionParamInterpolationExternalFromDefinition() =
    doSymbolRenameTest("template.html", "newName", dir = true)

  @Test
  fun testArrowFunctionParamInterpolationExternalFromUsage() =
    doSymbolRenameTest("template.html", "newName", dir = true)

  @Test
  fun testArrowFunctionParamHostBindingFromDefinition() =
    doSymbolRenameTest("newName", dir = false)

  @Test
  fun testArrowFunctionParamHostBindingFromUsage() =
    doSymbolRenameTest("newName", dir = false)

  @Test
  fun testArrowFunctionParamLetBlockFromDefinition() =
    doSymbolRenameTest("newName", dir = false)

  @Test
  fun testArrowFunctionParamLetBlockFromUsage() =
    doSymbolRenameTest("newName", dir = false)

}
