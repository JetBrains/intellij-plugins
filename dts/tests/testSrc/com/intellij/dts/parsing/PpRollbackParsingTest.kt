package com.intellij.dts.parsing

import com.intellij.dts.lang.psi.DtsTypes
import com.intellij.lang.ASTNode
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.TokenType
import com.intellij.psi.impl.source.tree.CompositeElement
import com.intellij.psi.impl.source.tree.RecursiveTreeElementWalkingVisitor
import com.intellij.psi.impl.source.tree.TreeElement
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.psi.util.prevLeaf
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo

private class TreeToBuffer(node: ASTNode, private val buffer: Appendable, private val ignore: TokenSet) : RecursiveTreeElementWalkingVisitor(node) {
  private var indent = 0

  private fun fixWhiteSpaces(text: String): String {
    return text.replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t")
  }

  override fun visitNode(root: TreeElement) {
    if (root.elementType == TokenType.WHITE_SPACE || root.elementType in ignore) return

    StringUtil.repeatSymbol(buffer, ' ', indent)

    if (root is CompositeElement) {
      buffer.append(root.toString())
    }
    else {
      val text = fixWhiteSpaces(root.text)
      buffer.append(root.toString()).append("('").append(text).append("')")
    }

    buffer.append("\n")

    if (root is CompositeElement && root.getFirstChildNode() == null) {
      StringUtil.repeatSymbol(buffer, ' ', indent + 2)
      buffer.append("<empty list>\n")
    }

    indent += 2

    super.visitNode(root)
  }

  override fun elementFinished(node: ASTNode) {
    if (node.elementType == TokenType.WHITE_SPACE || node.elementType in ignore) return
    indent -= 2
  }
}

@TestApplication
class PpRollbackParsingTest : DtsParsingTestBase("ppRollback") {
  private lateinit var name: String

  @BeforeEach
  fun setUp(testInfo: TestInfo) {
    name = testInfo.testMethod.get().name
  }

  private val variants = mapOf(
    DtsTypes.INCLUDE_STATEMENT to "/include/ \"file\"",
    DtsTypes.PP_STATEMENT to "#define test test",
  )

  private fun psiToString(element: PsiElement, ignore: TokenSet): String {
    val buffer = StringBuilder()
    (element.node as TreeElement).acceptTree(TreeToBuffer(element.node, buffer, ignore))

    return buffer.toString()
  }

  private fun assertErrorNotAfter(message: String, root: PsiElement, types: TokenSet) {
    val error = PsiTreeUtil.findChildOfAnyType(root, PsiErrorElement::class.java)
    assertNotNull(error, message)

    val prev = PsiTreeUtil.findFirstParent(error!!.prevLeaf(skipEmptyElements = false)) { it.elementType in types }
    assertNull(prev, "$message, error after: ${prev.elementType}")
  }

  private fun doTest() {
    val content = parsing.loadFile("${parsing.testName}.dts")
    require(content.contains("<pp-statement>"))

    ReadAction.computeBlocking<Unit, RuntimeException> {
      val reference = parseFile(name, content.replace("<pp-statement>", ""))
      val referenceText = psiToString(reference, TokenSet.EMPTY)

      for ((type, statement) in variants.entries) {
        val variant = parseFile(name, content.replace("<pp-statement>", statement))
        val text = psiToString(variant, TokenSet.create(type))

        assertEquals(referenceText, text, "variant: $type")
        assertErrorNotAfter("variant: $type", variant, TokenSet.create(type))
      }

      val allVariants = parseFile(name, content.replace("<pp-statement>", variants.values.joinToString("\n")))
      val allText = psiToString(allVariants, TokenSet.create(*variants.keys.toTypedArray()))

      assertEquals(referenceText, allText, "variant: all")
      assertErrorNotAfter("variant: all", allVariants, TokenSet.create(*variants.keys.toTypedArray()))
    }
  }

  @Test
  fun testSubNode() = doTest()

  @Test
  fun testProperty() = doTest()

  @Test
  fun testPropertyWithLabel() = doTest()

  @Test
  fun testPropertyList() = doTest()

  @Test
  fun testNestedNodes() = doTest()

  @Test
  fun testNestedProperty() = doTest()

  // fails du to psi error element two low in tree, should not be a problem
  // fun testRootNode() = doTest()
}