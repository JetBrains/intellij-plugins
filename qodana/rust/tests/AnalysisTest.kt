package org.intellij.qodana.rust

import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.jupiter.api.DisplayNameGeneration
import org.junit.jupiter.api.DisplayNameGenerator
import org.junit.jupiter.api.Test

@DisplayNameGeneration(DisplayNameGenerator.Simple::class)
class AnalysisTest : IntegrationTest() {
  @Test
  fun `basic`() {
    val workdir = checkout("rust-test")
    val result = analyze(workdir)

    result.ok.shouldBeTrue()
    result.findIssue("RsLiveness").shouldNotBeNull()
    result.findIssue("RsSimplifyPrint").shouldNotBeNull()
    result.findIssue("RsUnnecessaryReturn").shouldNotBeNull()
    result.findIssue("RsUnusedImport").shouldNotBeNull()
  }

  @Test
  fun `dirty project`() {
    val workdir = checkout("rust-test")
    analyze(workdir)
    val result = analyze(workdir)

    result.ok.shouldBeTrue()
    result.findIssue("RsLiveness").shouldNotBeNull()
    result.findIssue("RsSimplifyPrint").shouldNotBeNull()
    result.findIssue("RsUnnecessaryReturn").shouldNotBeNull()
    result.findIssue("RsUnusedImport").shouldNotBeNull()
  }

  @Test
  fun `features`() {
    val workdir = checkout("rust-test-features")
    val result = analyze(workdir)

    result.ok.shouldBeTrue()
    result.findIssue("RsLiveness", "src/main.rs:2").shouldNotBeNull()
    result.findIssue("RsLiveness", "src/main.rs:7").shouldNotBeNull()
    result.findIssue("RsLiveness", "src/main.rs:12").shouldBeNull()
    result.findIssues("RsLiveness").shouldHaveSize(2)
  }

  @Test
  fun `cfg options`() {
    val workdir = checkout("rust-test-cfg-options")
    val result = analyze(workdir)

    result.ok.shouldBeTrue()
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
    val result = analyze(workdir)

    result.ok.shouldBeTrue()
    result.findIssue("RsUnusedImport").shouldNotBeNull()
    result.findIssue("RsUnnecessaryReturn").shouldNotBeNull()
  }

}
