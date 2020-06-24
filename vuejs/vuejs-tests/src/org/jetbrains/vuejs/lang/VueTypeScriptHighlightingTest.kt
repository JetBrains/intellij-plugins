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

import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.lang.javascript.JSDaemonAnalyzerLightTestCase
import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.typescript.TypeScriptHighlightingTest
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.util.Function
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.lang.html.VueLanguage

class VueTypeScriptHighlightingTest : TypeScriptHighlightingTest() {
  private val toFix = setOf(
    "NodeDefTypes153",
    "NodeDefTypes",
    "GenericSeveralSignaturesWithConstraint",
    "ReExportDefaultSOE",
    "PrimitiveTypesAssignments",
    "PropertyMarker",
    "ExternalModulesMany",
    "ExtendingBuiltInTypes",
    "NamespaceImportColor",
    "ReExportAllModule",
    "ExportAssignmentAsExportDefault",
    "AbstractClassesKeywords",
    "ExtendLibString",
    "InterfaceMethodCall",
    "ImportSOE2",
    "SOEExportSpecifier",
    "RenameFileToTSX",
    "ExportSpecifierGlobalThing",
    "NestedModuleAugmentation",
    "ExtendStandardInterface"
  )

  override fun doTestFor(checkWeakWarnings: Boolean,
                         function: Function<MutableCollection<HighlightInfo>, Void>?,
                         vararg fileNames: String?) {
    LOG.info("Running overridden code for vue")
    if (skipTest()) {
      LOG.info("Skipping muted test")
      return
    }
    if (fileNames.size == 1 && fileNames[0]!!.endsWith(".d.ts")) {
      LOG.info("Skipping because only .d.ts file for test")
      return
    }
    if (fileNames.size > 1) {
      //TODO. the only case that should be skipped is when the first file is not a ES6 module.
      LOG.info("Skipping because several files")
      return
    }

    super.doTestFor(checkWeakWarnings, function, *fileNames)
  }
  
  private fun skipTest() = toFix.contains(getTestName(false))

  override fun configureEditorFile(name: String?) {
    val tsFile = LocalFileSystem.getInstance().findFileByPath("$testDataPath/$name")
    val text: Any = StringUtil.convertLineSeparators(VfsUtil.loadText(tsFile!!))
    myFixture.configureByText(VueFileType.INSTANCE, surroundWithScriptTag(text))
  }

  override fun checkEditorText(ext: String?) {
    val tsFile = LocalFileSystem.getInstance().findFileByPath("$testDataPath/${getTestName(false)}_after.$ext")
    val text: Any = StringUtil.convertLineSeparators(VfsUtil.loadText(tsFile!!))
    myFixture.checkResult(surroundWithScriptTag(text))
  }

  override fun checkHighlightingByRelativePath(relativePath: String?) {
    val text = surroundWithScriptTag(JSDaemonAnalyzerLightTestCase.loadText(relativePath))
    JSDaemonAnalyzerLightTestCase.checkHighlightByFile(myFixture, relativePath, text)
  }

  private fun surroundWithScriptTag(text: Any) = "<script lang=\"ts\">\n$text\n</script>"

  // these tests need to be ignored with additional code:
  override fun testIntermediateResultsNotCachedForRecursiveTypes() {
    LOG.info("Skipping muted test")
  }

  override fun doHighlightingWithInvokeFixAndCheckResult(fixName: String?, ext: String?, vararg files: String?) {
    JSTestUtils.testWithTempCodeStyleSettings<Throwable>(project) { styleSettings ->
      styleSettings.getCommonSettings(VueLanguage.INSTANCE).indentOptions?.let {
        it.INDENT_SIZE = 4
        it.TAB_SIZE = 4
        it.CONTINUATION_INDENT_SIZE = 8
      }
      super.doHighlightingWithInvokeFixAndCheckResult(fixName, ext, *files)
    }
  }
}
