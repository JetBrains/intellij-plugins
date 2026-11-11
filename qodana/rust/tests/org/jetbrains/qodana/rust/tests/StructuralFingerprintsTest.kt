package org.jetbrains.qodana.rust.tests

import com.jetbrains.qodana.sarif.baseline.BaselineCalculation.ENCLOSING_SCOPE_INDICATOR
import com.jetbrains.qodana.sarif.baseline.BaselineCalculation.EXTRACTION_AND_REFACTOR_TOLERANT_INDICATOR
import com.jetbrains.qodana.sarif.baseline.BaselineCalculation.MOVE_AND_REFACTOR_TOLERANT_INDICATOR
import com.jetbrains.qodana.sarif.model.Result
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.jetbrains.qodana.tests.utils.QodanaAnalysisResult
import org.junit.jupiter.api.DisplayNameGeneration
import org.junit.jupiter.api.DisplayNameGenerator
import org.junit.jupiter.api.Test

/**
 * Verifies the structural fingerprints of the Rust linter over a real analysis.
 *
 * The Rust PSI has no light test fixture, so this reads the signals from the SARIF report.
 * `RsApproxConstant` fires on every `3.1416`, and each site sits in a different kind of scope.
 *
 * `enclosingScopeIndicator` holds `TYPE#name` as text and pins the scope. The other two are hashes, and
 * comparing them pins the AST shape. One test covers every rule, because each analysis starts a full IDE.
 */
@DisplayNameGeneration(DisplayNameGenerator.Simple::class)
class StructuralFingerprintsTest : IntegrationTest() {

  @Test
  fun `the scope rules of the Rust provider`() {
    val result = analyze(checkout(PROJECT)) { vm.properties["qodana.structural.fingerprints"] = "true" }

    result.ok.shouldBeTrue()
    result.findIssues("RsApproxConstant").shouldHaveSize(14)

    // A constant of a trait, an `impl` or a `mod` sits in no function. Only a scope there names it.
    result.scopeAt(8).shouldBe("TRAIT_ITEM#Shape")
    result.scopeAt(12).shouldBe("IMPL_ITEM#Circle")
    result.scopeAt(32).shouldBe("MOD_ITEM#inner")

    // The `impl` label drops the type arguments, so a rename of the type parameter changes nothing.
    result.scopeAt(24).shouldBe("IMPL_ITEM#Wrapper")
    result.scopeAt(28).shouldBe("IMPL_ITEM#Wrapper")

    // A closure with no name reports the enclosing function. A bound one reports the name that holds it.
    result.scopeAt(40).shouldBe("FUNCTION#mapped")
    result.scopeAt(44).shouldBe("LAMBDA_EXPR#scale")
    result.scopeAt(61).shouldBe("LAMBDA_EXPR#f")
    result.scopeAt(66).shouldBe("LAMBDA_EXPR#SCALED")

    // A field assignment binds the plain field name, never the receiver.
    result.scopeAt(74).shouldBe("LAMBDA_EXPR#factor")

    // Two `impl Circle` blocks, the second with an extra method. The shape describes the constant,
    // never the member list around it.
    result.approxConstantAt(16).structural(EXTRACTION_AND_REFACTOR_TOLERANT_INDICATOR)
      .shouldBe(result.approxConstantAt(12).structural(EXTRACTION_AND_REFACTOR_TOLERANT_INDICATOR))

    // The lambda shape contract, kept in sync with every other language. Line 44 holds a bound closure and
    // line 55 an unnamed one. Both scopes are named `scale`, so an equal hash proves an equal shape.
    result.approxConstantAt(55).structural(EXTRACTION_AND_REFACTOR_TOLERANT_INDICATOR)
      .shouldBe(result.approxConstantAt(44).structural(EXTRACTION_AND_REFACTOR_TOLERANT_INDICATOR))

    // `vec![3.1416]` and `[3.1416]` sit in one function, so rule, scope and shape all match. The two agree
    // only when the walk stops at the `vec!` argument too.
    val inOneFunction = result.findIssues("RsApproxConstant", "src/main.rs:36")
    inOneFunction.shouldHaveSize(2)
    inOneFunction[1].structural(EXTRACTION_AND_REFACTOR_TOLERANT_INDICATOR)
      .shouldBe(inOneFunction[0].structural(EXTRACTION_AND_REFACTOR_TOLERANT_INDICATOR))
  }

  /** The structural indicators stay off unless the property asks for them. */
  @Test
  fun `the structural indicators are off by default`() {
    val result = analyze(checkout(PROJECT)) { launchName = "default" }

    result.ok.shouldBeTrue()
    result.approxConstantAt(8).structural(MOVE_AND_REFACTOR_TOLERANT_INDICATOR).shouldBe(null)
  }

  /** The `TYPE#name` of the scope holding the `RsApproxConstant` of [line]. */
  private fun QodanaAnalysisResult.scopeAt(line: Int): String? =
    approxConstantAt(line).structural(ENCLOSING_SCOPE_INDICATOR)

  private fun QodanaAnalysisResult.approxConstantAt(line: Int): Result =
    findIssue("RsApproxConstant", "src/main.rs:$line").shouldNotBeNull()

  private fun Result.structural(name: String): String? = partialFingerprints?.get(name, 1)

  private companion object {
    const val PROJECT = "rust-test-fingerprints"
  }
}
