// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang

import com.intellij.lang.javascript.JSTestOption
import com.intellij.lang.javascript.JSTestOptions
import com.intellij.lang.javascript.JavaScriptFormatterTestBase
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.javascript.modules.JSImportHighlightingAndCompletionLightTestBase
import com.intellij.lang.javascript.modules.JSImportTestUtil
import com.intellij.openapi.util.registry.Registry
import com.intellij.psi.css.inspections.invalid.CssUnknownTargetInspection
import com.intellij.webpack.createAndSetWebpackConfig
import org.jetbrains.plugins.scss.inspections.SassScssUnresolvedMixinInspection
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.function.Consumer

private const val BASE_PATH = "/ts_imports"

@RunWith(JUnit4::class)
class VueModuleImportTest : JSImportHighlightingAndCompletionLightTestBase() {

  override fun getBasePath(): String = BASE_PATH
  override fun getExtension(): String = "vue"
  override fun getTestDataPath(): String = getVueTestDataPath() + basePath

  @Test
  fun testTypesModule() {
    doTestWithCopyDirectory()
  }

  @Test
  fun testSimpleDeclare() {
    doTestWithCopyDirectory()
  }

  @Test
  fun testReferenceGlobalTyping() {
    doTestWithCopyDirectory()
  }

  @Test
  fun testAutoImportFromTs() {
    doTestAutoImportWithCopyDirectory()
  }

  @Test
  fun testPathMappingResolve() {
    doTestAutoImportWithCopyDirectory()
  }

  @Test
  fun testAutoImportFromVue() {
    doTestAutoImportWithCopyDirectory()
  }

  @Test
  fun testAutoImportVueFileToTs() {
    doTestActionWithCopyDirectory(JSImportTestUtil.getActionName(), "ts", null, null)
  }

  @Test
  fun testAutoImportFromVueWithJs() {
    JavaScriptFormatterTestBase.setTempSettings(project, JavascriptLanguage, Consumer<JSCodeStyleSettings> {
      it.USE_EXPLICIT_JS_EXTENSION = JSCodeStyleSettings.UseExplicitExtension.ALWAYS_JS
    })

    doTestActionWithCopyDirectory("Insert 'import HelloWorld from \"./src/HelloWorld.vue\"'", "vue", null, null)
  }

  @Test
  fun testAmbientTypingsInVue() {
    doTestWithCopyDirectory()
  }

  @Test
  fun testSimpleVarFromES6() {
    doTestWithCopyDirectory()
    checkAfterFile(extension)
  }

  @Test
  fun testSimpleJSConfig() {
    doTestWithCopyDirectory()
    checkAfterFile(extension)
  }

  @JSTestOptions(selectLookupItem = 0)
  fun testEnumImport() {
    doTestWithCopyDirectory(1, true, extension)
    checkAfterFile(extension)
  }

  @Test
  fun testReExportWIthJs() {
    doTestWithCopyDirectory(1, true, "ts")
  }

  @Test
  @JSTestOptions(JSTestOption.WithInfos, JSTestOption.WithSymbolNames)
  fun testCustomComponentHighlighting() {
    doTestWithCopyDirectory()
  }

  @Test
  fun testCssReferencePathMapping() {
    myFixture.enableInspections(CssUnknownTargetInspection())
    myFixture.copyDirectoryToProject(getTestName(false), "")
    myFixture.configureFromTempProjectFile("src/${getTestName(false)}.vue")
    myFixture.testHighlighting()
  }

  @Test
  @JSTestOptions(selectLookupItem = 0)
  fun testVueFileNameCompletion() {
    doTestWithCopyDirectory()
    checkAfterFile(extension)
  }

  @Test
  fun testStylesResolvePathMapping() {
    //css doesn't have stubs for imports!
    Registry.get("ast.loading.filter").setValue(false, testRootDisposable)

    myFixture.enableInspections(CssUnknownTargetInspection(), SassScssUnresolvedMixinInspection())
    myFixture.copyDirectoryToProject(getTestName(false), "")
    myFixture.configureFromTempProjectFile("spa/src/testComponent.vue")
    myFixture.allowTreeAccessForAllFiles()
    myFixture.testHighlighting()
  }

  @Test
  fun testStylesResolvePathMappingTilde() {
    //css doesn't have stubs for imports!
    Registry.get("ast.loading.filter").setValue(false, testRootDisposable)

    myFixture.enableInspections(CssUnknownTargetInspection(), SassScssUnresolvedMixinInspection())
    myFixture.copyDirectoryToProject(getTestName(false), "")
    myFixture.configureFromTempProjectFile("spa/src/testComponent.vue")
    myFixture.allowTreeAccessForAllFiles()
    myFixture.testHighlighting()
  }

  @Test
  fun testTSOverVuePriority() {
    doHighlightOnlyTestWithCopyDirectory(extension)
  }

  @Test
  fun testJSOverVuePriority() {
    doHighlightOnlyTestWithCopyDirectory(extension)
  }

  @Test
  fun testAutoImportFromVueWebpack() {
    doTestActionWithCopyDirectory(JSImportTestUtil.getActionName(), "vue", Consumer {
      createAndSetWebpackConfig(project, testRootDisposable, "aliasPath", "src", null, it.path)
    })
  }

  @Test
  fun testAutoImportFromVueWebpackWithExtension() {
    doTestActionWithCopyDirectory(JSImportTestUtil.getActionName(), "vue", Consumer {
      createAndSetWebpackConfig(project, testRootDisposable, "aliasPath", "src", listOf(".vue"), it.path)
    })
  }
}
