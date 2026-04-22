package org.jetbrains.qodana.cpp

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import kotlin.io.path.createDirectories
import kotlin.io.path.div
import kotlin.io.path.writeText

class BuildSystemSelectionTest : CppIntegrationTest() {

  /** QD-12974: Makefile workspace init must not hang. */
  @Test
  fun `makefile project does not hang`() {
    val cwd = checkout("makefile-only-test")
    val result = analyze(cwd)

    // Both outcomes are acceptable: success means the project built fine;
    // failure with the right message means Makefile detection worked but the build failed gracefully.
    if (!result.ok) {
      result.stdout.shouldContain("Failed to configure project as a Makefile workspace")
    }
  }

  /** QD-13183: requesting a build system not present in the project must fail with an actionable error. */
  @Test
  fun `request CMake but only Makefile detected`() {
    val cwd = checkout("makefile-only-test")
    (cwd / "qodana.yaml").writeText("""
      cpp:
        buildSystem: CMake
    """.trimIndent())

    val result = analyze(cwd)
    result.ok shouldBe false
    result.stdout.shouldContain("Specified build system 'CMake' was not detected in the project")
  }

  /** QD-13184: selecting a build system with a stale .idea must preserve user inspection profiles. */
  @Test
  fun `select CMake build system with stale idea folder preserves inspection profiles`() {
    val cwd = checkout("build-system-selection-test")
    (cwd / "qodana.yaml").writeText("""
      cpp:
        buildSystem: CMake
      profile:
        name: no-dfa
    """.trimIndent())

    // Stale .idea with CMakeWorkspace, but also a custom inspection profile
    // that disables CppDFANullDereference. If .idea is deleted wholesale,
    // this profile is lost and the default profile (with DFA enabled) is used instead.
    (cwd / ".idea").createDirectories()
    (cwd / ".idea" / "misc.xml").writeText("""
      <?xml version="1.0" encoding="UTF-8"?>
      <project version="4">
        <component name="CMakeWorkspace" PROJECT_DIR="PROJECT_DIR" />
      </project>
    """.trimIndent())
    (cwd / ".idea" / "inspectionProfiles").createDirectories()
    (cwd / ".idea" / "inspectionProfiles" / "no_dfa.xml").writeText("""
      <component name="InspectionProjectProfileManager">
        <profile version="1.0">
          <option name="myName" value="no-dfa"/>
          <inspection_tool class="CppDFANullDereference" enabled="false" level="WARNING" enabled_by_default="false"/>
        </profile>
      </component>
    """.trimIndent())

    val result = analyze(cwd)
    result.ok shouldBe true
    // The custom profile disables CppDFANullDereference — if the profile was preserved,
    // this issue should NOT be found.
    result.findIssue("CppDFANullDereference") shouldBe null
  }

  /** QD-13184: stale workspace.xml must be deleted so that a cmake preset can take effect. */
  @Test
  fun `cmake preset with stale idea folder deletes workspace xml`() {
    val cwd = checkout("build-system-selection-test")
    (cwd / "qodana.yaml").writeText("""
      cpp:
        cmakePreset: default
    """.trimIndent())

    // Create a stale .idea with workspace.xml that would prevent preset from taking effect.
    (cwd / ".idea").createDirectories()
    (cwd / ".idea" / "misc.xml").writeText("""
      <?xml version="1.0" encoding="UTF-8"?>
      <project version="4">
        <component name="CMakeWorkspace" PROJECT_DIR="${'$'}PROJECT_DIR${'$'}" />
      </project>
    """.trimIndent())
    (cwd / ".idea" / "workspace.xml").writeText("""
      <?xml version="1.0" encoding="UTF-8"?>
      <project version="4">
        <component name="CMakeSettings" />
      </project>
    """.trimIndent())

    val result = analyze(cwd)
    // The preset "default" may not exist in CMakePresets.json, but we only care that workspace.xml
    // was cleaned up. misc.xml should be preserved since no buildSystem override is specified.
    result.ideaLog.shouldContain("Deleted .idea/workspace.xml")
  }

