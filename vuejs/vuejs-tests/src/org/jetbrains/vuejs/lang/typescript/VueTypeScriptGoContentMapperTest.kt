// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.lang.typescript

import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter
import com.intellij.javascript.testFramework.web.WebFrameworkTestModule
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil
import com.intellij.lang.typescript.lsp.TypeScriptContentMapperContribution
import com.intellij.lang.typescript.lsp.TypeScriptGoContentMappedFileSupport
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.polySymbols.testFramework.HybridFixtureContext
import com.intellij.polySymbols.testFramework.PolySymbolsFixture.EditorTypingTestFixture
import com.intellij.polySymbols.testFramework.PolySymbolsTestConfigurator
import com.intellij.polySymbols.testFramework.checkGotoDeclaration
import com.intellij.polySymbols.testFramework.moveToOffsetBySignature
import com.intellij.testFramework.ExpectedHighlightingData
import com.intellij.testFramework.VfsTestUtil
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.runInEdtAndWait
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.vuejs.VueFixture
import org.jetbrains.vuejs.VueTestMode
import org.jetbrains.vuejs.VueTsConfig.ContentMapper
import org.jetbrains.vuejs.VueTsConfigFile
import org.jetbrains.vuejs.copyBundledVueContentMapper
import org.jetbrains.vuejs.vueFixture
import org.junit.jupiter.api.Test

@TestApplication
class VueTypeScriptGoContentMapperTest {

  private val vue: VueContentMapperFixture by vueFixture(VueTestMode.TS_GO_PROXY) { VueContentMapperFixture(it) }

  @Test
  fun testNoTsconfig() {
    vue.checkHighlighting("App.vue", "main.ts", tsconfig = false)
  }

  @Test
  fun testTsconfigWithoutContentMappers() {
    vue.checkHighlighting("App.vue", "main.ts")
  }

  @Test
  fun testTsconfigWithVueContentMapper() {
    vue.checkHighlighting("App.vue", "main.ts", vueContentMapper = true)
  }

  @Test
  fun testVueFileOutsideTsconfig() {
    vue.checkHighlighting("src/main.ts", "other/Other.vue", tsconfig = false, vueContentMapper = true)
  }

  @Test
  fun testNoVueInstalledNoTsconfig() {
    vue.checkHighlighting("App.vue", installVue = false, tsconfig = false)
  }

  @Test
  fun testNoVueInstalledTsconfigWithVueContentMapper() {
    vue.checkHighlighting("App.vue", installVue = false, vueContentMapper = true)
  }

  @Test
  fun testUserContentMapper() {
    vue.checkHighlighting("main.ts", installVue = false, fooContentMapper = true)
  }

  @Test
  fun testVueAndUserContentMappers() {
    vue.checkHighlighting("App.vue", "main.ts", vueContentMapper = true, fooContentMapper = true)
  }

  @Test
  fun testInferredProjectUserContentMapper() {
    vue.checkHighlighting("main.ts", installVue = false, tsconfig = false, fooContentMapper = true, inferredFooContentMapper = true)
  }

  @Test
  fun testInferredProjectWithoutUserContentMapper() {
    vue.checkHighlighting("main.ts", installVue = false, tsconfig = false)
  }

