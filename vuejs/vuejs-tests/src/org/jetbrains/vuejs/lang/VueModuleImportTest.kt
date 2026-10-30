// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang

import com.intellij.lang.javascript.JSFixtureOptions
import com.intellij.lang.javascript.JSTestOption
import com.intellij.lang.javascript.JSTestOptions
import com.intellij.lang.javascript.JavaScriptFormatterTestBase
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings
import com.intellij.lang.javascript.jsProjectFixture
import com.intellij.lang.javascript.modules.JSImportHighlightingAndCompletionFixture.Companion.jsImportHighlightingAndCompletionFixture
import com.intellij.lang.javascript.modules.JSImportTestUtil
import com.intellij.openapi.util.registry.Registry
import com.intellij.psi.css.inspections.invalid.CssUnknownTargetInspection
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.webpack.createAndSetWebpackConfig
import org.jetbrains.plugins.scss.inspections.SassScssUnresolvedMixinInspection
import org.junit.jupiter.api.Test
import java.util.function.Consumer

private const val BASE_PATH = "/ts_imports"

@TestApplication
class VueModuleImportTest {
  private val projectFixture = jsProjectFixture()
  private val js by jsImportHighlightingAndCompletionFixture(projectFixture, JSFixtureOptions(getVueTestDataPath() + BASE_PATH, "vue"))

  @Test
  fun testTypesModule() {
    js.doTestWithCopyDirectory()
  }

  @Test
  fun testSimpleDeclare() {
    js.doTestWithCopyDirectory()
  }

  @Test
  fun testReferenceGlobalTyping() {
    js.doTestWithCopyDirectory()
  }

  @Test
  fun testAutoImportFromTs() {
    js.doTestAutoImportWithCopyDirectory()
  }

  @Test
  fun testPathMappingResolve() {
    js.doTestAutoImportWithCopyDirectory()
  }

  @Test
  fun testAutoImportFromVue() {
    js.doTestAutoImportWithCopyDirectory()
  }

  @Test
  fun testAutoImportVueFileToTs() {
    js.doTestActionWithCopyDirectory(JSImportTestUtil.getActionName(), "ts", null, null)
  }

  @Test
  fun testAutoImportFromVueWithJs() {
    JavaScriptFormatterTestBase.setTempSettings(js.project, JavascriptLanguage, Consumer<JSCodeStyleSettings> {
      it.USE_EXPLICIT_JS_EXTENSION = JSCodeStyleSettings.UseExplicitExtension.ALWAYS_JS
    })

    js.doTestActionWithCopyDirectory("Insert 'import HelloWorld from \"./src/HelloWorld.vue\"'", "vue", null, null)
  }

  @Test
  fun testAmbientTypingsInVue() {
    js.doTestWithCopyDirectory()
  }

  @Test
  fun testSimpleVarFromES6() {
    js.doTestWithCopyDirectory()
    js.checkAfterFile(js.extension)
  }

  @Test
  fun testSimpleJSConfig() {
    js.doTestWithCopyDirectory()
    js.checkAfterFile(js.extension)
  }

  @Test
  @JSTestOptions(selectLookupItem = 0)
  fun testEnumImport() {
    js.doTestWithCopyDirectory(1, true, js.extension)
    js.checkAfterFile(js.extension)
  }

  @Test
  fun testReExportWIthJs() {
    js.doTestWithCopyDirectory(1, true, "ts")
  }

  @Test
  @JSTestOptions(JSTestOption.WithInfos, JSTestOption.WithSymbolNames)
  fun testCustomComponentHighlighting() {
    js.doTestWithCopyDirectory()
  }

  @Test
  fun testCssReferencePathMapping() {
    js.fixture.enableInspections(CssUnknownTargetInspection())
    js.fixture.copyDirectoryToProject(js.getTestName(false), "")
    js.fixture.configureFromTempProjectFile("src/${js.getTestName(false)}.vue")
    js.fixture.testHighlighting()
  }

  @Test
  @JSTestOptions(selectLookupItem = 0)
  fun testVueFileNameCompletion() {
    js.doTestWithCopyDirectory()
    js.checkAfterFile(js.extension)
  }

  @Test
  fun testStylesResolvePathMapping() {
    //css doesn't have stubs for imports!
    Registry.get("ast.loading.filter").setValue(false, js.testRootDisposable)

    js.fixture.enableInspections(CssUnknownTargetInspection(), SassScssUnresolvedMixinInspection())
    js.fixture.copyDirectoryToProject(js.getTestName(false), "")
    js.fixture.configureFromTempProjectFile("spa/src/testComponent.vue")
    js.fixture.allowTreeAccessForAllFiles()
    js.fixture.testHighlighting()
  }

  @Test
  fun testStylesResolvePathMappingTilde() {
    //css doesn't have stubs for imports!
    Registry.get("ast.loading.filter").setValue(false, js.testRootDisposable)

    js.fixture.enableInspections(CssUnknownTargetInspection(), SassScssUnresolvedMixinInspection())
    js.fixture.copyDirectoryToProject(js.getTestName(false), "")
    js.fixture.configureFromTempProjectFile("spa/src/testComponent.vue")
    js.fixture.allowTreeAccessForAllFiles()
    js.fixture.testHighlighting()
  }

  @Test
  fun testTSOverVuePriority() {
    js.doHighlightOnlyTestWithCopyDirectory(js.extension)
  }

  @Test
  fun testJSOverVuePriority() {
    js.doHighlightOnlyTestWithCopyDirectory(js.extension)
  }

  @Test
  fun testAutoImportFromVueWebpack() {
    js.doTestActionWithCopyDirectory(JSImportTestUtil.getActionName(), "vue", Consumer {
      createAndSetWebpackConfig(js.project, js.testRootDisposable, "aliasPath", "src", null, it.path)
    })
  }

  @Test
  fun testAutoImportFromVueWebpackWithExtension() {
    js.doTestActionWithCopyDirectory(JSImportTestUtil.getActionName(), "vue", Consumer {
      createAndSetWebpackConfig(js.project, js.testRootDisposable, "aliasPath", "src", listOf(".vue"), it.path)
    })
  }
}
