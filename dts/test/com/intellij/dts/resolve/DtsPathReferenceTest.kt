package com.intellij.dts.resolve

import com.intellij.dts.DtsTestBase
import com.intellij.openapi.application.readAction

class DtsPathReferenceTest : DtsTestBase() {
  private val target = "target_node {};"

  fun `test root`() = dtsTimeoutRunBlocking {
    val input = """
            / {};
            &{<caret>/} {};
        """

    configureByText(input)

    val reference = readAction { myFixture.getReferenceAtCaretPositionWithAssertion() }
    assertEquals("/ {}", readAction { reference.resolve()?.text })
  }

  fun `test local`() = dtsTimeoutRunBlocking {
    val input = """
            / { $target };
            ${pathNode("/")}
        """

    doTest(input)
  }

  fun `test local nested`() = dtsTimeoutRunBlocking {
    val input = """
            / { 
                subNode { $target };
            };
            
            ${pathNode("/subNode/")}
        """

    doTest(input)
  }

  fun `test include`() = dtsTimeoutRunBlocking {
    val input = """
            /include/ "test.dtsi"
            ${pathNode("/")}
        """

    addFile("test.dtsi", "/ { $target };")

    doTest(input)
  }

  fun `test nested include`() = dtsTimeoutRunBlocking {
    val input = """
            /include/ "test0.dtsi"
            ${pathNode("/")}
        """

    addFile("test0.dtsi", "/include/ \"test1.dtsi\"")
    addFile("test1.dtsi", "/ { $target };")

    doTest(input)
  }

  fun `test recursive include`() = dtsTimeoutRunBlocking {
    val input = """
            /include/ "test0.dtsi"
            ${pathNode("/")}
        """

    addFile("test0.dtsi", "/include/ \"test1.dtsi\"")
    addFile("test1.dtsi", "/include/ \"test2.dtsi\"")
    addFile("test2.dtsi", "/include/ \"test0.dtsi\"\n/ { $target };")

    doTest(input)
  }

  fun `test target below ref node`() = dtsTimeoutRunBlocking {
    val input = """
            ${pathNode("/")}
            / { $target };
        """

    configureByText(input)
    assertNull(readAction { myFixture.getReferenceAtCaretPosition()!!.resolve() })
  }

  fun `test target included below ref node`() = dtsTimeoutRunBlocking {
    val input = """
            ${pathNode("/")}
            /include/ "test.dtsi"
        """

    addFile("test.dtsi", "/ { $target };")

    configureByText(input)
    assertNull(readAction { myFixture.getReferenceAtCaretPosition()!!.resolve() })
  }

  fun `test target below ref value`() = dtsTimeoutRunBlocking {
    val input = """
            / {
                prop = &{<caret>/target_node};
            };
            
            / { $target };
        """

    doTest(input)
  }

  fun `test target included below ref value`() = dtsTimeoutRunBlocking {
    val input = """
            / {
               prop = &{<caret>/target_node};
            };
            
            /include/ "test.dtsi"
        """

    addFile("test.dtsi", "/ { $target };")

    doTest(input)
  }

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
    configureByText(input)

    val reference = myFixture.getReferenceAtCaretPositionWithAssertion()
    assertEquals(target.trimEnd(';'), readAction { reference.resolve()?.text })
  }
}