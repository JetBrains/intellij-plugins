package org.jetbrains.qodana.staticAnalysis.sarif

import com.jetbrains.qodana.sarif.baseline.BaselineCalculation
import com.jetbrains.qodana.sarif.model.ArtifactContent
import com.jetbrains.qodana.sarif.model.ArtifactLocation
import com.jetbrains.qodana.sarif.model.Location
import com.jetbrains.qodana.sarif.model.Message
import com.jetbrains.qodana.sarif.model.PhysicalLocation
import com.jetbrains.qodana.sarif.model.PropertyBag
import com.jetbrains.qodana.sarif.model.Region
import com.jetbrains.qodana.sarif.model.Level
import com.jetbrains.qodana.sarif.model.Result
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.BaselineEqualityV2
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.EnclosingScopeIndicator
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.ExtractionAndRefactorTolerantIndicator
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.MoveAndRefactorTolerantIndicator
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.ShiftTolerantEquality
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.withPartialFingerprints
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.addStructuralFingerprints
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.StructuralFingerprintSignals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/** Unit coverage for the structural / shift-tolerant fingerprint indicators and the [withPartialFingerprints] pipeline. */
class StructuralFingerprintsTest {

  @Test
  fun `shift tolerant key and version`() {
    assertEquals(BaselineCalculation.SHIFT_TOLERANT_INDICATOR, ShiftTolerantEquality.name)
    assertEquals(1, ShiftTolerantEquality.version)
  }

  @Test
  fun `shift tolerant is deterministic`() {
    val a = result()
    val b = result()
    assertEquals(ShiftTolerantEquality.calculate(a), ShiftTolerantEquality.calculate(b))
  }

  @Test
  fun `shift tolerant ignores line and column shifts`() {
    val original = result(startLine = 10, startColumn = 5)
    val shifted = result(startLine = 42, startColumn = 17)
    assertEquals(ShiftTolerantEquality.calculate(original), ShiftTolerantEquality.calculate(shifted))
  }

  @Test
  fun `shift tolerant reacts to content changes`() {
    val base = result()
    assertNotEquals(ShiftTolerantEquality.calculate(base), ShiftTolerantEquality.calculate(result(ruleId = "Other")))
    assertNotEquals(ShiftTolerantEquality.calculate(base), ShiftTolerantEquality.calculate(result(additionalData = "other")))
    assertNotEquals(ShiftTolerantEquality.calculate(base), ShiftTolerantEquality.calculate(result(level = Level.ERROR)))
    assertNotEquals(ShiftTolerantEquality.calculate(base), ShiftTolerantEquality.calculate(result(charLength = 99)))
    assertNotEquals(ShiftTolerantEquality.calculate(base), ShiftTolerantEquality.calculate(result(snippet = "other()")))
  }

  @Test
  fun `extraction tolerant key and version`() {
    assertEquals(BaselineCalculation.EXTRACTION_AND_REFACTOR_TOLERANT_INDICATOR, ExtractionAndRefactorTolerantIndicator.name)
    assertEquals(1, ExtractionAndRefactorTolerantIndicator.version)
  }

  @Test
  fun `extraction tolerant requires enclosing scope`() {
    assertNull(ExtractionAndRefactorTolerantIndicator.calculate(result(), signals(astShape = "IF_STATEMENT(BLOCK)")))
    assertNotNull(ExtractionAndRefactorTolerantIndicator.calculate(result(), signals(astShape = "IF_STATEMENT(BLOCK)", scopeName = "foo")))
  }

  @Test
  fun `extraction tolerant reacts to shape and scope but not filename`() {
    val calc = ExtractionAndRefactorTolerantIndicator
    val base = calc.calculate(result(uri = "src/A.java"), signals(astShape = "IF_STATEMENT(BLOCK)", scopeName = "foo"))

    val otherShape = calc.calculate(result(uri = "src/A.java"), signals(astShape = "FOR_STATEMENT(BLOCK)", scopeName = "foo"))
    assertNotEquals(base, otherShape)

    val otherScope = calc.calculate(result(uri = "src/A.java"), signals(astShape = "IF_STATEMENT(BLOCK)", scopeName = "bar"))
    assertNotEquals(base, otherScope)

    val movedFile = calc.calculate(result(uri = "src/moved/B.java"), signals(astShape = "IF_STATEMENT(BLOCK)", scopeName = "foo"))
    assertEquals(base, movedFile)
  }

  @Test
  fun `move tolerant key and version`() {
    assertEquals(BaselineCalculation.MOVE_AND_REFACTOR_TOLERANT_INDICATOR, MoveAndRefactorTolerantIndicator.name)
    assertEquals(1, MoveAndRefactorTolerantIndicator.version)
  }

  @Test
  fun `move tolerant needs shape or scope`() {
    assertNull(MoveAndRefactorTolerantIndicator.calculate(result(), signals()))
    assertNotNull(MoveAndRefactorTolerantIndicator.calculate(result(), signals(astShape = "IF_STATEMENT(BLOCK)")))
  }

