package com.intellij.dts.ide

import com.intellij.dts.core.lang.DtsTokenSets
import com.intellij.dts.core.util.DtsUtil
import com.intellij.dts.dtsFixture
import com.intellij.dts.dtsTimeoutRunBlocking
import com.intellij.lang.ASTNode
import com.intellij.openapi.application.readAction
import com.intellij.psi.TokenType
import com.intellij.psi.impl.source.tree.LeafElement
import com.intellij.psi.impl.source.tree.RecursiveTreeElementWalkingVisitor
import com.intellij.psi.impl.source.tree.TreeElement
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.intellij.testFramework.junit5.fixture.tempPathFixture
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

private class PsiToString(node: ASTNode) : RecursiveTreeElementWalkingVisitor(node) {
  val buffer = StringBuffer()

  override fun visitLeaf(leaf: LeafElement) {
    val type = leaf.elementType

    if (type !in DtsTokenSets.comments && type != TokenType.WHITE_SPACE) {
      buffer.append(leaf.text)
    }
  }
}

@TestApplication
@TestDataPath($$"$PROJECT_ROOT/contrib/dts/tests/testData/ide/iterateLeafs")
class DtsIterateLeafsTest {
  private val tempPathFixture = tempPathFixture()
  private val projectFixture = projectFixture(tempPathFixture, openAfterCreation = true)

  private val dts by dtsFixture(projectFixture)

  private val myFixture get() = dts.codeInsight

  private val unproductiveStatements = """
        // comment
        /include/ "file"
        #include "file"
        /*comment*/
    """

  @Test
  fun `test root node`() = dtsTimeoutRunBlocking {
    doTest(
      input = "/ {};",
      leafs = listOf("/", "{", "}", ";"),
    )
  }

  @Test
  fun `test root node with unproductive statement`() = dtsTimeoutRunBlocking {
    doTest(
      input = "/ { $unproductiveStatements };",
      leafs = listOf("/", "{", "}", ";"),
    )
  }

  @Test
  fun `test root node with unproductive`() = dtsTimeoutRunBlocking {
    doTest(
      input = "/ { };",
      leafs = listOf("/", "{", "}", ";"),
    )
  }

  @Test
  fun `test property`() = dtsTimeoutRunBlocking {
    doTest(
      input = "/ { prop = $unproductiveStatements <>; };",
      leafs = listOf("/", "{", "prop", "=", "<", ">", ";", "}", ";")
    )
  }

  @Test
  fun `test file`() = dtsTimeoutRunBlocking {
    val fixture = dts.getTestFixture("dts")

    dts.configureByText(fixture)
    val file = myFixture.file

    val start = file.findElementAt(0)
    val actual = readAction {
      DtsUtil.iterateLeafs(start!!, strict = false).joinToString("") { it.text }
    }
    val visitor = PsiToString(file.node)
    (file.node as TreeElement).acceptTree(visitor)

    assertEquals(visitor.buffer.toString(), actual)
  }

  private suspend fun doTest(input: String, leafs: List<String>) {
    dts.configureByText(input)

    val start = myFixture.file.findElementAt(0)
    val actual = readAction { DtsUtil.iterateLeafs(start!!, strict = false).map { it.text }.toList() }

    assertEquals(leafs, actual)
  }
}