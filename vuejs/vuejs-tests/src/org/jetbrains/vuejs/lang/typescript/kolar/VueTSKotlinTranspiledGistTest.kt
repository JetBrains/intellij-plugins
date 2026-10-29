package org.jetbrains.vuejs.lang.typescript.kolar

import com.intellij.ide.impl.OpenProjectTask
import com.intellij.javascript.tskotlin.ast.SourceFile
import com.intellij.javascript.tskotlin.compiler.Program
import com.intellij.javascript.tskotlin.compiler.ProgramConfig
import com.intellij.javascript.tskotlin.compiler.ProgramHosts
import com.intellij.javascript.tskotlin.compiler.ProgramOptions
import com.intellij.javascript.tskotlin.core.CompilerOptions
import com.intellij.javascript.tskotlin.core.ModuleKind
import com.intellij.javascript.tskotlin.core.ModuleResolutionKind
import com.intellij.javascript.tskotlin.core.ScriptTarget
import com.intellij.javascript.tskotlin.core.Tristate
import com.intellij.javascript.tskotlin.core.WorkGroupDispatcher
import com.intellij.javascript.tskotlin.tsoptions.ParsedCommandLine
import com.intellij.javascript.tskotlin.tsoptions.ParsedOptions
import com.intellij.javascript.tskotlin.tspath.RootedDirectoryPath
import com.intellij.javascript.tskotlin.tspath.rootedFilePathFromAbsolute
import com.intellij.javascript.tskotlin.vfs.ForwardingFS
import com.intellij.javascript.tskotlin.vfs.FsFile
import com.intellij.lang.typescript.tsc.tskotlin.TSKotlinCompilerHost
import com.intellij.lang.typescript.tsc.tskotlin.TSKotlinFileIndexData
import com.intellij.lang.typescript.tsc.tskotlin.TSKotlinVfsService
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.runBlockingMaybeCancellable
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightFixture
import com.intellij.psi.PsiDocumentManager
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.moduleFixture
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.intellij.testFramework.junit5.fixture.tempPathFixture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.minutes

/**
 * Checks that a TSKotlin program reads a Vue file lazily after the first full parse of its Kolar service script.
 * The data of the gist must be the same as the data of the full parse, for each import style of a Vue file.
 */
@TestApplication
class VueTSKotlinTranspiledGistTest {
  private val pathFixture = tempPathFixture()
  private val projectFixture = projectFixture(pathFixture, OpenProjectTask {}, true)
  private val moduleFixture = projectFixture.moduleFixture(pathFixture, true)
  private val fixture = codeInsightFixture(projectFixture, pathFixture)
  private val project get() = projectFixture.get()

  @Test
  fun vueFileIsLazyAfterTheFirstParse(): Unit = timeoutRunBlocking(2.minutes) {
    val files = configure(VUE_FILES, OTHER_FILES)
    val expected = readAction {
      val program = createProgram(files.values)
      files.mapValues { (name, file) ->
        val parsed = program.file(file)
        assertTrue(parsed.tskotlinIsParsed(), name)
        // The program parses the service script, not the text of the Vue file
        assertNotEquals(VUE_FILES.getValue(name), parsed.text, name)
        TSKotlinFileIndexData.of(parsed)
      }
    }
    // A script that is not a module gets no data, so each program parses it
    assertEquals(setOf("External.vue"), expected.filterValues { it == null }.keys)
    val reads = HashMap<String, Int>()
    readAction {
      val program = createProgram(files.values, reads)
      for ((name, file) in files) {
        val second = program.file(file)
        val data = expected.getValue(name)
        assertEquals(data == null, second.tskotlinIsParsed(), name)
        if (data == null) continue
        assertEquals(0, reads[file.path] ?: 0, name)
        assertEquals(data, TSKotlinFileIndexData.of(second), name)
      }
    }
  }

  @Test
  fun vueFileWithUnsavedChangesIsParsed(): Unit = timeoutRunBlocking(2.minutes) {
    val name = "SetupTs.vue"
    val file = configure(mapOf(name to VUE_FILES.getValue(name))).getValue(name)
    readAction { assertTrue(createProgram(listOf(file)).file(file).tskotlinIsParsed()) }
    withContext(Dispatchers.EDT) {
      WriteCommandAction.runWriteCommandAction(project) {
        FileDocumentManager.getInstance().getDocument(file)!!.insertString(0, "<!-- edited -->\n")
      }
      PsiDocumentManager.getInstance(project).commitAllDocuments()
    }
    // The gist tracks the file content, so the committed text of the document gets no data from it
    readAction { assertTrue(createProgram(listOf(file)).file(file).tskotlinIsParsed()) }
  }

