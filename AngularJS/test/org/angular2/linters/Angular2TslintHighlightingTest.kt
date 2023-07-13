// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.linters

import com.intellij.codeInspection.InspectionProfileEntry
import com.intellij.lang.javascript.linter.LinterHighlightingTest
import com.intellij.lang.javascript.linter.tslint.TslintUtil
import com.intellij.lang.javascript.linter.tslint.highlight.TsLintInspection
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VfsUtilCore
import org.angularjs.AngularTestUtil
import java.io.IOException

class Angular2TslintHighlightingTest : LinterHighlightingTest() {
  @Throws(Exception::class)
  override fun setUp() {
    super.setUp()
    myFixture.setTestDataPath(AngularTestUtil.getBaseTestDataPath() + "linters/tslint")
  }

  override fun getInspection(): InspectionProfileEntry {
    return TsLintInspection()
  }

  override fun getPackageName(): String {
    return TslintUtil.PACKAGE_NAME
  }

  override fun getGlobalPackageVersionsToInstall(): Map<String, String?> {
    val map = HashMap<String, String?>()
    map["tslint"] = null
    map["typescript"] = null
    map["codelyzer"] = null
    map["@angular/core"] = null
    map["@angular/compiler"] = null
    map["zone.js"] = null
    map["rxjs"] = null
    return map
  }

  fun testErrorsFromTemplateFileFilteredInTs() {
    doEditorHighlightingTest<RuntimeException>("app.component.ts") { patchAdditionalRulesDir() }
  }

  fun testErrorsHighlightedInInlineTemplate() {
    doEditorHighlightingTest<RuntimeException>("app.component.ts") { patchAdditionalRulesDir() }
  }

  private fun patchAdditionalRulesDir() {
    val tslintJsonVFile = myFixture.findFileInTempDir("tslint.json")
    try {
      val text = VfsUtilCore.loadText(tslintJsonVFile)
      WriteAction.run<IOException> {
        val tslintPackageVFile = LocalFileSystem.getInstance().findFileByPath(
          nodePackage.systemDependentPath)!!
        val codelyzerPackageVFile = tslintPackageVFile.getParent().findChild("codelyzer")!!
        val textAfter = text.replace(RULES_DIRECTORY_TO_PATCH_MARKER, codelyzerPackageVFile.getPath())
        VfsUtil.saveText(tslintJsonVFile, textAfter)
      }
    }
    catch (e: IOException) {
      throw RuntimeException(e)
    }
  }

  companion object {
    private const val RULES_DIRECTORY_TO_PATCH_MARKER = "<RULES_DIRECTORY_TO_PATCH>"
  }
}