  @Test
  fun testScriptSetupCompletion() {
    vue.doContentMapperEditorTypingTest(
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
    vue.doContentMapperEditorTypingTest(
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
    vue.doContentMapperEditorTypingTest(
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
    vue.doContentMapperEditorTypingTest(
      vueContentMapper = true,
      dirName = "completionVueAutoImport",
      configureFileName = "main.ts",
    ) {
      moveToOffsetBySignature("import Best<caret>")
      completeBasic()
      assertLookupContains("import BestFeature\$1 from \"./BestFeature.vue\";")
      assertThat(vue.currentCompletionPresentationTexts()).contains("BestFeature").doesNotContain("BestFeature\$1")
    }
  }

  @Test
  fun testInferredProjectMapperCompletionIsUnavailable() {
    vue.doContentMapperEditorTypingTest(
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
    vue.doContentMapperTest(vueContentMapper = true, fooContentMapper = true) {
      vue.checkGotoDeclarationAt(
        path = "main.ts",
        fromSignature = "<caret>formatLabel(\"hello\")",
        declarationSignature = "export function <caret>formatLabel",
        expectedFileName = "App.vue",
      )
      vue.checkGotoDeclarationAt(
        path = "App.vue",
        fromSignature = "{{ <caret>formatValue(\"hello\") }}",
        declarationSignature = "export function <caret>formatValue",
        expectedFileName = "utils.ts",
      )
      vue.checkGotoDeclarationAt(
        path = "fooConsumer.ts",
        fromSignature = "console.log(<caret>host)",
        declarationSignature = "import { <caret>host } from \"./config.foo\"",
        expectedFileName = "fooConsumer.ts",
      )
    }
  }

  @Test
  fun testThisFallback() {
    vue.doContentMapperEditorTypingTest(installVue = false, tsconfig = false, vueContentMapper = true, configureFileName = "Options.vue") {
      moveToOffsetBySignature("this.<caret>")
      completeBasic()
      assertLookupContains("count", "increment")
    }
  }

  @Test
  fun testLiveVueEdit() {
    vue.doContentMapperTest(
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
      vue.checkFileHighlighting("main.ts")
    }
  }

  @Test
  fun testNewVueAfterStart() {
    vue.doContentMapperTest(
      vueContentMapper = true,
      expectedHighlightingPaths = listOf("main.ts"),
    ) {
      configureFromTempProjectFile("main.ts")
      configureByText("Created.vue", "<script lang=\"ts\">export const count = 1</script>")
      vue.checkFileHighlighting("main.ts")
    }
  }

  @Test
  fun testVueInNodeModules() {
    vue.doContentMapperTest(
      vueContentMapper = true,
      expectedHighlightingPaths = listOf("main.ts", "node_modules/vue-library/Widget.vue"),
    ) {
      vue.checkFileHighlighting("main.ts")
      vue.checkFileHighlighting("node_modules/vue-library/Widget.vue")
    }
  }

  @Test
  fun testInferredJavaScriptVue() {
    vue.doContentMapperTest(
      tsconfig = false,
      expectedHighlightingPaths = listOf("App.vue"),
    ) {
      vue.checkFileHighlighting("App.vue")
    }
  }

}

private class VueContentMapperFixture(
  context: HybridFixtureContext,
) : VueFixture(context, "typescriptGo/contentMapper", VueTestMode.TS_GO_PROXY, dirModeByDefault = true) {

  private var installVue: Boolean = true
  private var expectedDataConfigurator: ExpectedDataConfigurator? = null

  override fun adjustModules(
    modules: Array<out WebFrameworkTestModule>,
  ): Array<out WebFrameworkTestModule> =
    if (installVue) super.adjustModules(modules) else modules

  fun checkHighlighting(
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
        this@VueContentMapperFixture.checkFileHighlighting(path)
      }
    }
  }

  fun doContentMapperTest(
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

  fun doContentMapperEditorTypingTest(
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

  fun checkFileHighlighting(path: String) {
    fixture.configureFromTempProjectFile(path)
      .virtualFile
      .putUserData(VfsTestUtil.TEST_DATA_FILE_PATH, "$testDataPath/$defaultDirName/$path")
    fixture.collectAndCheckExpectedHighlighting(expectedDataConfigurator!!.getData(path))
  }

  fun checkGotoDeclarationAt(
    path: String,
    fromSignature: String,
    declarationSignature: String,
    expectedFileName: String,
  ) {
    fixture.configureFromTempProjectFile(path)
    fixture.checkGotoDeclaration(fromSignature, declarationSignature, expectedFileName)
  }

  fun currentCompletionPresentationTexts(): List<String> =
    fixture.lookupElements?.mapNotNull { LookupElementPresentation.renderElement(it).itemText }.orEmpty()

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
