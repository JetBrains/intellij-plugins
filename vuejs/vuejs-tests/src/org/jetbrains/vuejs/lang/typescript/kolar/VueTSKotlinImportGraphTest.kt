package org.jetbrains.vuejs.lang.typescript.kolar

import com.intellij.ide.impl.OpenProjectTask
import com.intellij.lang.typescript.tsconfig.TypeScriptConfigService
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.readAction
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
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
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

  private suspend fun configure(vararg entries: Pair<String, String>): Map<String, VirtualFile> {
    moduleFixture.get()
    val files = withContext(Dispatchers.EDT) {
      entries.associate { (path, text) -> path to fixture.get().addFileToProject(path, text).virtualFile }
    }
    IndexingTestUtil.waitUntilIndexesAreReady(project)
    return files
  }
}