  /** Adds [vueFiles] and [otherFiles] to the project, and returns the Vue files. */
  private suspend fun configure(vueFiles: Map<String, String>, otherFiles: Map<String, String> = emptyMap()): Map<String, VirtualFile> {
    moduleFixture.get()
    val files = withContext(Dispatchers.EDT) {
      fixture.get().addFileToProject("package.json", """{"name":"app","dependencies":{"vue":"^3.5.0"}}""")
      otherFiles.forEach { (name, text) -> fixture.get().addFileToProject(name, text) }
      val files = vueFiles.mapValues { (name, text) -> fixture.get().addFileToProject(name, text).virtualFile }
      PsiDocumentManager.getInstance(project).commitAllDocuments()
      edtWriteAction { FileDocumentManager.getInstance().saveAllDocuments() }
      files
    }
    IndexingTestUtil.waitUntilIndexesAreReady(project)
    return files
  }

  /** A program with [roots], whose FS counts the reads of each file in [reads]. Its work groups use this thread. */
  private fun createProgram(roots: Collection<VirtualFile>, reads: MutableMap<String, Int>? = null): Program {
    val root = roots.first().parent
    val fs = object : ForwardingFS(project.service<TSKotlinVfsService>().vfs) {
      override fun readFile(file: FsFile): String? {
        reads?.merge(file.path.asString(), 1, Int::plus)
        return file.readFile()
      }
    }
    val config = ParsedCommandLine(
      ParsedOptions(
        compilerOptions = CompilerOptions(
          allowNonTsExtensions = Tristate.True,
          noLib = Tristate.True,
          types = emptyList(),
          target = ScriptTarget.ESNext,
          module = ModuleKind.ESNext,
          moduleResolution = ModuleResolutionKind.Bundler,
        ),
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

  private fun Program.file(file: VirtualFile): SourceFile = getSourceFile(rootedFilePathFromAbsolute(file.path))!!

  private companion object {
    /** The files that the Vue files import. */
    val OTHER_FILES: Map<String, String> = mapOf(
      "external.ts" to "export default { name: 'External' }",
      "types.ts" to "export interface Props { value: number }\nexport interface Item { name: string }",
    )

    /** Vue files with the import styles of the code generation, see the codegen of the Vue Kolar transpiler. */
    val VUE_FILES: Map<String, String> = mapOf(
      "SetupTs.vue" to """
        <script setup lang="ts">
        import { ref } from 'vue'
        import type { Props } from './types'
        import Child from './Child.vue'
        const count = ref(0)
        import late from './late'
        </script>
        <template><Child :count="count" /></template>
      """.trimIndent(),
      "BothScripts.vue" to """
        <script lang="ts">
        import { defineComponent } from 'vue'
        export default defineComponent({ name: 'Both' })
        </script>
        <script setup lang="ts">
        import { computed } from 'vue'
        const doubled = computed(() => 2)
        </script>
        <template><div>{{ doubled }}</div></template>
      """.trimIndent(),
      "Options.vue" to """
        <script>
        export default {
          components: { Lazy: () => import('./Lazy.vue') },
          data() { return { value: 1 } },
        }
        </script>
        <template><Lazy /></template>
      """.trimIndent(),
      "NoDefault.vue" to """
        <script lang="ts">
        import { helper } from './helper'
        helper()
        </script>
        <template><div /></template>
      """.trimIndent(),
      "External.vue" to """
        <script src="./external.ts"></script>
        <template><div /></template>
      """.trimIndent(),
      "Generic.vue" to """
        <script setup lang="ts" generic="T extends string">
        import type { Item } from './types'
        defineProps<{ items: T[]; item: Item }>()
        </script>
        <template><div v-for="i in items">{{ i }}</div></template>
      """.trimIndent(),
      "Dynamic.vue" to """
        <script setup lang="ts">
        const loaded = await import('./dynamic')
        type Loaded = import('./types').Props
        </script>
        <template><div /></template>
      """.trimIndent(),
    )
  }
}