  @Test
  fun `move tolerant reacts to filename and scope`() {
    val calc = MoveAndRefactorTolerantIndicator
    val base = calc.calculate(result(uri = "src/A.java"), signals(astShape = "IF_STATEMENT(BLOCK)", scopeName = "foo"))

    val movedFile = calc.calculate(result(uri = "src/moved/B.java"), signals(astShape = "IF_STATEMENT(BLOCK)", scopeName = "foo"))
    assertNotEquals(base, movedFile)

    val otherScope = calc.calculate(result(uri = "src/A.java"), signals(astShape = "IF_STATEMENT(BLOCK)", scopeName = "bar"))
    assertNotEquals(base, otherScope)
  }

  @Test
  fun `enclosing scope key and version`() {
    assertEquals(BaselineCalculation.ENCLOSING_SCOPE_INDICATOR, EnclosingScopeIndicator.name)
    assertEquals(1, EnclosingScopeIndicator.version)
  }

  @Test
  fun `enclosing scope combines type and name`() {
    assertEquals("METHOD#foo", EnclosingScopeIndicator.calculate(result(), signals(scopeName = "foo", scopeType = "METHOD")))
  }

  @Test
  fun `enclosing scope is null when type or name missing`() {
    assertNull(EnclosingScopeIndicator.calculate(result(), signals(scopeName = "foo")))
    assertNull(EnclosingScopeIndicator.calculate(result(), signals(scopeType = "METHOD")))
    assertNull(EnclosingScopeIndicator.calculate(result(), signals()))
  }

  @Test
  fun `pipeline adds equality and structural indicators`() {
    val result = result()
      .withPartialFingerprints()
      .addStructuralFingerprints(signals(astShape = "IF_STATEMENT(BLOCK)", scopeName = "foo", scopeType = "METHOD"))

    val prints = result.partialFingerprints!!
    assertNotNull(prints.get(BaselineCalculation.EQUAL_INDICATOR, 1))
    assertNotNull(prints.get(BaselineCalculation.EQUAL_INDICATOR, 2))
    assertNotNull(prints.get(BaselineCalculation.EXTRACTION_AND_REFACTOR_TOLERANT_INDICATOR, 1))
    assertNotNull(prints.get(BaselineCalculation.MOVE_AND_REFACTOR_TOLERANT_INDICATOR, 1))
    assertNotNull(prints.get(BaselineCalculation.ENCLOSING_SCOPE_INDICATOR, 1))
    // shift-tolerant is off by default
    assertNull(prints.get(BaselineCalculation.SHIFT_TOLERANT_INDICATOR, 1))

    assertNull(result.properties)
  }

  @Test
  fun `pipeline emits only equality indicators by default`() {
    val result = result().withPartialFingerprints()

    val prints = result.partialFingerprints!!
    assertNotNull(prints.get(BaselineCalculation.EQUAL_INDICATOR, 1))
    assertNotNull(prints.get(BaselineCalculation.EQUAL_INDICATOR, 2))
    assertNull(prints.get(BaselineCalculation.SHIFT_TOLERANT_INDICATOR, 1))
    assertNull(prints.get(BaselineCalculation.EXTRACTION_AND_REFACTOR_TOLERANT_INDICATOR, 1))
    assertNull(prints.get(BaselineCalculation.MOVE_AND_REFACTOR_TOLERANT_INDICATOR, 1))
    assertNull(prints.get(BaselineCalculation.ENCLOSING_SCOPE_INDICATOR, 1))
  }

  private fun signals(astShape: String? = null, scopeName: String? = null, scopeType: String? = null): StructuralFingerprintSignals =
    StructuralFingerprintSignals(astShape, scopeName, scopeType)

  private fun result(
    ruleId: String = "IgnoreResultOfCall",
    message: String = "Result of method call ignored",
    level: Level = Level.WARNING,
    uri: String = "src/Foo.java",
    startLine: Int = 10,
    startColumn: Int = 5,
    charLength: Int = 7,
    snippet: String = "execute()",
    additionalData: String? = null,
  ): Result =
    Result()
      .withRuleId(ruleId)
      .withMessage(Message().withText(message))
      .withLevel(level)
      .withLocations(listOf(location(uri, startLine, startColumn, charLength, snippet)))
      .apply {
        additionalData?.let { withProperties(PropertyBag().apply { put(BaselineEqualityV2.ADDITIONAL_FINGERPRINT_DATA, it) }) }
      }

  private fun location(uri: String, startLine: Int, startColumn: Int, charLength: Int, snippet: String): Location =
    Location().withPhysicalLocation(
      PhysicalLocation()
        .withArtifactLocation(ArtifactLocation().withUri(uri).withUriBaseId("SRCROOT"))
        .withRegion(
          Region()
            .withStartLine(startLine)
            .withStartColumn(startColumn)
            .withCharLength(charLength)
            .withSnippet(ArtifactContent().withText(snippet))
        )
    )
}
