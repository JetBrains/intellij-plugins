package org.intellij.qodana.rust

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.jupiter.api.DisplayNameGeneration
import org.junit.jupiter.api.DisplayNameGenerator
import org.junit.jupiter.api.Test
import kotlin.test.assertContains

@DisplayNameGeneration(DisplayNameGenerator.Simple::class)
class AnalysisTest : IntegrationTest() {
  @Test
  fun `basic`() {
    val workdir = checkout("rust-test")
    val result = analyzeProject(workdir)

    assert(result.ok)
    assertContains(result.problems, "RsLiveness")
    assertContains(result.problems, "RsSimplifyPrint")
    assertContains(result.problems, "RsUnnecessaryReturn")
    assertContains(result.problems, "RsUnusedImport")
  }

  @Test
  fun `dirty project`() {
    val workdir = checkout("rust-test")
    analyzeProject(workdir)
    val result = analyzeProject(workdir)

    assert(result.ok)
    assertContains(result.problems, "RsLiveness")
    assertContains(result.problems, "RsSimplifyPrint")
    assertContains(result.problems, "RsUnnecessaryReturn")
    assertContains(result.problems, "RsUnusedImport")
  }

  @Test
  fun `features`() {
    val workdir = checkout("rust-test-features")
    val result = analyzeProject(workdir)

    assert(result.ok)
    result.findIssue("RsLiveness", "src/main.rs:2").shouldNotBeNull()
    result.findIssue("RsLiveness", "src/main.rs:7").shouldNotBeNull()
    result.findIssue("RsLiveness", "src/main.rs:12").shouldBeNull()
    result.findIssues("RsLiveness").shouldHaveSize(2)
  }

  @Test
  fun `cfg options`() {
    val workdir = checkout("rust-test-cfg-options")
    val result = analyzeProject(workdir)

    assert(result.ok)
    result.findIssue("RsLiveness", "src/main.rs:2").shouldNotBeNull()
    result.findIssue("RsLiveness", "src/main.rs:8").shouldNotBeNull()
    result.findIssue("RsLiveness", "src/main.rs:14").shouldBeNull()
    result.findIssue("RsLiveness", "src/main.rs:22").shouldBeNull()
    result.findIssue("RsLiveness", "src/main.rs:26").shouldNotBeNull()
    result.findIssues("RsLiveness").shouldHaveSize(3)
  }

  @Test
  fun `rustc private`() {
    val workdir = checkout("rust-test-rustc-private")
    val result = analyzeProject(workdir)

    assert(result.ok)
    assertContains(result.problems, "RsUnusedImport")
    assertContains(result.problems, "RsUnnecessaryReturn")
  }

}
