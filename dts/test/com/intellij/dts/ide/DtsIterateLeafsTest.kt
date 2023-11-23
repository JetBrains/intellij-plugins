package com.intellij.dts.ide

import com.intellij.dts.DtsTestBase
import com.intellij.dts.lang.DtsTokenSets
import com.intellij.dts.util.DtsUtil
import com.intellij.psi.TokenType
import com.intellij.psi.impl.source.tree.LeafElement
import com.intellij.psi.impl.source.tree.RecursiveTreeElementWalkingVisitor
import com.intellij.psi.impl.source.tree.TreeElement

private class PsiToString : RecursiveTreeElementWalkingVisitor() {
  val buffer = StringBuffer()

  override fun visitLeaf(leaf: LeafElement) {
    val type = leaf.elementType

    if (type !in DtsTokenSets.comments && type != TokenType.WHITE_SPACE) {
      buffer.append(leaf.text)
    }
  }
}

class DtsIterateLeafsTest : DtsTestBase() {
  private val unproductiveStatements = """
        // comment
        /include/ "file"
        #include "file"
        /*comment*/
    """

  override fun getBasePath(): String = "ide/iterateLeafs"

  fun `test root node`() = doTest(
    input = "/ {};",
    leafs = listOf("/", "{", "}", ";"),
  )

  fun `test root node with unproductive statement`() = doTest(
    input = "/ { $unproductiveStatements };",
    leafs = listOf("/", "{", "}", ";"),
  )

  fun `test root node with unproductive`() = doTest(
    input = "/ { };",
    leafs = listOf("/", "{", "}", ";"),
  )

  fun `test property`() = doTest(
    input = "/ { prop = $unproductiveStatements <>; };",
    leafs = listOf("/", "{", "prop", "=", "<", ">", ";", "}", ";")
  )

  fun `test file`() {
    val fixture = getTestFixture("dts")

    configureByText(fixture)
    val file = myFixture.file

    val start = file.findElementAt(0)
    val actual = DtsUtil.iterateLeafs(start!!, strict = false).map { it.text }.joinToString("")

    val visitor = PsiToString()
    (file.node as TreeElement).acceptTree(visitor)

    assertEquals(visitor.buffer.toString(), actual)
  }

  private fun doTest(input: String, leafs: List<String>) {
    configureByText(input)

    val start = myFixture.file.findElementAt(0)
    val actual = DtsUtil.iterateLeafs(start!!, strict = false).map { it.text }.toList()

    assertOrderedEquals(actual, leafs)
  }
}