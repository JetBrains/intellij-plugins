// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.refactoring

import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.typescript.formatter.TypeScriptCodeStyleSettings
import com.intellij.openapi.ui.TestDialog
import com.intellij.psi.codeStyle.CodeStyleSettings
import org.angular2.Angular2TestCase

class Angular2RenameTest : Angular2TestCase("refactoring/rename") {

  fun testRenameComponentFromStringUsage() =
    checkSymbolRename("newName", "test.component.ts")

  fun testComponentFieldFromTemplate() =
    checkSymbolRename("newName", "test.component.html")

  fun testI18nAttribute() =
    checkSymbolRename("new-name", "directive.ts")

  fun testLocalInTemplate() =
    checkSymbolRename("newName", "test.component.html")

  fun testReferenceFromTS() =
    checkSymbolRename("newReference", "test.component.ts")

  fun testReferenceFromHTML() =
    checkSymbolRename("newReference", "test.component.html")

  fun testReferenceFromTSNoStrings() =
    checkSymbolRename("newReference", "test.component.ts", searchCommentsAndText = false)

  fun testReferenceFromHTMLNoStrings() =
    checkSymbolRename("newReference", "test.component.html", searchCommentsAndText = false)

  fun testPipeFromHTML() =
    checkSymbolRename("bar", "test.component.html")

  fun testPipeFromHTMLNoStrings() =
    checkSymbolRename("bar", "test.component.html", searchCommentsAndText = false)

  fun testPipeFromTS() =
    checkSymbolRename("bar", "foo.pipe.ts")

  fun testPipeFromTS2() =
    checkSymbolRename("bar", "foo.pipe.ts")

  fun testPipeFromTS2NoStrings() =
    checkSymbolRename("bar", "foo.pipe.ts", searchCommentsAndText = false)

  fun testComponentWithRelatedFiles() =
    withTempCodeStyleSettings { t: CodeStyleSettings ->
      t.getCustomSettings(TypeScriptCodeStyleSettings::class.java).FILE_NAME_STYLE = JSCodeStyleSettings.JSFileNameStyle.PASCAL_CASE
      checkSymbolRename("NewNameComponent", "foo-bar.component.ts", testDialog = TestDialog.OK)
    }

  fun testComponentFile() =
    checkFileRename("new-name.component.ts", "foo-bar.component.ts", testDialog = TestDialog.OK)

  fun testComponentToNonComponentName() =
    checkSymbolRename("NewNameSomething", "foo-bar.component.ts", testDialog = TestDialog.OK)

  fun testModuleToNameWithoutPrefix() =
    checkSymbolRename("Module", "foo.module.ts", testDialog = TestDialog.OK)

  fun testInjectionReparse() =
    checkSymbolRename("product", "foo.component.html", testDialog = TestDialog.OK)

  fun testNgContentSelector() =
    checkSymbolRename("new-tag", "slots.component.ts")

  fun testDirectiveTag() =
    checkSymbolRename("foo-bar2", "tag.html")

  fun testDirectiveTagNormalized() =
    checkSymbolRename("fooBar2", "tag.html")

  fun testDirectiveAttribute() =
    checkSymbolRename("foo-bar2", "attribute2.html")

  fun testDirectiveAttributeNormalized() =
    checkSymbolRename("fooBar2", "attribute2.html")

  fun testDirectiveBinding() =
    checkSymbolRename("model2", "binding.html")

  fun testDirective() =
    checkSymbolRename("foo-bar2", "directive2.ts")

  fun testDirectiveEventHandler() =
    checkSymbolRename("complete2", "event.html")

  fun testExportAs() =
    checkSymbolRename("bolder", "app.component.html")

}
