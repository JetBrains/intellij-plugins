package com.intellij.dts.resolve

import com.intellij.dts.dtsFixture
import com.intellij.dts.dtsTimeoutRunBlocking
import com.intellij.openapi.application.readAction
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

@TestApplication
class DtsLabelReferenceTest {
  companion object {
    private val projectFixture = projectFixture(openAfterCreation = true)
  }

  private val dts by dtsFixture(projectFixture)

  private val myFixture get() = dts.codeInsight

  private val target = "label: target_node {};"
  private val reference = "&<caret>label {};"

  @Test
  fun `test ref`() = dtsTimeoutRunBlocking {
    val input = """
            label: &ref {};
            $reference
        """

    dts.configureByText(input)

    val reference = myFixture.getReferenceAtCaretPositionWithAssertion()
    assertEquals("label: &ref {}", readAction { reference.resolve()?.text })
  }

  @Test
  fun `test local`() = dtsTimeoutRunBlocking {
    val input = """
            / { $target };
            $reference
        """

    doTest(input)
  }

  @Test
  fun `test local nested`() = dtsTimeoutRunBlocking {
    val input = """
            / {
                subNode { $target };
            };
            
            $reference
        """

    doTest(input)
  }

  @Test
  fun `test include`() = dtsTimeoutRunBlocking {
    val input = """
            /include/ "test.dtsi"
            $reference
        """

    dts.addFile("test.dtsi", "/ { $target };")

    doTest(input)
  }

  @Test
  fun `test nested include`() = dtsTimeoutRunBlocking {
    val input = """
            /include/ "test0.dtsi"
            $reference
        """

    dts.addFile("test0.dtsi", "/include/ \"test1.dtsi\"")
    dts.addFile("test1.dtsi", "/ { $target };")

    doTest(input)
  }

  @Test
  fun `test recursive include`() = dtsTimeoutRunBlocking {
    val input = """
            /include/ "test0.dtsi"
            $reference
        """

    dts.addFile("test0.dtsi", "/include/ \"test1.dtsi\"")
    dts.addFile("test1.dtsi", "/include/ \"test2.dtsi\"")
    dts.addFile("test2.dtsi", "/include/ \"test0.dtsi\"\n/ { $target };")

    doTest(input)
  }

  @Test
  fun `test target below ref node`() = dtsTimeoutRunBlocking {
    val input = """
            $reference
            / { $target };
        """

    dts.configureByText(input)
    assertNull(readAction { myFixture.getReferenceAtCaretPosition()!!.resolve() })
  }

  @Test
  fun `test target included below ref node`() = dtsTimeoutRunBlocking {
    val input = """
            $reference
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
                prop = &<caret>label;
            };
            
            / { $target };
        """

    doTest(input)
  }

  @Test
  fun `test target included below ref value`() = dtsTimeoutRunBlocking {
    val input = """
            / {
                prop = &<caret>label;
            };
            
            /include/ "test.dtsi"
        """

    dts.addFile("test.dtsi", "/ { $target };")

    doTest(input)
  }

  private suspend fun doTest(input: String) {
    dts.configureByText(input)

    val reference = myFixture.getReferenceAtCaretPositionWithAssertion()
    assertEquals(target.trimEnd(';'), readAction { reference.resolve()?.text })
  }
}