  /** QD-13184: CMake selection must work when a stale .idea directory exists. */
  @Test
  fun `select CMake build system with stale idea folder`() {
    val cwd = checkout("build-system-selection-test")
    (cwd / "qodana.yaml").writeText("""
      cpp:
        buildSystem: CMake
    """.trimIndent())

    (cwd / ".idea").createDirectories()
    (cwd / ".idea" / "misc.xml").writeText("""
      <?xml version="1.0" encoding="UTF-8"?>
      <project version="4">
        <component name="CMakeWorkspace" PROJECT_DIR="PROJECT_DIR" />
      </project>
    """.trimIndent())

    val result = analyze(cwd)
    result.ok shouldBe true
    result.findIssue("CppDFANullDereference").shouldNotBeNull()
  }

  /** QD-13184: CMake selection on a clean checkout without prior .idea state. */
  @Test
  fun `select CMake build system on fresh project`() {
    val cwd = checkout("build-system-selection-test")
    (cwd / "qodana.yaml").writeText("""
      cpp:
        buildSystem: CMake
    """.trimIndent())

    val result = analyze(cwd)
    result.ok shouldBe true
    result.findIssue("CppDFANullDereference").shouldNotBeNull()
  }

  /** QD-13184: CompDB build system selection with a generated compile_commands.json. */
  @Test
  fun `select CompDB build system`() {
    val cwd = checkout("build-system-selection-test")
    (cwd / "qodana.yaml").writeText("""
      cpp:
        buildSystem: CompDB
    """.trimIndent())

    // Note: compile_commands.json is created at runtime because it needs absolute paths.
    (cwd / "compile_commands.json").writeText("""
      [
        {
          "directory": ${Json.encodeToString(cwd.toString())},
          "file": "main.cpp",
          "arguments": ["c++", "main.cpp", "-o", "main"]
        }
      ]
    """.trimIndent())

    val result = analyze(cwd)
    result.ok shouldBe true
    result.findIssue("CppDFANullDereference").shouldNotBeNull()
  }

  /** QD-13183: requesting CompDB without compile_commands.json must fail. */
  @Test
  fun `request CompDB but no compile_commands json`() {
    val cwd = checkout("build-system-selection-test")
    (cwd / "qodana.yaml").writeText("""
      cpp:
        buildSystem: CompDB
    """.trimIndent())

    val result = analyze(cwd)
    result.ok shouldBe false
    result.stdout.shouldContain("Specified build system 'CompDB' was not detected in the project")
  }

  /** QD-13183: requesting Make when only CMake is present must fail. */
  @Test
  fun `request Make but only CMake detected`() {
    val cwd = checkout("build-system-selection-test")
    (cwd / "qodana.yaml").writeText("""
      cpp:
        buildSystem: Make
    """.trimIndent())

    val result = analyze(cwd)
    result.ok shouldBe false
    result.stdout.shouldContain("Specified build system 'Make' was not detected in the project")
  }

  /** QD-12974: a broken Makefile must fail gracefully, not hang. */
  @Test
  fun `broken makefile project does not hang`() {
    val cwd = checkout("makefile-broken-test")
    val result = analyze(cwd)

    result.ok shouldBe false
    result.stdout.shouldContain("Failed to configure project as a Makefile workspace")
  }

  /** QD-13183: an unsupported buildSystem value must produce an error. */
  @Test
  fun `select invalid build system`() {
    val cwd = checkout("build-system-selection-test")
    (cwd / "qodana.yaml").writeText("""
      cpp:
        buildSystem: INVALID
    """.trimIndent())

    val result = analyze(cwd)
    result.ok shouldBe false
    result.stdout.shouldContain("Specified build system 'INVALID' is not supported by Qodana")
  }

  /** Auto-detection picks CMake when no buildSystem is specified. */
  @Test
  fun `select no build system`() {
    val cwd = checkout("build-system-selection-test")
    val result = analyze(cwd)
    result.ok shouldBe true
    result.findIssue("CppDFANullDereference").shouldNotBeNull()
  }

  /** QD-13183: when no build system can be detected, the error must mention cpp/buildSystem. */
  @Test
  fun `no build system detected produces actionable error`() {
    val cwd = checkout("empty-project-test")
    val result = analyze(cwd)

    result.ok shouldBe false
    result.stdout.shouldContain("Could not auto-detect the build system")
    result.stdout.shouldContain("cpp/buildSystem")
  }
}
