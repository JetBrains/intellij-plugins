package org.jetbrains.vuejs.lang.typescript.kolar

import com.intellij.ide.impl.OpenProjectTask
import com.intellij.javascript.tskotlin.compiler.Program
import com.intellij.javascript.tskotlin.compiler.ProgramConfig
import com.intellij.javascript.tskotlin.compiler.ProgramHosts
import com.intellij.javascript.tskotlin.compiler.ProgramOptions
import com.intellij.javascript.tskotlin.core.CompilerOptions
import com.intellij.javascript.tskotlin.core.ModuleDetectionKind
import com.intellij.javascript.tskotlin.core.ModuleKind
import com.intellij.javascript.tskotlin.core.ModuleResolutionKind
import com.intellij.javascript.tskotlin.core.ScriptTarget
import com.intellij.javascript.tskotlin.core.Tristate
import com.intellij.javascript.tskotlin.core.WorkGroupDispatcher
import com.intellij.javascript.tskotlin.tsoptions.ParsedCommandLine
import com.intellij.javascript.tskotlin.tsoptions.ParsedOptions
import com.intellij.javascript.tskotlin.tspath.RootedDirectoryPath
import com.intellij.javascript.tskotlin.tspath.rootedFilePathFromAbsolute
import com.intellij.lang.typescript.kolar.KolarTranspilerService
import com.intellij.lang.typescript.tsc.tskotlin.TSKotlinCompilerHost
import com.intellij.lang.typescript.tsc.tskotlin.TSKotlinVfsService
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigService
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.runBlockingMaybeCancellable
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.ManagingFS
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightFixture
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.RegistryKey
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.moduleFixture
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.intellij.testFramework.junit5.fixture.tempPathFixture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.jetbrains.vuejs.context.isVueContext
import kotlin.time.Duration.Companion.minutes

/** Checks that the TSKotlin config graph resolves the imports of a Vue file from its Kolar service script. */
@TestApplication
@RegistryKey("typescript.tskotlin.enabled", "true")
class VueTSKotlinImportGraphTest {
  private val pathFixture = tempPathFixture()
  private val projectFixture = projectFixture(pathFixture, OpenProjectTask {}, true)
  private val moduleFixture = projectFixture.moduleFixture(pathFixture, true)
  private val fixture = codeInsightFixture(projectFixture, pathFixture)
  private val project get() = projectFixture.get()

  @Test
  fun resolvesTheImportsOfAVueFile(): Unit = timeoutRunBlocking(2.minutes) {
    val files = configure(
      "package.json" to """{"name":"app","dependencies":{"vue":"^3.5.0"}}""",
      "tsconfig.json" to """
        {"compilerOptions":{"module":"ESNext","moduleResolution":"Bundler","noLib":true,"types":[],"noEmit":true},
         "include":["*.ts","*.vue"]}
      """.trimIndent(),
      "main.ts" to "import App from './App.vue';",
      "App.vue" to """
        <script setup lang="ts">
        import { value } from './dep'
        </script>
        <template><div>{{ value }}</div></template>
      """.trimIndent(),
      "dep.ts" to "export const value = 1;",
    )
    val outFiles = readAction {
      val imports = TypeScriptConfigService.Provider.parseConfigFile(project, files.getValue("tsconfig.json"))!!.configImportResolveStructure
      imports.startCaching()
      try {
        listOf("main.ts", "App.vue").associateWith { name ->
          val record = imports.getOutEdges(files.getValue(name))
          assertNotNull(record, "The TSKotlin resolver gives no record of $name")
          (record!![0] until record.size).map { record[it] }.filter { it > 0 }.mapNotNull { ManagingFS.getInstance().findFileById(it) }.toSet()
        }
      }
      finally {
        imports.stopCaching()
      }
    }
    // A TypeScript file imports the Vue file by its full extension, and the Vue file imports from its service script
    assertEquals(setOf(files.getValue("App.vue")), outFiles.getValue("main.ts"))
    assertTrue(files.getValue("dep.ts") in outFiles.getValue("App.vue"), outFiles.getValue("App.vue").toString())
  }

