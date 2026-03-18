package org.jetbrains.qodana.staticAnalysis.sarif

import com.jetbrains.qodana.sarif.model.ArtifactContent
import com.jetbrains.qodana.sarif.model.ArtifactLocation
import com.jetbrains.qodana.sarif.model.CodeFlow
import com.jetbrains.qodana.sarif.model.Edge
import com.jetbrains.qodana.sarif.model.Graph
import com.jetbrains.qodana.sarif.model.Location
import com.jetbrains.qodana.sarif.model.LocationRelationship
import com.jetbrains.qodana.sarif.model.Message
import com.jetbrains.qodana.sarif.model.Node
import com.jetbrains.qodana.sarif.model.PhysicalLocation
import com.jetbrains.qodana.sarif.model.PropertyBag
import com.jetbrains.qodana.sarif.model.Region
import com.jetbrains.qodana.sarif.model.Result
import com.jetbrains.qodana.sarif.model.ThreadFlow
import com.jetbrains.qodana.sarif.model.ThreadFlowLocation
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.BaselineEqualityV1
import org.jetbrains.qodana.staticAnalysis.sarif.fingerprints.BaselineEqualityV2
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class CodeFlowFingerprintsTest {
  @Test
  fun `code flow matches equivalent legacy graph`() {
    val steps = listOf(
      TraceStep(1, "src/Foo.kt", 10, 5, 9, "userInput", "source"),
      TraceStep(2, "src/Foo.kt", 14, 11, 9, "sanitized", "middle"),
      TraceStep(3, "src/Foo.kt", 20, 9, 7, "execute", "sink"),
    )
    val locations = listOf(primaryLocation(3))

    val graphResult = resultWithGraph(steps, locations)
    val codeFlowResult = resultWithCodeFlow(
      listOf(steps[1], steps[0], steps[2]),
      locations
    )

    assertEquals(BaselineEqualityV1.calculate(graphResult), BaselineEqualityV1.calculate(codeFlowResult))
    assertEquals(BaselineEqualityV2.calculate(graphResult), BaselineEqualityV2.calculate(codeFlowResult))
  }

  @Test
  fun `code flow contributes to fingerprint`() {
    val locations = listOf(primaryLocation(3))
    val original = resultWithCodeFlow(
      listOf(
        TraceStep(1, "src/Foo.kt", 10, 5, 9, "userInput", "source"),
        TraceStep(2, "src/Foo.kt", 14, 11, 9, "sanitized", "middle"),
        TraceStep(3, "src/Foo.kt", 20, 9, 7, "execute", "sink"),
      ),
      locations
    )
    val changed = resultWithCodeFlow(
      listOf(
        TraceStep(1, "src/Foo.kt", 10, 5, 9, "userInput", "source"),
        TraceStep(2, "src/Foo.kt", 16, 3, 7, "filtered", "middle"),
        TraceStep(3, "src/Foo.kt", 20, 9, 7, "execute", "sink"),
      ),
      locations
    )

    assertNotEquals(BaselineEqualityV1.calculate(original), BaselineEqualityV1.calculate(changed))
    assertNotEquals(BaselineEqualityV2.calculate(original), BaselineEqualityV2.calculate(changed))
  }

  @Test
  fun `regular code flow affects fingerprint`() {
    val locations = listOf(primaryLocation(3))
    val original = resultWithCodeFlow(
      listOf(
        TraceStep(1, "src/Foo.kt", 10, 5, 9, "userInput", "source"),
        TraceStep(2, "src/Foo.kt", 14, 11, 9, "sanitized", "middle"),
        TraceStep(3, "src/Foo.kt", 20, 9, 7, "execute", "sink"),
      ),
      locations
    )
    val changed = resultWithCodeFlow(
      listOf(
        TraceStep(1, "src/Foo.kt", 10, 5, 9, "userInput", "source"),
        TraceStep(2, "src/Foo.kt", 16, 3, 7, "filtered", "middle"),
        TraceStep(3, "src/Foo.kt", 20, 9, 7, "execute", "sink"),
      ),
      locations
    )

    assertNotEquals(BaselineEqualityV1.calculate(original), BaselineEqualityV1.calculate(changed))
    assertNotEquals(BaselineEqualityV2.calculate(original), BaselineEqualityV2.calculate(changed))
  }

  @Test
  fun `step fqn does not affect code flow fingerprint`() {
    val locations = listOf(primaryLocation(3))
    val original = resultWithCodeFlow(
      listOf(
        TraceStep(1, "src/Foo.kt", 10, 5, 9, "userInput", "source"),
        TraceStep(2, "src/Foo.kt", 14, 11, 9, "sanitized", "middle"),
        TraceStep(3, "src/Foo.kt", 20, 9, 7, "execute", "sink"),
      ),
      locations
    )
    val changed = resultWithCodeFlow(
      listOf(
        TraceStep(1, "src/Foo.kt", 10, 5, 9, "userInput", "another.Source"),
        TraceStep(2, "src/Foo.kt", 14, 11, 9, "sanitized", "another.Middle"),
        TraceStep(3, "src/Foo.kt", 20, 9, 7, "execute", "another.Sink"),
      ),
      locations
    )

    assertEquals(BaselineEqualityV1.calculate(original), BaselineEqualityV1.calculate(changed))
    assertEquals(BaselineEqualityV2.calculate(original), BaselineEqualityV2.calculate(changed))
  }

  private fun resultWithGraph(steps: List<TraceStep>, locations: List<Location>): Result {
    val nodes = linkedSetOf<Node>()
    val edges = linkedSetOf<Edge>()
    val sortedSteps = steps.sortedBy { it.order }
    sortedSteps.forEach { step ->
      nodes.add(
        Node(step.order.toString())
          .withLocation(step.location())
      )
    }
    sortedSteps.zipWithNext().forEachIndexed { index, (source, target) ->
      edges.add(Edge((index + 1).toString(), source.order.toString(), target.order.toString()))
    }

    return result(locations)
      .withGraphs(linkedSetOf(Graph().withNodes(nodes).withEdges(edges)))
  }

  private fun resultWithCodeFlow(steps: List<TraceStep>, locations: List<Location>): Result {
    val threadFlowLocations = steps.map { step ->
      ThreadFlowLocation()
        .withExecutionOrder(step.order)
        .withLocation(step.location())
        .withProperties(step.properties())
    }

    return result(locations)
      .withCodeFlows(
        listOf(
          CodeFlow().withThreadFlows(
            listOf(
              ThreadFlow().withLocations(threadFlowLocations)
            )
          )
        )
      )
  }

  private fun result(locations: List<Location>): Result {
    return Result()
      .withRuleId("JvmTaintAnalysis")
      .withMessage(Message().withText("User-controlled data reaches a sink"))
      .withLocations(locations)
  }

  private fun primaryLocation(targetId: Int): Location {
    return location("src/Foo.kt", 20, 9, 7, "execute")
      .withRelationships(
        setOf(
          LocationRelationship()
            .withTarget(targetId)
            .withKinds(setOf("includes"))
        )
      )
  }

  private fun location(
    uri: String,
    startLine: Int,
    startColumn: Int,
    charLength: Int,
    snippetText: String
  ): Location {
    return Location().withPhysicalLocation(
      PhysicalLocation()
        .withArtifactLocation(
          ArtifactLocation()
            .withUri(uri)
            .withUriBaseId("SRCROOT")
        )
        .withRegion(
          Region()
            .withStartLine(startLine)
            .withStartColumn(startColumn)
            .withCharLength(charLength)
            .withSourceLanguage("kotlin")
            .withSnippet(ArtifactContent().withText(snippetText))
        )
    )
  }

  private data class TraceStep(
    val order: Int,
    val uri: String,
    val startLine: Int,
    val startColumn: Int,
    val charLength: Int,
    val snippetText: String,
    val stepFqn: String,
  )

  private fun TraceStep.location(): Location = location(uri, startLine, startColumn, charLength, snippetText)

  private fun TraceStep.properties(): PropertyBag = PropertyBag().also { it["step_fqn"] = stepFqn }
}
