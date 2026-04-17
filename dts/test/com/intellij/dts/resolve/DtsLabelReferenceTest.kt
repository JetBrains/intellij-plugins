package com.intellij.dts.resolve

import com.intellij.dts.DtsTestBase
import com.intellij.openapi.application.readAction

class DtsLabelReferenceTest : DtsTestBase() {
  private val target = "label: target_node {};"
  private val reference = "&<caret>label {};"

  fun `test ref`() = dtsTimeoutRunBlocking {
    val input = """
            label: &ref {};
            $reference
        """

    configureByText(input)

    val reference = myFixture.getReferenceAtCaretPositionWithAssertion()
    assertEquals("label: &ref {}", readAction { reference.resolve()?.text })
  }

  fun `test local`() = dtsTimeoutRunBlocking {
    val input = """
            / { $target };
            $reference
        """

    doTest(input)
  }

  fun `test local nested`() = dtsTimeoutRunBlocking {
    val input = """
            / {
                subNode { $target };
            };
            
            $reference
        """

    doTest(input)
  }

  fun `test include`() = dtsTimeoutRunBlocking {
    val input = """
            /include/ "test.dtsi"
            $reference
        """

    addFile("test.dtsi", "/ { $target };")

    doTest(input)
  }

  fun `test nested include`() = dtsTimeoutRunBlocking {
    val input = """
            /include/ "test0.dtsi"
            $reference
        """

    addFile("test0.dtsi", "/include/ \"test1.dtsi\"")
    addFile("test1.dtsi", "/ { $target };")

    doTest(input)
  }

  fun `test recursive include`() = dtsTimeoutRunBlocking {
    val input = """
            /include/ "test0.dtsi"
            $reference
        """

    addFile("test0.dtsi", "/include/ \"test1.dtsi\"")
    addFile("test1.dtsi", "/include/ \"test2.dtsi\"")
    addFile("test2.dtsi", "/include/ \"test0.dtsi\"\n/ { $target };")

    doTest(input)
  }

  fun `test target below ref node`() = dtsTimeoutRunBlocking {
    val input = """
            $reference
            / { $target };
        """

    configureByText(input)
    assertNull(readAction { myFixture.getReferenceAtCaretPosition()!!.resolve() })
  }

  fun `test target included below ref node`() = dtsTimeoutRunBlocking {
    val input = """
            $reference
            /include/ "test.dtsi"
        """

    addFile("test.dtsi", "/ { $target };")

    configureByText(input)
    assertNull(readAction { myFixture.getReferenceAtCaretPosition()!!.resolve() })
  }

  fun `test target below ref value`() = dtsTimeoutRunBlocking {
    val input = """
            / {
                prop = &<caret>label;
            };
            
            / { $target };
        """

    doTest(input)
  }

  fun `test target included below ref value`() = dtsTimeoutRunBlocking {
    val input = """
            / {
                prop = &<caret>label;
            };
            
            /include/ "test.dtsi"
        """

    addFile("test.dtsi", "/ { $target };")

    doTest(input)
  }

  private suspend fun doTest(input: String) {
    configureByText(input)

    val reference = myFixture.getReferenceAtCaretPositionWithAssertion()
    assertEquals(target.trimEnd(';'), readAction { reference.resolve()?.text })
  }
}