  @Test
  fun doesNotParseLibraryVueFileWithoutServiceScript(): Unit = timeoutRunBlocking(2.minutes) {
    val files = configure(
      "package.json" to """{"name":"app"}""",
      "tsconfig.json" to """
        {"compilerOptions":{"module":"ESNext","moduleResolution":"Bundler","noLib":true,"types":[],"noEmit":true},
         "include":["*.ts","*.vue"]}
      """.trimIndent(),
      "main.ts" to """
        import Library from 'lib/component.vue';
        const library: string = Library.fromShim;
        const libraryNumber: number = Library.fromShim;
      """.trimIndent(),
      "vue.d.ts" to """
        declare module '*.vue' {
          const component: { fromShim: string };
          export default component;
        }
      """.trimIndent(),
      "standard.ts" to "const standard = true;",
      "node_modules/lib/package.json" to """{"name":"lib","version":"1.0.0"}""",
      "node_modules/lib/component.vue" to "<template><div>raw</div></template>",
    )
    val main = files.getValue("main.ts")
    val libraryVue = files.getValue("node_modules/lib/component.vue")
    val roots = listOf(main, files.getValue("vue.d.ts"), files.getValue("standard.ts"))

    readAction {
      assertTrue(isVueContext(libraryVue, project))
      val transpilerService = project.service<KolarTranspilerService>()
      assertTrue(transpilerService.getImportExtensions().contains(".vue"))
      assertFalse(transpilerService.isAcceptable(libraryVue))
      for (moduleDetection in listOf<ModuleDetectionKind?>(null, ModuleDetectionKind.Force)) {
        val program = createProgram(roots, moduleDetection)
        val parsedMain = program.getSourceFile(rootedFilePathFromAbsolute(main.path))!!
        val diagnostics = runBlockingMaybeCancellable {
          withContext(WorkGroupDispatcher.ofCaller()) { program.getSemanticDiagnostics(parsedMain) }
        }
        assertNull(program.getSourceFile(rootedFilePathFromAbsolute(libraryVue.path)), "$moduleDetection library Vue file")
        assertEquals("const standard = true;", program.getSourceFile(rootedFilePathFromAbsolute(files.getValue("standard.ts").path))?.text)
        assertEquals(1, diagnostics.size, "$moduleDetection $diagnostics")
      }
    }
  }

  private suspend fun configure(vararg entries: Pair<String, String>): Map<String, VirtualFile> {
    moduleFixture.get()
    val files = withContext(Dispatchers.EDT) {
      entries.associate { (path, text) -> path to fixture.get().addFileToProject(path, text).virtualFile }
    }
    IndexingTestUtil.waitUntilIndexesAreReady(project)
    return files
  }

  private fun createProgram(roots: List<VirtualFile>, moduleDetection: ModuleDetectionKind? = null): Program {
    val root = roots.first().parent
    val fs = project.service<TSKotlinVfsService>().vfs
    val compilerOptions = CompilerOptions(
      noLib = Tristate.True,
      types = emptyList(),
      target = ScriptTarget.ESNext,
      module = ModuleKind.ESNext,
      moduleResolution = ModuleResolutionKind.Bundler,
    )
    moduleDetection?.let { compilerOptions.moduleDetection = it }
    val config = ParsedCommandLine(
      ParsedOptions(
        compilerOptions = compilerOptions,
        fileNames = roots.map { rootedFilePathFromAbsolute(it.path) },
      ),
      baseDirectory = RootedDirectoryPath(root.path),
      caseSensitivity = fs.caseSensitivity(),
    )
    return runBlockingMaybeCancellable {
      withContext(WorkGroupDispatcher.ofCaller()) {
        Program.new(ProgramOptions(
          programConfig = ProgramConfig(config),
          programHosts = ProgramHosts(TSKotlinCompilerHost(fs, root.path, project = project)),
        ))
      }
    }
  }
}
