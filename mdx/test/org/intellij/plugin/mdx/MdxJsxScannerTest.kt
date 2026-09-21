package org.intellij.plugin.mdx

import com.intellij.openapi.util.TextRange
import com.intellij.testFramework.junit5.TestApplication
import org.intellij.plugin.mdx.lang.parse.MdxJsxScanner
import org.intellij.plugin.mdx.lang.parse.MdxTextRangeSet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@TestApplication
class MdxJsxScannerTest {
  @Test
  fun scansMarkdownLikeTextInExpressionAttribute() {
    val text = "<Alert value={[Target](./target.mdx)} />"

    val element = MdxJsxScanner.scanJsxElement(text, 0)

    assertEquals(MdxJsxScanner.Termination.MATCHED, element?.termination)
    assertEquals(text.length, element?.range?.endOffset)
  }

  @Test
  fun validRegularExpressionKeepsItsBrace() {
    val text = "<Alert value={/} /} />"

    val element = MdxJsxScanner.scanJsxElement(text, 0)

    assertEquals(MdxJsxScanner.Termination.MATCHED, element?.termination)
    assertEquals(text.length, element?.range?.endOffset)
  }

  @Test
  fun malformedRegularExpressionRecoversAtHostBrace() {
    val text = "<Alert value={/} />"

    val element = MdxJsxScanner.scanJsxElement(text, 0)

    assertEquals(MdxJsxScanner.Termination.MATCHED, element?.termination)
    assertEquals(text.length, element?.range?.endOffset)
  }

  @Test
  fun scansTemplateLiteralExpressionAttribute() {
    val text = """
      <Source language="tsx" code={`
        const [state, setState] = useState<SomeType>()
      `}/>
    """.trimIndent()

    val element = MdxJsxScanner.scanJsxElement(text, 0)

    assertEquals(MdxJsxScanner.Termination.MATCHED, element?.termination)
    assertEquals(text.length, element?.range?.endOffset)
  }

  @Test
  fun recordsEveryOpeningTagAttributeAndExpression() {
    val text = "<Card disabled title = \"a > b\" count=42 {...props} value={{ nested: true }} />"

    val tag = MdxJsxScanner.scanJsxElement(text, 0)?.tags?.single()

    assertEquals(
      listOf("disabled ", "title = \"a > b\"", "count=42", "value={{ nested: true }}"),
      tag?.attributes?.map { it.substring(text) },
    )
    assertEquals(listOf("{...props}", "{{ nested: true }}"), tag?.expressions?.map { it.substring(text) })
  }

  @Test
  fun recordsElementBodiesAndParentsDuringTagMatching() {
    val text = "<A><B>one</B><><C /></></A>"
    val records = MdxJsxScanner.scanJsxElement(text, 0)!!.elements.sortedBy { it.index }
    assertEquals(listOf(0, 1, 2, 3), records.map { it.index })
    assertEquals(listOf(null, 0, 0, 2), records.map { it.parentIndex })
    assertEquals(listOf(text, "<B>one</B>", "<><C /></>", "<C />"), records.map { it.range.substring(text) })
    assertEquals(listOf("<B>one</B><><C /></>", "one", "<C />", null), records.map { it.bodyRange?.substring(text) })
  }

  @Test
  fun suppliedOpaqueRangeHidesMatchingClosingTag() {
    val text = "<span>`</span>` body</span>"
    val opaqueStart = text.indexOf('`')
    val opaqueEnd = text.indexOf('`', opaqueStart + 1) + 1

    val element = MdxJsxScanner.scanJsxElement(
      text,
      0,
      opaqueRanges = MdxTextRangeSet.of(listOf(TextRange(opaqueStart, opaqueEnd))),
    )

    assertEquals(MdxJsxScanner.Termination.MATCHED, element?.termination)
    assertEquals(text.length, element?.range?.endOffset)
    assertEquals(listOf("span", "span"), element?.tags?.map { it.name })
  }

  @Test
  fun recognizesMultilineExpressionAttributeAsJsxBlockStart() {
    assertTrue(MdxJsxScanner.isLineStartJsx("<Source code={`", 0))
    assertTrue(MdxJsxScanner.isLineStartJsx("<div onClick={(e) => {", 0))
  }

