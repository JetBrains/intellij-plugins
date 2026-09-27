package org.jetbrains.qodana.staticAnalysis.inspections.runner.globalOutput

import com.intellij.openapi.components.PathMacroManager
import com.intellij.openapi.components.service
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.VfsTestUtil
import com.jetbrains.qodana.sarif.model.Level
import com.jetbrains.qodana.sarif.model.Result
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.runBlocking
import org.jetbrains.qodana.license.QodanaLicenseType
import org.jetbrains.qodana.staticAnalysis.inspections.runner.Problem
import org.jetbrains.qodana.staticAnalysis.inspections.runner.QodanaRunContext
import org.jetbrains.qodana.staticAnalysis.inspections.runner.startup.LoadedProfile
import org.jetbrains.qodana.staticAnalysis.newProfileWithInspections
import org.jetbrains.qodana.staticAnalysis.profile.MainInspectionGroup
import org.jetbrains.qodana.staticAnalysis.profile.QodanaProfile
import org.jetbrains.qodana.staticAnalysis.sarif.notifications.QodanaConfigureNotificationCollector
import org.jetbrains.qodana.staticAnalysis.sarif.notifications.RuntimeNotificationCollector
import org.jetbrains.qodana.staticAnalysis.scopes.QodanaAnalysisScope
import org.jetbrains.qodana.staticAnalysis.testFramework.QodanaRunnerTestCase
import org.jetbrains.qodana.staticAnalysis.testFramework.reinstantiateInspectionRelatedServices
import org.jetbrains.qodana.util.QodanaMessageReporter
import org.junit.Test
import java.nio.file.Files

private const val INSPECTION = "DuplicatedCode"

private val CONTENT = """
  class A {
    void a() {
      int x = 1;
      int y = 2;
    }
  }
""".trimIndent()

/**
 * Covers the branches of [DuplicateCodeConsumer] that a whole analysis run cannot reach.
 * The test writes `DuplicatedCode.xml` and `DuplicatedCode_aggregate.xml` itself, so it controls
 * which fragment of the aggregate report has a row in the tool result database.
 */
class DuplicateCodeConsumerTest : QodanaRunnerTestCase() {
  override fun setUp() {
    super.setUp()
    // The real inspection is registered in the test IDE, but the cached registrar does not hold it yet.
    reinstantiateInspectionRelatedServices(project, testRootDisposable)
  }

  @Test
  fun `keeps a fragment that has no row of its own`() {
    val file = createSourceFile("A.java")
    val first = CONTENT.indexOf("int x")
    val second = CONTENT.indexOf("int y")

    // Only the first fragment has a problem, so only it has a row. The second fragment sits in the
    // same file, so the inspection did run on it. The aggregate report holds the line 0 that
    // AggregateReport writes when it cannot read the document, which the consumer must not trust.
    val result = consume(
      problems = listOf(problemXml(file, line = 3, offset = columnOf(first), length = 10)),
      fragments = listOf(
        fragmentXml(file, line = 3, start = first, end = first + 10),
        fragmentXml(file, line = 0, start = second, end = second + 10),
      ),
    )

    assertEquals(listOf("test-module/A.java:3", "test-module/A.java:4"), result.locationLines())
    assertEmpty(notifications())
  }

  @Test
  fun `prefers an exact-match row over a fallback row as the template`() {
    val a = createSourceFile("A.java")
    val b = createSourceFile("B.java")
    val first = CONTENT.indexOf("int x")
    val second = CONTENT.indexOf("int y")

    // The first fragment of the cluster falls back to another cluster's row in A.java. The second
    // fragment has an exact-match row in B.java. The consumer must pick the exact-match row as the
    // template, so the resulting level comes from B.java's row, not from the A.java fallback.
    val result = consume(
      problems = listOf(
        problemXml(a, line = 3, offset = columnOf(first), length = 10, severity = "WEAK WARNING"),
        problemXml(b, line = 3, offset = columnOf(first), length = 10, severity = "ERROR"),
      ),
      fragments = listOf(
        fragmentXml(a, line = 0, start = second, end = second + 10),
        fragmentXml(b, line = 3, start = first, end = first + 10),
      ),
    )

    assertEquals(Level.ERROR, result.level)
  }

  @Test
  fun `drops a cluster whose file the inspection never analyzed`() {
    val file = createSourceFile("A.java")
    val first = CONTENT.indexOf("int x")
    val second = CONTENT.indexOf("int y")

    // No problem at all, so the file has no row and the whole cluster is out of the scope.
    val result = consumeOrNull(
      problems = emptyList(),
      fragments = listOf(
        fragmentXml(file, line = 3, start = first, end = first + 10),
        fragmentXml(file, line = 4, start = second, end = second + 10),
      ),
    )

    assertNull(result)
    assertEmpty(notifications())
  }

