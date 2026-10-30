// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzerSettings
import com.intellij.codeInsight.daemon.LineMarkerSettings
import com.intellij.javascript.library.JSLibraryVersions
import com.intellij.lang.javascript.JSCodeInsightFixture.Companion.assertNotEmpty
import com.intellij.lang.javascript.JSDaemonAnalyzerFixture
import com.intellij.lang.javascript.JSTestOption
import com.intellij.lang.javascript.JSTestOptions
import com.intellij.lang.javascript.JSTestUtils
import com.intellij.lang.javascript.TypeScriptTestUtil
import com.intellij.lang.javascript.highlighting.JavaScriptLineMarkerProvider
import com.intellij.lang.javascript.jsFixture
import com.intellij.lang.javascript.jsProjectFixture
import com.intellij.lang.javascript.typescript.TypeScriptHighlightingFixture
import com.intellij.lang.typescript.inspections.TypeScriptSmartCastInspection
import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.ex.EditorSettingsExternalizable
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.testFramework.junit5.TestApplication
import org.jetbrains.vuejs.lang.html.VueFileType
import org.junit.jupiter.api.Test

/**
 * Runs the TypeScript line marker tests on Vue files.
 */
@TestApplication
class VueTypeScriptLineMarkersTest {
  private val js by jsFixture("vueTypeScriptLineMarkersFixture", projectFixture, TypeScriptHighlightingFixture.defaultOptions()) {
    fixture, testClass, testMethod, options ->
    object : TypeScriptHighlightingFixture(fixture, testClass, testMethod, options, true) {
      override fun doTestFor(checkWeakWarnings: Boolean, vararg fileNames: String) {
        LOG.info("Running overridden code for vue")
        if (fileNames.getOrNull(0)?.endsWith(".d.ts") == true) {
          LOG.info("Skipping because only .d.ts file for test")
          return
        }
        super.doTestFor(checkWeakWarnings, *fileNames)
      }

      override fun configureEditorFile(name: String) {
        val tsFile = StandardFileSystems.local().findFileByPath("$testDataPath/$name")
        var text: String = StringUtil.convertLineSeparators(VfsUtil.loadText(tsFile!!))
        val testName = getTestName(true)
        localVarsMap[testName]?.forEach {
          text = text.replace("<symbolName descr=\"identifiers//global variable\">$it", "<symbolName descr=\"identifiers//local variable\">$it")
        }
        localFunsMap[testName]?.forEach {
          text = text.replace("<symbolName descr=\"identifiers//global function\">$it", "<symbolName descr=\"identifiers//local function\">$it")
        }
        this.fixture.configureByText(VueFileType, surroundWithScriptTag(text))
      }

      override fun checkEditorText(ext: String) {
        val tsFile = StandardFileSystems.local().findFileByPath("$testDataPath/${getTestName(false)}_after.$ext")
        val text: Any = StringUtil.convertLineSeparators(VfsUtil.loadText(tsFile!!))
        this.fixture.checkResult(surroundWithScriptTag(text))
      }

      override fun checkHighlightingByRelativePath(relativePath: String) {
        val text = surroundWithScriptTag(JSDaemonAnalyzerFixture.loadText(relativePath))
        JSDaemonAnalyzerFixture.checkHighlightByFile(this.fixture, relativePath, text)
      }
    }
  }

  private fun disableJSGutterOption(key: String) {
    val option = JavaScriptLineMarkerProvider.getOption(key)
    val oldValue = option!!.isEnabled
    LineMarkerSettings.getSettings().setEnabled(option, false)
    Disposer.register(js.testRootDisposable, Disposable { LineMarkerSettings.getSettings().setEnabled(option, oldValue) })
  }

  private fun defaultLineMarkersTest() {
    js.defaultTest()
    JSTestUtils.runInBackgroundReadActionAndWaitVoid {
      val lineMarkers = JSTestUtils.collectLineMarkersForFile(js.fixture.getFile())
      for (lineMarker in lineMarkers) {
        if (lineMarker.getNavigationHandler() == null) continue  // Recursive markers, line markers

        assertNotEmpty(JSTestUtils.getLineMarkerNavigationTargets(lineMarker).toList())
      }
    }
  }

