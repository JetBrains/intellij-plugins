package org.intellij.plugin.mdx

import com.intellij.testFramework.junit5.TestApplication
import org.intellij.markdown.MarkdownElementType
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.accept
import org.intellij.markdown.ast.visitors.RecursiveVisitor
import org.intellij.markdown.parser.CancellationToken
import org.intellij.markdown.parser.MarkdownParser
import org.intellij.plugin.mdx.lang.parse.MdxFlavourDescriptor
import org.intellij.plugin.mdx.lang.parse.MdxJsxScanner
import org.intellij.plugin.mdx.lang.parse.MdxMarkdownLibElementTypes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@TestApplication
class MdxJsxScannerTest {
  @Test
  fun scansMarkdownLikeTextInExpressionAttribute() {
    val text = "<Alert value={[Target](./target.mdx)} />"

    val element = MdxJsxScanner.scanJsxElement(text, 0)

    assertTrue(element?.terminated == true)
    assertEquals(text.length, element?.range?.last)
  }

  @Test
  fun markdownLikeTextInExpressionAttributeBelongsToJsx() {
    val text: CharSequence = "Text <Alert value={[Target](./target.mdx)} /> end"
    val root = MarkdownParser(MdxFlavourDescriptor, cancellationToken = CancellationToken.NonCancellable)
      .parse(MarkdownElementType("MDX_TEST_ROOT"), text)
    var hasJsx = false
    root.accept(object : RecursiveVisitor() {
      override fun visitNode(node: ASTNode) {
        if (node.type == MdxMarkdownLibElementTypes.MDX_JSX_TEXT_ELEMENT) {
          hasJsx = true
        }
        super.visitNode(node)
      }
    })

    assertTrue(hasJsx)
  }

  @Test
  fun validRegularExpressionKeepsItsBrace() {
    val text = "<Alert value={/} /} />"

    val element = MdxJsxScanner.scanJsxElement(text, 0)

    assertTrue(element?.terminated == true)
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

    assertTrue(element?.terminated == true)
    assertEquals(text.length, element?.range?.last)
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

    assertTrue(element?.terminated == true)
    assertEquals(text.length, element?.range?.last)
  }

}
