package com.intellij.dts.resolve

import com.intellij.dts.DtsTestBase

class DtsPathReferenceTest : DtsTestBase() {
  private val target = "target_node {};"

  fun `test root`() {
    val input = """
            / {};
            &{<caret>/} {};
        """

    configureByText(input)

    val reference = myFixture.getReferenceAtCaretPositionWithAssertion()
    assertEquals("/ {}", reference.resolve()?.text)
  }

  fun `test local`() {
    val input = """
            / { $target };
            ${pathNode("/")}
        """

    doTest(input)
  }

  fun `test local nested`() {
    val input = """
            / { 
                subNode { $target };
            };
            
            ${pathNode("/subNode/")}
        """

    doTest(input)
  }

  fun `test include`() {
    val input = """
            /include/ "test.dtsi"
            ${pathNode("/")}
        """

    addFile("test.dtsi", "/ { $target };")

    doTest(input)
  }

  fun `test nested include`() {
    val input = """
            /include/ "test0.dtsi"
            ${pathNode("/")}
        """

    addFile("test0.dtsi", "/include/ \"test1.dtsi\"")
    addFile("test1.dtsi", "/ { $target };")

    doTest(input)
  }

  fun `test recursive include`() {
    val input = """
            /include/ "test0.dtsi"
            ${pathNode("/")}
        """

    addFile("test0.dtsi", "/include/ \"test1.dtsi\"")
    addFile("test1.dtsi", "/include/ \"test2.dtsi\"")
    addFile("test2.dtsi", "/include/ \"test0.dtsi\"\n/ { $target };")

    doTest(input)
  }

  fun `test target below ref node`() {
    val input = """
            ${pathNode("/")}
            / { $target };
        """

    configureByText(input)
    assertNull(myFixture.getReferenceAtCaretPosition()!!.resolve())
  }

  fun `test target included below ref node`() {
    val input = """
            ${pathNode("/")}
            /include/ "test.dtsi"
        """

    addFile("test.dtsi", "/ { $target };")

    configureByText(input)
    assertNull(myFixture.getReferenceAtCaretPosition()!!.resolve())
  }

  fun `test target below ref value`() {
    val input = """
            / {
                prop = &{<caret>/target_node};
            };
            
            / { $target };
        """

    doTest(input)
  }

  fun `test target included below ref value`() {
    val input = """
            / {
               prop = &{<caret>/target_node};
            };
            
            /include/ "test.dtsi"
        """

    addFile("test.dtsi", "/ { $target };")

    doTest(input)
  }

  fun `test target in ref node`() {
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

  private fun doTest(input: String) {
    configureByText(input)

    val reference = myFixture.getReferenceAtCaretPositionWithAssertion()
    assertEquals(target.trimEnd(';'), reference.resolve()?.text)
  }
}