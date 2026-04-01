package org.intellij.qodana.rust

import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.jupiter.api.DisplayNameGeneration
import org.junit.jupiter.api.DisplayNameGenerator
import org.junit.jupiter.api.Test
import kotlin.io.path.div
import kotlin.io.path.writeText

@DisplayNameGeneration(DisplayNameGenerator.Simple::class)
class AnalysisTest : IntegrationTest() {
  @Test
  fun `basic`() {
    val workdir = checkout("rust-test")
    val result = analyze(workdir)

    result.ok.shouldBeTrue()
    result.findIssue("RsUnusedImport", "src/main.rs:1").shouldNotBeNull()
    result.findIssue("RsLiveness", "src/main.rs:6").shouldNotBeNull()
    result.findIssue("RsSimplifyPrint", "src/main.rs:22").shouldNotBeNull()
    result.findIssue("RsUnnecessaryReturn", "src/main.rs:30").shouldNotBeNull()
    result.findIssues("RsUnusedImport").shouldHaveSize(1)
    result.findIssues("RsLiveness").shouldHaveSize(1)
    result.findIssues("RsSimplifyPrint").shouldHaveSize(1)
    result.findIssues("RsUnnecessaryReturn").shouldHaveSize(1)
  }

  @Test
  fun `dirty project`() {
    val workdir = checkout("rust-test")

    // First run establishes IDE state (indexes, .idea, caches)
    val result1 = analyze(workdir)
    result1.ok.shouldBeTrue()

    // Second run verifies analysis works correctly on a "dirty" working directory
    val result2 = analyze(workdir)
    result2.ok.shouldBeTrue()
    result2.findIssue("RsUnusedImport", "src/main.rs:1").shouldNotBeNull()
    result2.findIssue("RsLiveness", "src/main.rs:6").shouldNotBeNull()
    result2.findIssue("RsSimplifyPrint", "src/main.rs:22").shouldNotBeNull()
    result2.findIssue("RsUnnecessaryReturn", "src/main.rs:30").shouldNotBeNull()
    result2.findIssues("RsUnusedImport").shouldHaveSize(1)
    result2.findIssues("RsLiveness").shouldHaveSize(1)
    result2.findIssues("RsSimplifyPrint").shouldHaveSize(1)
    result2.findIssues("RsUnnecessaryReturn").shouldHaveSize(1)
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
    result.findIssue("RsUnusedImport", "src/lib.rs:3").shouldNotBeNull()
    result.findIssue("RsUnnecessaryReturn", "src/lib.rs:6").shouldNotBeNull()
    result.findIssues("RsUnusedImport").shouldHaveSize(1)
    result.findIssues("RsUnnecessaryReturn").shouldHaveSize(1)
  }

  /**
   * Reproduces the intermittent CI failure where `basic` and `dirty project` produce zero results.
   *
   * Root cause (from CI build 905876656 logs):
   * Some CI agents have an old Cargo that doesn't support `edition = "2024"`.
   * `cargo metadata` fails with: "this version of Cargo is older than the `2024` edition,
   * and only supports `2015`, `2018`, and `2021` editions."
   * The Cargo project model fails to load, and Qodana analysis produces zero inspection results.
   *
   * This test simulates the same failure by using `edition = "2099"` (unsupported by any Cargo version).
   */
  @Test
  fun `no results when cargo does not support the project edition`() {
    val workdir = checkout("rust-test")
    (workdir / "Cargo.toml").writeText("""
      [package]
      name = "rust-test"
      version = "0.1.0"
      edition = "2099"

      [dependencies]
    """.trimIndent())

    val result = analyze(workdir)

    result.ok.shouldBeTrue()
    result.results!!.filter { it.ruleId?.startsWith("Rs") == true }.shouldBeEmpty()
  }

}
