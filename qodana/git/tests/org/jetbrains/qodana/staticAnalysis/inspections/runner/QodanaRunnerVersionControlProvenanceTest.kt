package org.jetbrains.qodana.staticAnalysis.inspections.runner

import com.intellij.testFramework.TestDataPath
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.jupiter.api.Assertions
import java.nio.file.Path

@TestDataPath($$"$CONTENT_ROOT/core/test-data/QodanaRunnerTest")
class QodanaRunnerVersionControlProvenanceTest : QodanaRunnerWithVcsTestCase() {
  override val testDataBasePath: Path = Path.of("QodanaRunnerTest", "empty profile")
  override val makeInitialCommit: Boolean = false

  @Test
  fun `test no HEAD produces degraded version control provenance`(): Unit = runBlocking {
    runAnalysis()

    assertSarifResults()
    val versionControlDetails = manager.sarifRun.versionControlProvenance.single()
    Assertions.assertNull(versionControlDetails.repositoryUri)
    Assertions.assertNull(versionControlDetails.revisionId)
    Assertions.assertEquals("master", versionControlDetails.branch)
    Assertions.assertNull(versionControlDetails.revisionTag)
    Assertions.assertNull(versionControlDetails.asOfTimeUtc)
    Assertions.assertNull(versionControlDetails.mappedTo)
    Assertions.assertEquals(mapOf("vcsType" to "Git", "repoUrl" to ""), versionControlDetails.properties)
  }
}