  @Test
  fun scansMultilineArrowFunctionAttribute() {
    val text = """
      <div onClick={(e) => {
          console.log(e)
      }}>
          Hello
      </div>
    """.trimIndent()

    val element = MdxJsxScanner.scanJsxElement(text, 0)

    assertEquals(MdxJsxScanner.Termination.MATCHED, element?.termination)
    assertEquals(text.length, element?.range?.endOffset)
  }

  @Test
  fun unrelatedCloserRecoversElementImmediately() {
    val text = "<Outer><Inner></Other>after</Inner></Outer>"
    val recoveryEnd = text.indexOf("</Other>") + "</Other>".length

    val element = MdxJsxScanner.scanJsxElement(text, 0)

    assertEquals(MdxJsxScanner.Termination.RECOVERED, element?.termination)
    assertEquals(recoveryEnd, element?.range?.endOffset)
    assertTrue(element?.incomplete == true)
  }

  @Test
  fun skippedNamedDescendantKeepsMatchedElementIncomplete() {
    val text = "<Outer><Inner></Outer>"

    val element = MdxJsxScanner.scanJsxElement(text, 0)

    assertEquals(MdxJsxScanner.Termination.MATCHED, element?.termination)
    assertEquals(text.length, element?.range?.endOffset)
    assertTrue(element?.incomplete == true)
  }

  @Test
  fun namedTagsAndFragmentsOnlyMatchIdenticalClosers() {
    val cases = listOf(
      "<Outer><></Outer>",
      "<><Inner></>",
    )

    for (text in cases) {
      val element = MdxJsxScanner.scanJsxElement(text, 0)

      assertEquals(MdxJsxScanner.Termination.MATCHED, element?.termination, text)
      assertEquals(text.length, element?.range?.endOffset, text)
      assertTrue(element?.incomplete == true, text)
    }
  }

  @Test
  fun incrementalSessionMatchesOneShotPrefixes() {
    val text = """
      <Outer
        title=">">
        <Inner value={{ nested: true }} />
        {items.map(item => <Item value={item} />)}
      </Outer>
    """.trimIndent()
    val session = MdxJsxScanner.Session(text, 0)

    for (limit in lineEnds(text)) {
      assertEquals(
        MdxJsxScanner.scanJsxElement(text, 0, limit),
        session.advanceTo(limit),
        "limit=$limit",
      )
    }
  }

  @Test
  fun incrementalSessionMatchesOneShotRecoveryAtEveryPrefix() {
    val cases = listOf(
      "<Alert value={/} />",
      "<Outer><Inner></Outer>",
      "<Outer><Inner></Other>after</Inner></Outer>",
      "<><Inner></>",
      "<Outer><></Outer>",
      "<Outer>{/}</Outer>",
    )

    for (text in cases) {
      val session = MdxJsxScanner.Session(text, 0)
      for (limit in 1..text.length) {
        assertEquals(
          MdxJsxScanner.scanJsxElement(text, 0, limit),
          session.advanceTo(limit),
          "text=$text, limit=$limit",
        )
      }
    }
  }

  @Test
  fun incrementalSessionDoesNotReadBeyondExposedPrefix() {
    val tags = listOf(
      "<Alert value={/} /} />",
      "<Card disabled title=\"a > b\" count=42 {...props} value={{ nested: true }} />",
      "<Outer>{/}</Outer>",
    )

    for (text in tags) {
      val guarded = PrefixGuardCharSequence(text)
      guarded.expose(1)
      val session = MdxJsxScanner.Session(guarded, 0)
      for (limit in 1..text.length) {
        guarded.expose(limit)
        assertEquals(MdxJsxScanner.scanJsxElement(text, 0, limit), session.advanceTo(limit), "tag=$text, limit=$limit")
      }
    }
  }

