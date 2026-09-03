package org.jetbrains.qodana.poly

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayNameGeneration
import org.junit.jupiter.api.DisplayNameGenerator
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.minutes

@DisplayNameGeneration(DisplayNameGenerator.Simple::class)
class GeneratedSourcesTest : IntegrationTest() {
  @Test
  fun `generated and minified files are out of the analysis scope`() {
    val workdir = checkout("generated-sources")

    val result = analyze(workdir) {
      timeout = 10.minutes
      vm.properties["qodana.product.name"] = "Qodana Poly"
    }

    assertTrue(result.ok, "Qodana failed with exit code ${result.exitCode}:\n${result.stdout}")
    assertEquals(
      listOf("app.js"),
      result.results!!.mapNotNull { it.locations?.firstOrNull()?.physicalLocation?.artifactLocation?.uri }.distinct().sorted()
    )
  }
}