  @Test
  fun `drops a cluster that keeps a single fragment`() {
    val analyzed = createSourceFile("A.java")
    val other = createSourceFile("B.java")
    val first = CONTENT.indexOf("int x")

    // B.java has no row, so the consumer drops its fragment. One fragment is not a duplicate.
    val result = consumeOrNull(
      problems = listOf(problemXml(analyzed, line = 3, offset = columnOf(first), length = 10)),
      fragments = listOf(
        fragmentXml(analyzed, line = 3, start = first, end = first + 10),
        fragmentXml(other, line = 3, start = first, end = first + 10),
      ),
    )

    assertNull(result)
    assertEmpty(notifications())
  }

  @Test
  fun `reports an error notification for a fragment it cannot place`() {
    val file = createSourceFile("A.java")
    val other = createSourceFile("B.java")
    val first = CONTENT.indexOf("int x")

    // The third fragment names an offset past the end of an analyzed file, so the consumer cannot
    // place it. The two other fragments stay in the report, and the run reports the loss.
    val result = consume(
      problems = listOf(
        problemXml(file, line = 3, offset = columnOf(first), length = 10),
        problemXml(other, line = 3, offset = columnOf(first), length = 10),
      ),
      fragments = listOf(
        fragmentXml(file, line = 3, start = first, end = first + 10),
        fragmentXml(other, line = 3, start = first, end = first + 10),
        fragmentXml(file, line = 99, start = CONTENT.length + 1000, end = CONTENT.length + 1010),
      ),
    )

    assertEquals(listOf("test-module/A.java:3", "test-module/B.java:3"), result.locationLines())
    assertEquals(
      listOf("error: Could not restore a duplicated code fragment from the analysis results. The report is incomplete. x1"),
      notifications(),
    )
  }

  @Test
  fun `keeps the module of every fragment`() {
    val first = createSourceFile("A.java")
    val second = createSourceFile("B.java")
    val x = CONTENT.indexOf("int x")
    val y = CONTENT.indexOf("int y")

    // The fragment of A.java matches its own row. The fragment of B.java has no row of its own, so
    // it takes the module from another row of B.java. Both must keep the module of their own file.
    val result = consume(
      problems = listOf(
        problemXml(first, line = 3, offset = columnOf(x), length = 10, module = "module-a"),
        problemXml(second, line = 3, offset = columnOf(x), length = 10, module = "module-b"),
      ),
      fragments = listOf(
        fragmentXml(first, line = 3, start = x, end = x + 10),
        fragmentXml(second, line = 0, start = y, end = y + 10),
      ),
    )

    assertEquals(listOf("test-module/A.java:3", "test-module/B.java:4"), result.locationLines())
    assertEquals(listOf("module-a", "module-b"), result.locationModules())
  }

  @Test
  fun `merges the losses of two clusters into one notification`() {
    val first = createSourceFile("A.java")
    val second = createSourceFile("B.java")
    val x = CONTENT.indexOf("int x")
    val y = CONTENT.indexOf("int y")
    val beyondEnd = CONTENT.length + 1000

    // Each cluster keeps two fragments and loses one. The collector reports one merged notification.
    val results = consumeClusters(
      problems = listOf(
        problemXml(first, line = 3, offset = columnOf(x), length = 10),
        problemXml(second, line = 3, offset = columnOf(x), length = 10),
      ),
      clusters = listOf(
        listOf(
          fragmentXml(first, line = 3, start = x, end = x + 10),
          fragmentXml(second, line = 3, start = x, end = x + 10),
          fragmentXml(first, line = 99, start = beyondEnd, end = beyondEnd + 10),
        ),
        listOf(
          fragmentXml(first, line = 4, start = y, end = y + 10),
          fragmentXml(second, line = 4, start = y, end = y + 10),
          fragmentXml(first, line = 99, start = beyondEnd + 20, end = beyondEnd + 30),
        ),
      ),
    )

    assertEquals(
      listOf(
        listOf("test-module/A.java:3", "test-module/B.java:3"),
        listOf("test-module/A.java:4", "test-module/B.java:4"),
      ),
      results.map { requireNotNull(it) { "The consumer produced no result" }.locationLines() },
    )
    assertEquals(
      listOf("error: Could not restore a duplicated code fragment from the analysis results. The report is incomplete. x2"),
      notifications(),
    )
  }

