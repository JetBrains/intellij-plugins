package org.jetbrains.qodana.yaml

import com.intellij.codeInsight.lookup.Lookup
import com.intellij.codeInspection.ex.InspectionProfileImpl
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.registry.Registry
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase

class QodanaYamlTest : LightJavaCodeInsightFixtureTestCase() {
  override fun setUp() {
    super.setUp()
    val initInspections = InspectionProfileImpl.INIT_INSPECTIONS

    InspectionProfileImpl.INIT_INSPECTIONS = true
    Disposer.register(testRootDisposable) {
      InspectionProfileImpl.INIT_INSPECTIONS = initInspections
    }
  }

  fun `test inspection completion offers All option`() {
    setupYamlSample("""
      include:
        - name: Al<caret>
    """.trimIndent())
    myFixture.completeBasic()
    myFixture.type(Lookup.REPLACE_SELECT_CHAR)

    myFixture.checkResult(
      """
      include:
        - name: All
      """.trimIndent()
    )
  }

  fun `test inspection completion lots of options for include`() {
    Registry.get("ide.completion.variant.limit").setValue("10000", testRootDisposable)
    setupYamlSample("""
      include:
        - name: <caret>
    """.trimIndent())

    checkCompletionVariantsContain("All", "Sanity1", "StartFromMeOne", "Recommended0") { completions ->
      assert(completions.size > 55) { "Expected a lot (>500) of inspections" }
    }
  }

  fun `ignore_test inspection completion only All and included for exclude (empty profile)`() {
    setupYamlSample("""
      profile:
        name: test.empty
      include:
        - name: StartFromMeTwo
      exclude:
        - name: <caret>
    """.trimIndent())

    checkCompletionVariantsContain("All", "StartFromMeTwo") { completions ->
      assertEquals(2, completions.size)
    }
  }

  fun `ignore_test inspection completion offers (All + included + inspections from profile) for exclude (sanity profile)`() {
    setupYamlSample("""
      profile:
        name : test.qodana.sanity
      include:
        - name: Recommended15
      exclude:
        - name: <caret>
    """.trimIndent())

    checkCompletionVariantsEqual(
      "All", "Recommended15",
      "Sanity1", "Sanity2", "Sanity3", "Sanity4", "Sanity5"
    )
  }

  fun `ignore_ test inspection completion offers inspections from profile for exclude (recommended profile)`() {
    setupYamlSample("""
      profile:
        name : test.qodana.recommended
      exclude:
        - name: <caret>
    """.trimIndent())

    checkCompletionVariantsContain(
      // Contains all
      "All",
      // Contains all from sanity
      *PROFILE_SANITY.inspections.map { it.shortName }.toTypedArray(),
      // Contains all from starter
      *PROFILE_STARTER.inspections.map { it.shortName }.toTypedArray(),
      // Contains something else (more rare inspections)
      "Recommended14"
    ) { completions ->
      // A lot of them
      assert(completions.size > 55)
    }
  }

  fun `test profile completion`() {
    setupYamlSample("""
      profile:
        - name : <caret>
    """.trimIndent(), withMockProfiles = false)

    checkCompletionVariantsContain("empty", "Default", "qodana.starter", "qodana.recommended")
  }

  fun `test path completion`() {
    myFixture.createFile("file1.kt", "")
    myFixture.createFile("file2.kt", "")

    setupYamlSample("""
      include:
        - name : All
          paths:
            - <caret>
    """.trimIndent())

    checkCompletionVariantsContain("qodana.yaml", "file1.kt", "file2.kt")
  }

  private fun setupYamlSample(code: String, withMockProfiles: Boolean = true) {
    myFixture.configureByText("qodana.yaml", code)
    myFixture.allowTreeAccessForAllFiles()
    if (withMockProfiles) {
      setupMockProfiles(project, testRootDisposable)
    }
  }

  private fun checkCompletionVariantsContain(vararg expected: String, extraChecks: (List<String>) -> Unit = {}) {
    val variants = myFixture.completeBasic()
    val actual = variants.map { it.allLookupStrings }.flatten().sorted()
    assertEquals(expected.toList().sorted().distinct(), actual.filter { it in expected })
    extraChecks(actual)
  }

  private fun checkCompletionVariantsEqual(vararg expected: String, extraChecks: (List<String>) -> Unit = {}) {
    val variants = myFixture.completeBasic()
    val actual = variants.map { it.allLookupStrings }.flatten().sorted()
    assertEquals(expected.toList().sorted(), actual)
    extraChecks(actual)
  }
}