  @Test
  fun lateOpacityInMixedBatchRollsBackAccumulatedSyntax() {
    val text = "<Box>\n`{value}</Box>`\n</Box>"
    val firstLineEnd = text.indexOf('\n') + 1
    val codeLineEnd = text.indexOf('\n', firstLineEnd) + 1
    val codeStart = text.indexOf('`')
    val codeEnd = text.indexOf('`', codeStart + 1) + 1
    val firstCloserEnd = text.indexOf("</Box>") + "</Box>".length
    val session = MdxJsxScanner.Session(text, 0)

    session.advanceTo(firstLineEnd)
    val premature = session.advanceTo(codeLineEnd)
    assertEquals(firstCloserEnd, premature?.range?.endOffset)
    assertEquals(1, premature?.expressions?.size)
    val retainedTags = premature?.tags?.toList()
    val retainedExpressions = premature?.expressions?.toList()

    session.addOpaqueRanges(listOf(TextRange(0, 1), TextRange(codeStart, codeEnd)))
    val repaired = session.advanceTo(text.length)

    assertEquals(MdxJsxScanner.Termination.MATCHED, repaired?.termination)
    assertEquals(text.length, repaired?.range?.endOffset)
    assertEquals(listOf("Box", "Box"), repaired?.tags?.map { it.name })
    assertEquals(emptyList<TextRange>(), repaired?.expressions)
    assertEquals(retainedTags, premature?.tags)
    assertEquals(retainedExpressions, premature?.expressions)
  }

  @Test
  fun repeatedLimitsPreserveLateOpacityRollback() {
    val text = "<Box>\n`{value}</Box>`\n</Box>"
    val openingEnd = text.indexOf('\n') + 1
    val codeLineEnd = text.indexOf('\n', openingEnd) + 1
    val codeStart = text.indexOf('`')
    val codeEnd = text.indexOf('`', codeStart + 1) + 1

    for (repairAtSameLimit in listOf(false, true)) {
      val session = MdxJsxScanner.Session(text, 0)
      val opening = session.advanceTo(openingEnd)
      assertSame(opening, session.advanceTo(openingEnd))
      val premature = session.advanceTo(codeLineEnd)
      val retainedTags = premature?.tags?.toList()
      val retainedExpressions = premature?.expressions?.toList()
      val retainedElements = premature?.elements?.toList()
      repeat(3) {
        assertSame(premature, session.advanceTo(codeLineEnd))
      }

      session.addOpaqueRanges(listOf(TextRange(codeStart, codeEnd)))
      if (repairAtSameLimit) {
        val repaired = session.advanceTo(codeLineEnd)
        assertEquals(MdxJsxScanner.Termination.UNTERMINATED, repaired?.termination)
        assertEquals(listOf("Box"), repaired?.tags?.map { it.name })
        assertSame(repaired, session.advanceTo(codeLineEnd))
      }
      val complete = session.advanceTo(text.length)

      assertEquals(MdxJsxScanner.Termination.MATCHED, complete?.termination)
      assertEquals(text.length, complete?.range?.endOffset)
      assertEquals(listOf("Box", "Box"), complete?.tags?.map { it.name })
      assertEquals(emptyList<TextRange>(), complete?.expressions)
      assertEquals(retainedTags, premature?.tags)
      assertEquals(retainedExpressions, premature?.expressions)
      assertEquals(retainedElements, premature?.elements)
      assertEquals(listOf(TextRange(0, text.length)), complete?.elements?.map { it.range })
      assertSame(complete, session.advanceTo(text.length))
    }
  }

  @Test
  fun incrementallyScansDeepNesting() {
    val depth = 5_000
    val openingPrefix = "<E>".repeat(depth)
    val text = openingPrefix + "</E>".repeat(depth)
    val session = MdxJsxScanner.Session(text, 0)

    val prefix = session.advanceTo(openingPrefix.length)
    val element = session.advanceTo(text.length)

    assertEquals(MdxJsxScanner.Termination.UNTERMINATED, prefix?.termination)
    assertEquals(depth, prefix?.tags?.size)
    assertEquals(MdxJsxScanner.Termination.MATCHED, element?.termination)
    assertEquals(text.length, element?.range?.endOffset)
    assertEquals(depth * 2, element?.tags?.size)
  }
}