  private fun createSourceFile(name: String): VirtualFile {
    val basePath = requireNotNull(project.basePath) { "No project base path" }
    val baseDir = requireNotNull(LocalFileSystem.getInstance().refreshAndFindFileByPath(basePath)) {
      "No directory in the virtual file system at $basePath"
    }
    return VfsTestUtil.createFile(baseDir, "test-module/$name", CONTENT)
  }

  /** The column of [offset] inside its line, which is what the inspection XML holds. */
  private fun columnOf(offset: Int): Int = offset - (CONTENT.lastIndexOf('\n', offset - 1) + 1)

  private fun problemXml(
    file: VirtualFile,
    line: Int,
    offset: Int,
    length: Int,
    module: String = "test-module",
    severity: String = "WEAK WARNING",
  ): String = """
    <problem>
      <file>${file.url}</file>
      <line>$line</line>
      <offset>$offset</offset>
      <length>$length</length>
      <highlighted_element>int x = 1;</highlighted_element>
      <language>JAVA</language>
      <module>$module</module>
      <description>Duplicated code</description>
      <problem_class id="$INSPECTION" severity="$severity">Duplicated code</problem_class>
    </problem>
  """.trimIndent()

  private fun fragmentXml(file: VirtualFile, line: Int, start: Int, end: Int): String =
    """<fragment file="${file.url}" line="$line" start="$start" end="$end"/>"""

  private fun Result.locationLines(): List<String> =
    locations.orEmpty().map { "${it.physicalLocation.artifactLocation.uri}:${it.physicalLocation.region.startLine}" }

  private fun Result.locationModules(): List<String?> =
    locations.orEmpty().map { it.logicalLocations?.firstOrNull()?.fullyQualifiedName }

  private fun notifications(): List<String> =
    project.service<RuntimeNotificationCollector>().notifications.map {
      val occurrences = (it.properties?.get(RuntimeNotificationCollector.OCCURRENCES_PROPERTY) as? Number)?.toInt() ?: 1
      "${it.level}: ${it.message?.text} x$occurrences"
    }

  private fun consume(problems: List<String>, fragments: List<String>): Result =
    requireNotNull(consumeOrNull(problems, fragments)) { "The consumer produced no result" }

  private fun consumeOrNull(problems: List<String>, fragments: List<String>): Result? =
    consumeClusters(problems, listOf(fragments)).single()

  /** Runs [DuplicateCodeConsumer] over the two XML files that [problems] and [clusters] describe. */
  private fun consumeClusters(problems: List<String>, clusters: List<List<String>>): List<Result?> = runBlocking {
    val outputPath = Files.createTempDirectory("DuplicateCodeConsumerTest")
    val profile = newProfileWithInspections(INSPECTION)
    val scope = QodanaAnalysisScope.fromConfigOrDefault(qodanaConfig, project) { error("No scope at $it") }
    val qodanaProfile = QodanaProfile(
      MainInspectionGroup(profile).applyConfig(qodanaConfig, project, false),
      emptyList(),
      project,
      QodanaLicenseType.ULTIMATE_PLUS,
    )
    val runContext = QodanaRunContext(
      project,
      LoadedProfile(profile, "", ""),
      scope,
      qodanaProfile,
      qodanaConfig,
      this,
      QodanaMessageReporter.DEFAULT,
    )
    val context = runContext.createGlobalInspectionContext(outputPath, qodanaProfile)
    QodanaConfigureNotificationCollector().configureForQodana(qodanaConfig, project)

    val problemsFile = outputPath.resolve("DuplicatedCode.xml")
    Files.writeString(problemsFile, "<problems>${problems.joinToString("\n")}</problems>")
    val aggregateFile = outputPath.resolve("DuplicatedCode_aggregate.xml")
    val duplicates = clusters.joinToString("\n") { fragments ->
      """<duplicate cost="1" hash="0">${fragments.joinToString("\n")}</duplicate>"""
    }
    Files.writeString(aggregateFile, "<problems>$duplicates</problems>")

    try {
      val collected = mutableListOf<Problem>()
      DuplicateCodeConsumer().consumeOwnedFiles(
        context.profileState,
        listOf(problemsFile, aggregateFile),
        context.database,
        project,
      ) { list, _ -> collected += list }

      val macroManager = PathMacroManager.getInstance(project)
      assertEquals("One problem per cluster", clusters.size, collected.size)
      collected.map { it.getSarif(macroManager, context.database) }
    }
    finally {
      context.closeQodanaContext()
      // The context launches coroutines in this scope, and runBlocking would wait for them forever.
      coroutineContext.cancelChildren()
    }
  }
}
