package org.jetbrains.qodana.staticAnalysis.sarif

import com.jetbrains.qodana.sarif.baseline.BaselineCalculation
import com.jetbrains.qodana.sarif.model.ArtifactContent
import com.jetbrains.qodana.sarif.model.ArtifactLocation
import com.jetbrains.qodana.sarif.model.Location
import com.jetbrains.qodana.sarif.model.LogicalLocation
import com.jetbrains.qodana.sarif.model.Message
import com.jetbrains.qodana.sarif.model.PhysicalLocation
import com.jetbrains.qodana.sarif.model.PropertyBag
import com.jetbrains.qodana.sarif.model.Region
import com.jetbrains.qodana.sarif.model.Level
import com.jetbrains.qodana.sarif.model.Result
import com.jetbrains.qodana.sarif.model.VersionedMap
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.BaselineEqualityV2
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.EnclosingScopeIndicator
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.ExtractionAndRefactorTolerantIndicator
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.MoveAndRefactorTolerantIndicator
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.ScopeAndShapeTolerantIndicator
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.ShiftTolerantIndicator
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.withPartialFingerprints
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.addStructuralFingerprints
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.areStructuralFingerprintsEnabled
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.StructuralFingerprintSignals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit coverage for the structural / shift-tolerant fingerprint indicators and the [withPartialFingerprints] pipeline. */
class StructuralFingerprintsTest {

  @Test
  fun `shift tolerant key and version`() {
    assertEquals(BaselineCalculation.SHIFT_TOLERANT_INDICATOR, ShiftTolerantIndicator.name)
    assertEquals(1, ShiftTolerantIndicator.version)
  }

  @Test
  fun `shift tolerant is deterministic`() {
    val a = result()
    val b = result()
    assertEquals(ShiftTolerantIndicator.calculate(a), ShiftTolerantIndicator.calculate(b))
  }

  @Test
  fun `shift tolerant ignores the message text`() {
    assertEquals(ShiftTolerantIndicator.calculate(result()), ShiftTolerantIndicator.calculate(result(message = "other")))
  }

  @Test
  fun `shift tolerant ignores line and column shifts`() {
    val original = result(startLine = 10, startColumn = 5)
    val shifted = result(startLine = 42, startColumn = 17)
    assertEquals(ShiftTolerantIndicator.calculate(original), ShiftTolerantIndicator.calculate(shifted))
  }

