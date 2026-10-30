// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang

import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.jsFixture
import com.intellij.lang.javascript.typescript.TypeScriptHighlightingFixture
import com.intellij.lang.javascript.typescript.TypeScriptHighlightingTest
import com.intellij.lang.javascript.typescript.TypeScriptHighlightingTestSimpleHelper
import com.intellij.lang.javascript.typescript.TypeScriptHighlightingWithoutServerTest
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.testFramework.junit5.fixture.TestFixture
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.lang.html.VueLanguage
import org.junit.jupiter.api.Test

class VueTypeScriptHighlightingTest : TypeScriptHighlightingWithoutServerTest() {
  override fun createFixture(): TestFixture<TypeScriptHighlightingFixture> =
    jsFixture("vueTypeScriptHighlightingFixture", projectFixture, TypeScriptHighlightingFixture.defaultOptions()) { fixture, testClass, testMethod, options ->
      object : TypeScriptHighlightingFixture(fixture, testClass, testMethod, options, true) {
        override fun doTestFor(checkWeakWarnings: Boolean, vararg fileNames: String) {
          LOG.info("Running overridden code for vue")
          if (toFix.contains(getTestName(false))) {
            LOG.info("Skipping muted test")
            return
          }
          if (fileNames.size == 1 && fileNames[0].endsWith(".d.ts")) {
            LOG.info("Skipping because only .d.ts file for test")
            return
          }
          if (fileNames.size > 1) {
            //TODO. the only case that should be skipped is when the first file is not a ES6 module.
            LOG.info("Skipping because several files")
            return
          }

          super.doTestFor(checkWeakWarnings, *fileNames)
        }

        override fun configureEditorFile(name: String) {
          val tsFile = StandardFileSystems.local().findFileByPath("$testDataPath/$name")
          val text: Any = StringUtil.convertLineSeparators(VfsUtil.loadText(tsFile!!))
          this.fixture.configureByText(VueFileType, surroundWithScriptTag(text))
        }

        override fun checkEditorText(ext: String) {
          val tsFile = StandardFileSystems.local().findFileByPath("$testDataPath/${getTestName(false)}_after.$ext")
          val text: Any = StringUtil.convertLineSeparators(VfsUtil.loadText(tsFile!!))
          this.fixture.checkResult(surroundWithScriptTag(text))
        }

        override fun checkHighlightingByRelativePath(relativePath: String) {
          val text = surroundWithScriptTag(loadText(relativePath))
          checkHighlightByFile(this.fixture, relativePath, text)
        }
      }
    }

  override fun helper(): TypeScriptHighlightingTest.TypeScriptHighlightTestHelper = object : TypeScriptHighlightingTestSimpleHelper(ts) {
    override fun doHighlightingWithInvokeFixAndCheckResult(fixName: String, ext: String, vararg files: String) {
      JSTestUtils.testWithTempCodeStyleSettings<Throwable>(ts.project) { styleSettings ->
        styleSettings.getCommonSettings(VueLanguage).indentOptions?.let {
          it.INDENT_SIZE = 4
          it.TAB_SIZE = 4
          it.CONTINUATION_INDENT_SIZE = 8
        }
        super.doHighlightingWithInvokeFixAndCheckResult(fixName, ext, *files)
      }
    }
  }

  // these tests need to be ignored with additional code:
  @Test
  override fun testIntermediateResultsNotCachedForRecursiveTypes() {
    //skip
  }

  @Test
  override fun testTypeScriptImportStatementType() {
    //skip
  }

  @Test
  override fun testPrivateBrandCheck_js() {
    // skip
    // See https://youtrack.jetbrains.com/issue/WEB-57683/
  }

  @Test
  override fun testAddExcessMembersToOtherFile() {
    // inapplicable
  }

  override fun getPerformanceTestName(): String {
    return "VueTypeScriptHighlighting - Highlighting rx observable"
  }
}

private val LOG = logger<VueTypeScriptHighlightingTest>()

private val toFix = setOf(
  "SemanticKeywords", //different visibility
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

private fun surroundWithScriptTag(text: Any) = "<script lang=\"ts\">\n$text\n</script>"
