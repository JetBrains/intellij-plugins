// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.refactoring

import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.typescript.formatter.TypeScriptCodeStyleSettings
import com.intellij.openapi.ui.TestDialog
import com.intellij.psi.codeStyle.CodeStyleSettings
import org.angular2.Angular2TestCase

class Angular2RenameTest : Angular2TestCase("refactoring/rename") {

  fun testRenameComponentFromStringUsage() =
    checkSymbolRename("test.component.ts", "newName")

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
    checkSymbolRename("test.component.ts", "newReference", searchCommentsAndText = false)

  fun testReferenceFromHTMLNoStrings() =
    checkSymbolRename("test.component.html", "newReference", searchCommentsAndText = false)

  fun testPipeFromHTML() =
    checkSymbolRename("test.component.html", "bar")

  fun testPipeFromHTMLNoStrings() =
    checkSymbolRename("test.component.html", "bar", searchCommentsAndText = false)

  fun testPipeFromTS() =
    checkSymbolRename("foo.pipe.ts", "bar")

  fun testPipeFromTS2() =
    checkSymbolRename("foo.pipe.ts", "bar")

  fun testPipeFromTS2NoStrings() =
    checkSymbolRename("foo.pipe.ts", "bar", searchCommentsAndText = false)

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

  fun testHostDirectiveOutputForwarded() = checkSymbolRename("newOutput", dir = false)

  fun testHostDirectiveOutputMappedSource() = checkSymbolRename("newOutput", dir = false)

  fun testHostDirectiveOutputMappedSourceFromUsage() = checkSymbolRename("newOutput", dir = false)

  fun testHostDirectiveOutputMappedTarget() = checkSymbolRename("newOutput", dir = false)

  fun testHostDirectiveOutputMappedTargetFromUsage() = checkSymbolRename("newOutput", dir = false)

}
