package org.intellij.plugin.mdx

import com.intellij.testFramework.junit5.TestApplication
import org.intellij.plugin.mdx.lang.parse.MdxJsxScanner
import org.intellij.plugin.mdx.lang.parse.MdxOpaqueRanges
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@TestApplication
class MdxJsxScannerTest {
  @Test
  fun scansMarkdownLikeTextInExpressionAttribute() {
    val text = "<Alert value={[Target](./target.mdx)} />"

    val element = MdxJsxScanner.scanJsxElement(text, 0)

    assertEquals(MdxJsxScanner.Termination.MATCHED, element?.termination)
    assertEquals(text.length, element?.range?.last)
  }

  @Test
  fun validRegularExpressionKeepsItsBrace() {
    val text = "<Alert value={/} /} />"

    val element = MdxJsxScanner.scanJsxElement(text, 0)

    assertEquals(MdxJsxScanner.Termination.MATCHED, element?.termination)
    assertEquals(text.length, element?.range?.last)
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
    assertEquals(text.length, element?.range?.last)
  }

  @Test
  fun suppliedOpaqueRangeHidesMatchingClosingTag() {
    val text = "<span>`</span>` body</span>"
    val opaqueStart = text.indexOf('`')
    val opaqueEnd = text.indexOf('`', opaqueStart + 1) + 1

    val element = MdxJsxScanner.scanJsxElement(text, 0, opaqueRanges = MdxOpaqueRanges.of(listOf(opaqueStart..opaqueEnd)))

    assertEquals(MdxJsxScanner.Termination.MATCHED, element?.termination)
    assertEquals(text.length, element?.range?.last)
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
    assertEquals(text.length, element?.range?.last)
  }

  @Test
  fun unrelatedCloserRecoversElementImmediately() {
    val text = "<Outer><Inner></Other>after</Inner></Outer>"
    val recoveryEnd = text.indexOf("</Other>") + "</Other>".length

    val element = MdxJsxScanner.scanJsxElement(text, 0)

    assertEquals(MdxJsxScanner.Termination.RECOVERED, element?.termination)
    assertEquals(recoveryEnd, element?.range?.last)
    assertTrue(element?.incomplete == true)
  }

  @Test
  fun skippedNamedDescendantKeepsMatchedElementIncomplete() {
    val text = "<Outer><Inner></Outer>"

    val element = MdxJsxScanner.scanJsxElement(text, 0)

    assertEquals(MdxJsxScanner.Termination.MATCHED, element?.termination)
    assertEquals(text.length, element?.range?.last)
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
      assertEquals(text.length, element?.range?.last, text)
      assertTrue(element?.incomplete == true, text)
    }
  }
}
