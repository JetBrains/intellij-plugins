// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript

import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter
import com.intellij.javascript.testFramework.web.WebFrameworkTestModule
import com.intellij.lang.javascript.TrackFailedTestRule
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.lang.typescript.lsp.TypeScriptContentMapperContribution
import com.intellij.lang.typescript.lsp.TypeScriptGoContentMappedFileSupport
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.polySymbols.testFramework.PolySymbolsTestConfigurator
import com.intellij.polySymbols.testFramework.checkGotoDeclaration
import com.intellij.testFramework.ExpectedHighlightingData
import com.intellij.testFramework.VfsTestUtil
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.runInEdtAndWait
import org.jetbrains.vuejs.VueTestCase
import org.jetbrains.vuejs.VueTestMode
import org.jetbrains.vuejs.VueTsConfig.ContentMapper
import org.jetbrains.vuejs.VueTsConfigFile
import org.jetbrains.vuejs.copyBundledVueContentMapper
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class VueTypeScriptGoContentMapperTest :
  VueTestCase("typescriptGo/contentMapper", VueTestMode.TS_GO_PROXY) {

  override val dirModeByDefault: Boolean = true

  @Rule
  @JvmField
  val rule: TestRule = TrackFailedTestRule(
    // TSGo gets `includeCompletionsForImportStatements=false` because of IJPL-155741
    "testDefaultImportCompletionInTypeScript",
  )

  @Test
  fun testNoTsconfig() {
    checkHighlighting("App.vue", "main.ts", tsconfig = false)
  }

  @Test
  fun testTsconfigWithoutContentMappers() {
    checkHighlighting("App.vue", "main.ts")
  }

  @Test
  fun testTsconfigWithVueContentMapper() {
    checkHighlighting("App.vue", "main.ts", vueContentMapper = true)
  }

  @Test
  fun testVueFileOutsideTsconfig() {
    checkHighlighting("src/main.ts", "other/Other.vue", tsconfig = false, vueContentMapper = true)
  }

  @Test
  fun testNoVueInstalledNoTsconfig() {
    checkHighlighting("App.vue", installVue = false, tsconfig = false)
  }

  @Test
  fun testNoVueInstalledTsconfigWithVueContentMapper() {
    checkHighlighting("App.vue", installVue = false, vueContentMapper = true)
  }

  @Test
  fun testUserContentMapper() {
    checkHighlighting("main.ts", installVue = false, fooContentMapper = true)
  }

  @Test
  fun testVueAndUserContentMappers() {
    checkHighlighting("App.vue", "main.ts", vueContentMapper = true, fooContentMapper = true)
  }

  @Test
  fun testInferredProjectUserContentMapper() {
    checkHighlighting("main.ts", installVue = false, tsconfig = false, fooContentMapper = true, inferredFooContentMapper = true)
  }

  @Test
  fun testInferredProjectWithoutUserContentMapper() {
    checkHighlighting("main.ts", installVue = false, tsconfig = false)
  }

  @Test
  fun testScriptSetupCompletion() {
    doContentMapperEditorTypingTest(
      vueContentMapper = true,
      fooContentMapper = true,
      dirName = "completionInterop",
      configureFileName = "App.vue",
    ) {
      moveToOffsetBySignature("double(<caret>count.value)")
      completeBasic()
      assertLookupContains("count")
    }
  }

  @Test
  fun testInferredVueScriptSetupCompletionIsUnavailable() {
    doContentMapperEditorTypingTest(
      tsconfig = false,
      vueContentMapper = true,
      dirName = "noTsconfig",
      configureFileName = "App.vue",
    ) {
      moveToOffsetBySignature("double(c<caret>ount.value)")
      type("o")
      assertLookupNotShown()
    }
  }

  @Test
  fun testVueNamedImportCompletionAndFooCompletionUnavailable() {
    doContentMapperEditorTypingTest(
      vueContentMapper = true,
      fooContentMapper = true,
      dirName = "typeScriptImportCompletion",
      configureFileName = "main.ts",
    ) {
      moveToOffsetBySignature("import { forma<caret> } from \"./App.vue\"")
      type("t")
      assertLookupContains("formatLabel")

      moveToOffsetBySignature("import { h<caret> } from \"./config.foo\"")
      type("o")
      assertLookupNotShown()
    }
  }

  @Test
  fun testDefaultImportCompletionInTypeScript() {
    doContentMapperEditorTypingTest(
      vueContentMapper = true,
      dirName = "completionVueAutoImport",
      configureFileName = "main.ts",
    ) {
      moveToOffsetBySignature("import Best<caret>")
      completeBasic()
      assertLookupContains("import BestFeature\$1 from \"./BestFeature.vue\";")
      val presentationTexts = currentCompletionPresentationTexts()
      assertTrue(presentationTexts.contains("BestFeature"))
      assertFalse(presentationTexts.contains("BestFeature\$1"))
    }
  }

  @Test
  fun testInferredProjectMapperCompletionIsUnavailable() {
    doContentMapperEditorTypingTest(
      installVue = false,
      tsconfig = false,
      fooContentMapper = true,
      inferredFooContentMapper = true,
      dirName = "inferredFooCompletion",
      configureFileName = "main.ts",
    ) {
      moveToOffsetBySignature("import { h<caret> } from \"./config.foo\"")
      type("o")
      assertLookupNotShown()
    }
  }

  @Test
  fun testGotoInterop() {
    doContentMapperTest(vueContentMapper = true, fooContentMapper = true) {
      checkGotoDeclarationAt(
        path = "main.ts",
        fromSignature = "<caret>formatLabel(\"hello\")",
        declarationSignature = "export function <caret>formatLabel",
        expectedFileName = "App.vue",
      )
      checkGotoDeclarationAt(
        path = "App.vue",
        fromSignature = "{{ <caret>formatValue(\"hello\") }}",
        declarationSignature = "export function <caret>formatValue",
        expectedFileName = "utils.ts",
      )
      checkGotoDeclarationAt(
        path = "fooConsumer.ts",
        fromSignature = "console.log(<caret>host)",
        declarationSignature = "import { <caret>host } from \"./config.foo\"",
        expectedFileName = "fooConsumer.ts",
      )
    }
  }

  @Test
  fun testThisFallback() {
    doContentMapperEditorTypingTest(installVue = false, tsconfig = false, vueContentMapper = true, configureFileName = "Options.vue") {
      moveToOffsetBySignature("this.<caret>")
      completeBasic()
      assertLookupContains("count", "increment")
    }
  }

  @Test
  fun testLiveVueEdit() {
    doContentMapperTest(
      vueContentMapper = true,
      expectedHighlightingPaths = listOf("main.ts"),
    ) {
      val appFile = configureFromTempProjectFile("App.vue")
      val document = FileDocumentManager.getInstance().getDocument(appFile.virtualFile)!!
      runInEdtAndWait {
        com.intellij.openapi.command.WriteCommandAction.runWriteCommandAction(project) {
          document.setText(document.text.replace("export const value = 1", "export const value = \"changed\""))
        }
      }
      checkFileHighlighting("main.ts")
    }
  }

  @Test
  fun testNewVueAfterStart() {
    doContentMapperTest(
      vueContentMapper = true,
      expectedHighlightingPaths = listOf("main.ts"),
    ) {
      configureFromTempProjectFile("main.ts")
      configureByText("Created.vue", "<script lang=\"ts\">export const count = 1</script>")
      checkFileHighlighting("main.ts")
    }
  }

  @Test
  fun testVueInNodeModules() {
    doContentMapperTest(
      vueContentMapper = true,
      expectedHighlightingPaths = listOf("main.ts", "node_modules/vue-library/Widget.vue"),
    ) {
      checkFileHighlighting("main.ts")
      checkFileHighlighting("node_modules/vue-library/Widget.vue")
    }
  }

  @Test
  fun testInferredJavaScriptVue() {
    doContentMapperTest(
      tsconfig = false,
      expectedHighlightingPaths = listOf("App.vue"),
    ) {
      checkFileHighlighting("App.vue")
    }
  }

  private var installVue: Boolean = true
  private var expectedDataConfigurator: ExpectedDataConfigurator? = null

  override fun adjustModules(
    modules: Array<out WebFrameworkTestModule>,
  ): Array<out WebFrameworkTestModule> =
    if (installVue) super.adjustModules(modules) else modules

  private fun checkHighlighting(
    vararg paths: String,
    installVue: Boolean = true,
    tsconfig: Boolean = true,
    vueContentMapper: Boolean = false,
    fooContentMapper: Boolean = false,
    inferredFooContentMapper: Boolean = false,
  ) {
    doContentMapperTest(
      installVue = installVue,
      tsconfig = tsconfig,
      vueContentMapper = vueContentMapper,
      fooContentMapper = fooContentMapper,
      inferredFooContentMapper = inferredFooContentMapper,
      expectedHighlightingPaths = paths.toList(),
    ) {
      for (path in paths) {
        this@VueTypeScriptGoContentMapperTest.checkFileHighlighting(path)
      }
    }
  }

  private fun doContentMapperTest(
    installVue: Boolean = true,
    tsconfig: Boolean = true,
    vueContentMapper: Boolean = false,
    fooContentMapper: Boolean = false,
    inferredFooContentMapper: Boolean = false,
    expectedHighlightingPaths: List<String> = emptyList(),
    test: CodeInsightTestFixture.() -> Unit,
  ) {
    this.installVue = installVue
    val expectedData = ExpectedDataConfigurator(expectedHighlightingPaths)
    expectedDataConfigurator = expectedData
    val configurators = contentMapperConfigurators(tsconfig, vueContentMapper, fooContentMapper, inferredFooContentMapper) + expectedData
    doConfiguredTest(configureFile = false, configurators = configurators) {
      test(this)
    }
  }

  private fun doContentMapperEditorTypingTest(
    installVue: Boolean = true,
    tsconfig: Boolean = true,
    vueContentMapper: Boolean = false,
    fooContentMapper: Boolean = false,
    inferredFooContentMapper: Boolean = false,
    dirName: String = defaultDirName,
    configureFileName: String,
    test: EditorTypingTestFixture.() -> Unit,
  ) {
    this.installVue = installVue
    doEditorTypingTest(
      dirName = dirName,
      configureFileName = configureFileName,
      checkResult = false,
      configurators = contentMapperConfigurators(tsconfig, vueContentMapper, fooContentMapper, inferredFooContentMapper),
      test = test,
    )
  }

  /**
   * [tsconfig] generates a `tsconfig.json` with a [VueTsConfigFile].
   * Pass `false` for a test without a tsconfig, or with its own tsconfig in the test data.
   * [vueContentMapper] installs the bundled `@vue/content-mapper` into `node_modules` and adds it to the tsconfig.
   * [fooContentMapper] installs the `foo-content-mapper` package of the test data into `node_modules` and adds it to the tsconfig.
   * [inferredFooContentMapper] gives `foo-content-mapper` to the inferred projects through [TypeScriptGoContentMappedFileSupport].
   */
  private fun contentMapperConfigurators(
    tsconfig: Boolean,
    vueContentMapper: Boolean,
    fooContentMapper: Boolean,
    inferredFooContentMapper: Boolean,
  ): List<PolySymbolsTestConfigurator> =
    buildList {
      if (fooContentMapper) add(FooContentMapperPackage())
      if (tsconfig) {
        // The VueTsConfigFile installs the Vue content mapper of its tsconfig.
        add(VueTsConfigFile(contentMappers = listOfNotNull(
          VueTsConfigFile.VUE_CONTENT_MAPPER.takeIf { vueContentMapper },
          FOO_CONTENT_MAPPER_ENTRY.takeIf { fooContentMapper },
        )))
      }
      else {
        add(VueTsConfigFile(enabled = false))
        if (vueContentMapper) add(VueContentMapperPackage())
      }
      if (inferredFooContentMapper) add(InferredFooContentMapper())
    }

  private fun checkFileHighlighting(path: String) {
    myFixture.configureFromTempProjectFile(path)
      .virtualFile
      .putUserData(VfsTestUtil.TEST_DATA_FILE_PATH, "$testDataPath/$defaultDirName/$path")
    myFixture.collectAndCheckExpectedHighlighting(expectedDataConfigurator!!.getData(path))
  }

  private fun checkGotoDeclarationAt(
    path: String,
    fromSignature: String,
    declarationSignature: String,
    expectedFileName: String,
  ) {
    myFixture.configureFromTempProjectFile(path)
    myFixture.checkGotoDeclaration(fromSignature, declarationSignature, expectedFileName)
  }

  private fun currentCompletionPresentationTexts(): List<String> =
    myFixture.lookupElements?.mapNotNull { LookupElementPresentation.renderElement(it).itemText }.orEmpty()

  private class ExpectedDataConfigurator(
    private val paths: List<String>,
  ) : PolySymbolsTestConfigurator {
    private val data = mutableMapOf<String, ExpectedHighlightingData>()

    fun getData(path: String): ExpectedHighlightingData =
      data.getValue(path)

    override fun configure(fixture: CodeInsightTestFixture) {
      runInEdtAndWait {
        val documentManager = FileDocumentManager.getInstance()
        for (path in paths) {
          val file = fixture.tempDirFixture.getFile(path) ?: error("No file $path in the test data")
          val document = documentManager.getDocument(file)!!
          data[path] = ExpectedHighlightingData(document, true, true, false).apply { init() }
        }
        documentManager.saveAllDocuments()
      }
    }
  }

  private class VueContentMapperPackage : PolySymbolsTestConfigurator {
    override fun configure(fixture: CodeInsightTestFixture) {
      copyBundledVueContentMapper(fixture)
    }
  }

  private class FooContentMapperPackage : PolySymbolsTestConfigurator {
    override fun configure(fixture: CodeInsightTestFixture) {
      fixture.copyDirectoryToProject(FOO_CONTENT_MAPPER_TEST_DATA, "node_modules/$FOO_CONTENT_MAPPER")
    }
  }

  private class InferredFooContentMapper : PolySymbolsTestConfigurator {
    override fun configure(fixture: CodeInsightTestFixture) {
      val mapperScript = fixture.tempDirFixture.getFile("node_modules/$FOO_CONTENT_MAPPER/mapper.js")!!
      fixture.project.extensionArea
        .getExtensionPoint<TypeScriptGoContentMappedFileSupport>(TypeScriptGoContentMappedFileSupport.EP_NAME.name)
        .registerExtension(FooContentMappedFileSupport(fixture, mapperScript), fixture.testRootDisposable)
    }
  }

  private class FooContentMappedFileSupport(
    private val fixture: CodeInsightTestFixture,
    private val mapperScript: VirtualFile,
  ) : TypeScriptGoContentMappedFileSupport {
    override fun isHighlightingCandidate(file: VirtualFile): Boolean = false

    override fun isAcceptable(file: VirtualFile): Boolean = file.extension == "foo"

    override fun getInferredProjectContentMappers(): List<TypeScriptContentMapperContribution> {
      val node = (TypeScriptLanguageServiceUtil.getNodeInterpreter(fixture.project) as NodeJsLocalInterpreter)
        .interpreterSystemDependentPath
      return listOf(TypeScriptContentMapperContribution(
        extensions = listOf(".foo"),
        name = FOO_CONTENT_MAPPER,
        version = "1.0.0",
        exec = listOf(node, mapperScript.path),
        cwd = fixture.project.basePath,
        options = null,
        dynamicConfig = false,
      ))
    }
  }

  private companion object {
    const val FOO_CONTENT_MAPPER: String = "foo-content-mapper"
    const val FOO_CONTENT_MAPPER_TEST_DATA: String = "_mappers/$FOO_CONTENT_MAPPER"
    val FOO_CONTENT_MAPPER_ENTRY: ContentMapper = ContentMapper(packageName = FOO_CONTENT_MAPPER, extensions = listOf(".foo"))
  }
}