  @Test
  @JSTestOptions(JSTestOption.WithSymbolNames)
  fun testInterfaceMethodCall() {
    JSLibraryVersions.mockTS5(js.testRootDisposable)
    defaultLineMarkersTest()
  }

  @Test
  @JSTestOptions(JSTestOption.WithSymbolNames)
  fun testInterfaceWithConstructSignature() {
    defaultLineMarkersTest() // WEB-77329
  }

  @Test
  @JSTestOptions(JSTestOption.WithSymbolNames)
  fun testPropertyMarker() {
    defaultLineMarkersTest()
  }

  @Test
  @JSTestOptions(JSTestOption.WithInfos, JSTestOption.WithSymbolNames)
  fun testGenericsMarker() {
    defaultLineMarkersTest()
  }

  @Test
  @JSTestOptions(JSTestOption.WithSymbolNames)
  fun testSemanticHighlightingBasic() {
    defaultLineMarkersTest()
  }

  @Test
  @JSTestOptions(JSTestOption.WithInfos, JSTestOption.WithSymbolNames)
  fun testSemanticHighlighting() {
    js.defaultTestForTwoFiles()
  }

  @Test
  @JSTestOptions(JSTestOption.WithSymbolNames)
  fun testSemanticHighlightingInModule() {
    js.defaultTestForTwoFiles()
  }

  @Test
  @JSTestOptions(JSTestOption.WithInfos, JSTestOption.WithSymbolNames)
  fun testNamespaceImportColor() {
    defaultLineMarkersTest()
  }

  @Test
  @JSTestOptions(JSTestOption.WithInfos, JSTestOption.WithSymbolNames)
  fun testFieldParameterSemantic() {
    defaultLineMarkersTest()
  }

  private fun doTestSmartCastHighlighting() {
    val docOnHover = EditorSettingsExternalizable.getInstance().isShowQuickDocOnMouseOverElement
    try {
      js.fixture.enableInspections(TypeScriptSmartCastInspection())
      EditorSettingsExternalizable.getInstance().setShowQuickDocOnMouseOverElement(false)
      defaultLineMarkersTest()
    }
    finally {
      EditorSettingsExternalizable.getInstance().setShowQuickDocOnMouseOverElement(docOnHover)
    }
  }

  @Test
  @JSTestOptions(JSTestOption.WithInfos, JSTestOption.WithSymbolNames)
  fun testTypeGuardNarrowedHighlighting() {
    JSLibraryVersions.mockTS5(js.testRootDisposable)
    doTestSmartCastHighlighting()
  }

  @Test
  @JSTestOptions(JSTestOption.WithInfos, JSTestOption.WithSymbolNames)
  fun testTypeGuardHighlightingWithImplicit() {
    doTestSmartCastHighlighting()
  }

  @Test
  @JSTestOptions(JSTestOption.WithInfos, JSTestOption.WithSymbolNames)
  fun testTypeGuardsHighlightingOptionals() {
    TypeScriptTestUtil.setStrictNullChecks(js.project, js.testRootDisposable)
    doTestSmartCastHighlighting()
  }

  @Test
  fun testLineMarkerDisableImplemented() {
    disableJSGutterOption("js.implemented")
    defaultLineMarkersTest()
  }

  @Test
  fun testLineMarkerDisableImplementing() {
    disableJSGutterOption("js.implementing")
    defaultLineMarkersTest()
  }

  @Test
  fun testLineMarkerDisableOverridden() {
    disableJSGutterOption("js.overridden")
    defaultLineMarkersTest()
  }

  @Test
  fun testLineMarkerDisableOverriding() {
    disableJSGutterOption("js.overriding")
    defaultLineMarkersTest()
  }

  @Test
  fun testOverridingMarkers() {
    defaultLineMarkersTest()
  }

  @Test
  fun testMethodSeparators() {
    val myDaemonCodeAnalyzerSettings = DaemonCodeAnalyzerSettings.getInstance()
    myDaemonCodeAnalyzerSettings.SHOW_METHOD_SEPARATORS = true
    try {
      defaultLineMarkersTest()
    }
    finally {
      myDaemonCodeAnalyzerSettings.SHOW_METHOD_SEPARATORS = false
    }
  }

