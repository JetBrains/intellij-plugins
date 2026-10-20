package com.intellij.dts.resolve

import com.intellij.dts.dtsFixture
import com.intellij.dts.dtsTimeoutRunBlocking
import com.intellij.openapi.application.readAction
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import com.intellij.testFramework.junit5.fixture.tempPathFixture
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

@TestApplication
class DtsPathReferenceTest {
  private val tempPathFixture = tempPathFixture()
  private val projectFixture = projectFixture(tempPathFixture, openAfterCreation = true)

  private val dts by dtsFixture(projectFixture)

  private val myFixture get() = dts.codeInsight

  private val target = "target_node {};"

  @Test
  fun `test root`() = dtsTimeoutRunBlocking {
    val input = """
            / {};
            &{<caret>/} {};
        """

    dts.configureByText(input)

    val reference = readAction { myFixture.getReferenceAtCaretPositionWithAssertion() }
    assertEquals("/ {}", readAction { reference.resolve()?.text })
  }

  @Test
  fun `test local`() = dtsTimeoutRunBlocking {
    val input = """
            / { $target };
            ${pathNode("/")}
        """

    doTest(input)
  }

  @Test
  fun `test local nested`() = dtsTimeoutRunBlocking {
    val input = """
            / { 
                subNode { $target };
            };
            
            ${pathNode("/subNode/")}
        """

    doTest(input)
  }

  @Test
  fun `test include`() = dtsTimeoutRunBlocking {
    val input = """
            /include/ "test.dtsi"
            ${pathNode("/")}
        """

    dts.addFile("test.dtsi", "/ { $target };")

    doTest(input)
  }

  @Test
  fun `test nested include`() = dtsTimeoutRunBlocking {
    val input = """
            /include/ "test0.dtsi"
            ${pathNode("/")}
        """

    dts.addFile("test0.dtsi", "/include/ \"test1.dtsi\"")
    dts.addFile("test1.dtsi", "/ { $target };")

    doTest(input)
  }

  @Test
  fun `test recursive include`() = dtsTimeoutRunBlocking {
    val input = """
            /include/ "test0.dtsi"
            ${pathNode("/")}
        """

    dts.addFile("test0.dtsi", "/include/ \"test1.dtsi\"")
    dts.addFile("test1.dtsi", "/include/ \"test2.dtsi\"")
    dts.addFile("test2.dtsi", "/include/ \"test0.dtsi\"\n/ { $target };")

    doTest(input)
  }

  @Test
  fun `test target below ref node`() = dtsTimeoutRunBlocking {
    val input = """
            ${pathNode("/")}
            / { $target };
        """

    dts.configureByText(input)
    assertNull(readAction { myFixture.getReferenceAtCaretPosition()!!.resolve() })
  }

  @Test
  fun `test target included below ref node`() = dtsTimeoutRunBlocking {
    val input = """
            ${pathNode("/")}
            /include/ "test.dtsi"
        """

    dts.addFile("test.dtsi", "/ { $target };")

    dts.configureByText(input)
    assertNull(readAction { myFixture.getReferenceAtCaretPosition()!!.resolve() })
  }

  @Test
  fun `test target below ref value`() = dtsTimeoutRunBlocking {
    val input = """
            / {
                prop = &{<caret>/target_node};
            };
            
            / { $target };
        """

    doTest(input)
  }

  @Test
  fun `test target included below ref value`() = dtsTimeoutRunBlocking {
    val input = """
            / {
               prop = &{<caret>/target_node};
            };
            
            /include/ "test.dtsi"
        """

    dts.addFile("test.dtsi", "/ { $target };")

    doTest(input)
  }

  @Test
  fun `test target in ref node`() = dtsTimeoutRunBlocking {
    val input = """
            / {
                label: subNode {};
            };
            
            &label {
                $target
            };
            
            ${pathNode("/subNode/")}
        """

    doTest(input)
  }

  private fun pathNode(path: String) = "&{<caret>${path}target_node} {};"

  private suspend fun doTest(input: String) {
    dts.configureByText(input)

    val reference = myFixture.getReferenceAtCaretPositionWithAssertion()
    assertEquals(target.trimEnd(';'), readAction { reference.resolve()?.text })
  }
}