  @Test
  fun `shift tolerant reacts to content changes`() {
    val base = result()
    assertNotEquals(ShiftTolerantIndicator.calculate(base), ShiftTolerantIndicator.calculate(result(ruleId = "Other")))
    assertNotEquals(ShiftTolerantIndicator.calculate(base), ShiftTolerantIndicator.calculate(result(additionalData = "x")))
    assertNotEquals(ShiftTolerantIndicator.calculate(base), ShiftTolerantIndicator.calculate(result(level = Level.ERROR)))
    assertNotEquals(ShiftTolerantIndicator.calculate(base), ShiftTolerantIndicator.calculate(result(charLength = 99)))
    assertNotEquals(ShiftTolerantIndicator.calculate(base), ShiftTolerantIndicator.calculate(result(snippet = "other()")))
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

  /**
   * The guard needs both signals. A problem at the top level of a file has a shape but no scope, so it gets
   * no move-tolerant hash at all.
   */
  @Test
  fun `move tolerant needs both shape and scope`() {
    assertNull(MoveAndRefactorTolerantIndicator.calculate(result(), signals()))
    assertNull(MoveAndRefactorTolerantIndicator.calculate(result(), signals(astShape = "IF_STATEMENT(BLOCK)")))
    assertNull(MoveAndRefactorTolerantIndicator.calculate(result(), signals(scopeName = "foo")))
    assertNotNull(MoveAndRefactorTolerantIndicator.calculate(result(), signals(astShape = "IF_STATEMENT(BLOCK)", scopeName = "foo")))
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
    // shift-tolerant is off by default outside the QDJVM and QDJS linters
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

  /** The linter distribution sets the default. Without it the indicators stay off. */
  @Test
  fun `the default property decides when the main one is absent`() {
    assertTrue(areStructuralFingerprintsEnabled(property = null, default = "true"))
    assertFalse(areStructuralFingerprintsEnabled(property = null, default = "false"))
    assertFalse(areStructuralFingerprintsEnabled(property = null, default = null))
  }

  /** The user sets the main property, so it wins over the default of the linter. */
  @Test
  fun `the main property overrides the default`() {
    assertTrue(areStructuralFingerprintsEnabled(property = "true", default = "false"))
    assertTrue(areStructuralFingerprintsEnabled(property = "true", default = null))
    assertFalse(areStructuralFingerprintsEnabled(property = "false", default = "true"))
  }

  @Test
  fun `scope and shape key and version`() {
    assertEquals("scopeAndShapeTolerantIndicator", ScopeAndShapeTolerantIndicator.name)
    assertEquals(1, ScopeAndShapeTolerantIndicator.version)
  }

  /** It reads only the problem element type, so the shape and the scope do not make it answer. */
  @Test
  fun `scope and shape needs the problem element type`() {
    assertNull(ScopeAndShapeTolerantIndicator.calculate(result(), signals()))
    assertNull(ScopeAndShapeTolerantIndicator.calculate(result(), signals(astShape = "IF_STATEMENT(BLOCK)", scopeName = "foo")))
    assertNotNull(ScopeAndShapeTolerantIndicator.calculate(result(), signals(problemElementType = "IF_STATEMENT")))
  }

  @Test
  fun `scope and shape reacts to element type and filename`() {
    val calc = ScopeAndShapeTolerantIndicator
    val base = calc.calculate(result(uri = "src/A.java"), signals(problemElementType = "IF_STATEMENT"))

    val otherType = calc.calculate(result(uri = "src/A.java"), signals(problemElementType = "FOR_STATEMENT"))
    assertNotEquals(base, otherType)

    val movedFile = calc.calculate(result(uri = "src/moved/B.java"), signals(problemElementType = "IF_STATEMENT"))
    assertNotEquals(base, movedFile)
  }

  /**
   * A problem at the top level of a file. The three indicators that need a scope stay absent, and only the
   * one that reads the problem element type answers.
   */
  @Test
  fun `pipeline skips the scope indicators for a top level problem`() {
    val result = result()
      .withPartialFingerprints()
      .addStructuralFingerprints(signals(astShape = "IF_STATEMENT(BLOCK)", problemElementType = "IF_STATEMENT"))

    val prints = result.partialFingerprints!!
    assertNull(prints.get(BaselineCalculation.EXTRACTION_AND_REFACTOR_TOLERANT_INDICATOR, 1))
    assertNull(prints.get(BaselineCalculation.MOVE_AND_REFACTOR_TOLERANT_INDICATOR, 1))
    assertNull(prints.get(BaselineCalculation.ENCLOSING_SCOPE_INDICATOR, 1))
    assertNotNull(prints.get(ScopeAndShapeTolerantIndicator.name, 1))
  }

  /**
   * A baseline matcher reads a hash at the version it asks for. So the shift-tolerant hash must sit at
   * version 1, and a shifted rerun of the same problem must find the baseline value there.
   */
  @Test
  fun `shift tolerant matches a baseline at its own version`() {
    val baseline = result(startLine = 10, startColumn = 5)
    val rerun = result(startLine = 42, startColumn = 17)

    val baselinePrints = VersionedMap<String>().apply {
      put(ShiftTolerantIndicator.name, ShiftTolerantIndicator.version, ShiftTolerantIndicator.calculate(baseline))
    }

    assertEquals(
      ShiftTolerantIndicator.calculate(rerun),
      baselinePrints.get(BaselineCalculation.SHIFT_TOLERANT_INDICATOR, 1),
    )
    assertNull(
      "a version bump would hide the hash from a baseline written at version 1",
      baselinePrints.get(BaselineCalculation.SHIFT_TOLERANT_INDICATOR, 2),
    )
  }

  /** The remaining inputs of the shift-tolerant hash: the file it points at and the base id of that file. */
  @Test
  fun `shift tolerant reacts to the location`() {
    val base = result()
    assertNotEquals(ShiftTolerantIndicator.calculate(base), ShiftTolerantIndicator.calculate(result(uri = "src/Other.java")))
    assertNotEquals(ShiftTolerantIndicator.calculate(base), ShiftTolerantIndicator.calculate(result(uriBaseId = "TESTROOT")))
  }

  /**
   * A result with a logical location and no physical one. The hash reads the logical name and kind, so a
   * problem without a file still gets a stable value that reacts to its own inputs.
   */
  @Test
  fun `shift tolerant reads a logical location`() {
    val base = logicalResult(name = "com.foo.Bar.baz", kind = "function")

    assertEquals(ShiftTolerantIndicator.calculate(base), ShiftTolerantIndicator.calculate(logicalResult(name = "com.foo.Bar.baz", kind = "function")))
    assertNotEquals(ShiftTolerantIndicator.calculate(base), ShiftTolerantIndicator.calculate(logicalResult(name = "com.foo.Bar.qux", kind = "function")))
    assertNotEquals(ShiftTolerantIndicator.calculate(base), ShiftTolerantIndicator.calculate(logicalResult(name = "com.foo.Bar.baz", kind = "member")))
  }

  /**
   * The indicator is coarse on purpose. Inside one file it reads only the rule and the problem element type,
   * so two problems of one rule on the same kind of element collide. Only `additionalFingerprintData`
   * separates them.
   */
  @Test
  fun `scope and shape collides inside one file`() {
    val calc = ScopeAndShapeTolerantIndicator
    val first = calc.calculate(result(uri = "src/A.java", startLine = 10), signals(problemElementType = "IF_STATEMENT"))
    val second = calc.calculate(result(uri = "src/A.java", startLine = 99), signals(problemElementType = "IF_STATEMENT"))
    assertEquals("two problems of one rule on one element kind share the hash", first, second)

    val otherRule = calc.calculate(result(uri = "src/A.java", ruleId = "Other"), signals(problemElementType = "IF_STATEMENT"))
    assertNotEquals(first, otherRule)

    val withData = calc.calculate(result(uri = "src/A.java", additionalData = "x"), signals(problemElementType = "IF_STATEMENT"))
    assertNotEquals(first, withData)
  }

  private fun signals(
    astShape: String? = null,
    scopeName: String? = null,
    scopeType: String? = null,
    problemElementType: String? = null,
  ): StructuralFingerprintSignals =
    StructuralFingerprintSignals(astShape, scopeName, scopeType, problemElementType)

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
    uriBaseId: String = "SRCROOT",
  ): Result =
    Result()
      .withRuleId(ruleId)
      .withMessage(Message().withText(message))
      .withLevel(level)
      .withLocations(listOf(location(uri, startLine, startColumn, charLength, snippet, uriBaseId)))
      .apply {
        additionalData?.let { withProperties(PropertyBag().apply { put(BaselineEqualityV2.ADDITIONAL_FINGERPRINT_DATA, it) }) }
      }

  /** A result whose location is logical, so the hash takes the other branch of the walk over locations. */
  private fun logicalResult(name: String, kind: String): Result =
    Result()
      .withRuleId("IgnoreResultOfCall")
      .withMessage(Message().withText("Result of method call ignored"))
      .withLevel(Level.WARNING)
      .withLocations(listOf(Location().withLogicalLocations(setOf(LogicalLocation().withName(name).withKind(kind)))))

  private fun location(uri: String, startLine: Int, startColumn: Int, charLength: Int, snippet: String, uriBaseId: String): Location =
    Location().withPhysicalLocation(
      PhysicalLocation()
        .withArtifactLocation(ArtifactLocation().withUri(uri).withUriBaseId(uriBaseId))
        .withRegion(
          Region()
            .withStartLine(startLine)
            .withStartColumn(startColumn)
            .withCharLength(charLength)
            .withSnippet(ArtifactContent().withText(snippet))
        )
    )
}