  @Test
  fun testOverrideInterfaceMarkers() {
    defaultLineMarkersTest()
  }

  @Test
  fun testImplementsMarkerForClass() {
    defaultLineMarkersTest()
  }

  @Test
  fun testInterfaceHierarchyMarkers() {
    defaultLineMarkersTest()
  }

  @Test
  fun testCheckFunctionTypeDeclaration() {
    defaultLineMarkersTest()
  }

  @Test
  fun testFieldImplementsMarkers() {
    defaultLineMarkersTest()
  }

  @Test
  @JSTestOptions(JSTestOption.AllowRecursionGuard)
  fun testRecursiveMarker() {
    defaultLineMarkersTest()
  }

  @Test
  fun testOptionalMethodImplement() {
    defaultLineMarkersTest()
  }

  @Test
  fun testOptionalMethodOverride() {
    defaultLineMarkersTest()
  }

  @Test
  fun testIntersectionImplementLineMarkers() {
    js.defaultTestForTwoFiles()
  }

  @Test
  fun testDestructuringWithNullVariable() {
    defaultLineMarkersTest()
  }

  @Test
  fun testOverrideMethodProperty() {
    defaultLineMarkersTest()
  }

  @Test
  fun testOverrideSetGet() {
    defaultLineMarkersTest()
  }

  @Test
  fun testImplementParameterFieldFromInterface() {
    defaultLineMarkersTest()
  }

  @Test
  fun testExtendFieldFromParameterField() {
    defaultLineMarkersTest()
  }

  @Test
  fun testExtendPrivateParameterFieldFromParameterField() {
    defaultLineMarkersTest()
  }

  @Test
  fun testExtendProtectedParameterFieldFromParameterField() {
    defaultLineMarkersTest()
  }

  @Test
  fun testExtendPublicParameterFieldFromParameterField() {
    defaultLineMarkersTest()
  }

  @Test
  fun testExtendClassFieldFromParameterField() {
    defaultLineMarkersTest()
  }

  @Test
  fun testExtendClassFieldFromClassField() {
    defaultLineMarkersTest()
  }

  @Test
  fun testOverrideParameterFieldWithDifferentAccessTypes() {
    defaultLineMarkersTest()
  }

  @Test
  fun testImplementInterfaceInHierarchy() {
    defaultLineMarkersTest()
  }

  @Test
  fun testImplementClass() {
    defaultLineMarkersTest()
  }

  @Test
  fun testOverrideWithSearchInHierarchy() {
    defaultLineMarkersTest()
  }

  @Test
  fun testOverrideConstructor() {
    defaultLineMarkersTest()
  }

  @Test
  fun testPropertyWithExtendsMarkers() {
    defaultLineMarkersTest()
  }

  @Test
  @JSTestOptions(JSTestOption.WithInfos, JSTestOption.WithSymbolNames)
  fun testInferGenericsHighlighting() {
    defaultLineMarkersTest()
  }

  @Test
  fun testPropertyImplementsContextualTypeMember() {
    defaultLineMarkersTest() // WEB-74577
  }

  companion object {
    private val projectFixture = jsProjectFixture()
    private val LOG = logger<VueTypeScriptLineMarkersTest>()
  }
}

private val localVarsMap = mapOf(
  Pair("propertyMarker", listOf("interfacePropertyMarker")),
  Pair("interfaceMethodCall", listOf("simpleObject")),
  Pair("namespaceImportColor", listOf("i")),
  Pair("semanticHighlighting", listOf("x", "s", "string", "i"))
)

private val localFunsMap = mapOf(
  Pair("typeGuardNarrowedHighlighting", listOf("assertNever", "q")),
  Pair("typeGuardHighlightingWithImplicit", listOf("foo")),
  Pair("typeGuardsHighlightingOptionals", listOf("foo", "zoo", "goo")),
  Pair("genericsMarker", listOf("funcGenerics", "funcGeneric"))
)

private fun surroundWithScriptTag(text: Any) = "<<symbolName descr=\"Custom tag name\">script</symbolName> lang=\"ts\">\n" +
                                               "$text\n" +
                                               "</<symbolName descr=\"Custom tag name\">script</symbolName>>"
