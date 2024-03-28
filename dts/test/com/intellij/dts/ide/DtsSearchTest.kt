package com.intellij.dts.ide

import com.intellij.dts.DtsTestBase
import com.intellij.dts.lang.psi.DtsNode
import com.intellij.dts.lang.psi.getDtsPath
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.util.startOffset

class DtsSearchTest : DtsTestBase() {
  override fun getTestFileExtension(): String = "dts"

  private fun refNode(path: String) = "&{$path<caret>} {};"

  private fun refProp(path: String) = "prop = &{$path<caret>};"

  private fun include() = "/include/ \"include.dtsi\""

  fun `test find root node`() = doTest(
    """
            / {};  
          
            ${refNode("/")}
        """,
    expected = listOf("0:/"),
  )

  fun `test find root nodes`() = doTest(
    """
            / {};  
            / {};  
            / {};  
          
            ${refNode("/")}
        """,
    expected = listOf("16:/", "8:/", "0:/"),
  )

  fun `test does not find root node`() = doTest(
    """
            ${refNode("/")} 
            / {};
        """,
  )

  fun `test find included root node`() = doTest(
    """
            ${include()}  
            ${refNode("/")}  
        """,
    include = "/ {};",
    expected = listOf("0:/"),
  )

  fun `test does not find included root node`() = doTest(
    """
            ${refNode("/")} 
            ${include()}   
        """,
    include = "/ {};",
  )

  fun `test find parent node from prop`() = doTest(
    """
            / {
                ${refProp("/")}
            }; 
        """,
    expected = listOf("0:/"),
  )

  fun `test find sibling node from prop`() = doTest(
    """
            / {
                ${refProp("/sibling")}
                sibling {};
            }; 
        """,
    expected = listOf("32:/sibling"),
  )

  fun `test find nested node`() = doTest(
    """
            / { nested {}; };
            ${refNode("/nested")}
        """,
    expected = listOf("4:/nested"),
  )

  fun `test does not find nested node`() = doTest(
    """
            ${refNode("/nested")}
            / { nested {}; };
        """,
  )

  fun `test find included nested node`() = doTest(
    """
            ${include()}
            ${refNode("/nested")}
        """,
    include = "/ { nested {}; };",
    expected = listOf("4:/nested"),
  )

  fun `test find first nested node`() = doTest(
    """
            / {
                nested {};
                nested {};
            };
            ${refNode("/nested")}
        """,
    expected = listOf("23:/nested", "8:/nested"),
  )

  fun `test find deeply nested node`() = doTest(
    """
            / {
              n1 { n2 { n3 { n4 { node {}; }; }; }; };
            };
            ${refNode("/n1/n2/n3/n4/node")}
        """,
    expected = listOf("26:/n1/n2/n3/n4/node"),
  )

  fun `test find nested node in ref`() = doTest(
    """
            / {};
            &{/} { nested {}; };
            ${refNode("/nested")}
        """,
    expected = listOf("13:/nested"),
  )

  fun `test find nested nodes in ref`() = doTest(
    """
            / {};
            &{/} { nested {}; };
            &{/} { nested {}; };
            &{/} { nested {}; };
            ${refNode("/nested")}
        """,
    expected = listOf("55:/nested", "34:/nested", "13:/nested"),
  )

  // Fails because the reference in the included file cannot be resolved. In
  // order to resolve the reference in the included file some kind of context
  // would be required. Which allows the included file to access statements
  // from the including file.
  fun `failing test find included nested node in ref`() = doTest(
    """
            / {};
            ${include()} 
            ${refNode("/nested")}
        """,
    include = "&{/} { nested {}; };",
    expected = listOf("13:/nested"),
  )

  fun `test find nested node in label ref`() = doTest(
    """
            / {
                l: label {};
            };
            &l { nested {}; };
            ${refNode("/label/nested")}
        """,
    expected = listOf("29:/label/nested"),
  )

  fun `test find nested nodes in label ref`() = doTest(
    """
            / {
                l: label {};
            };
            &l { nested {}; };
            &l { nested {}; };
            ${refNode("/label/nested")}
        """,
    expected = listOf("48:/label/nested", "29:/label/nested"),
  )

  private fun doTest(
    text: String,
    include: String? = null,
    expected: List<String> = emptyList(),
  ) {
    configureByText(text.trimIndent())

    if (include != null) {
      addFile("include.dtsi", include)
    }

    val reference = myFixture.getReferenceAtCaretPosition() as PsiPolyVariantReference
    val results = reference.multiResolve(false)

    assertSize(expected.size, results)

    for ((item, result) in expected.zip(results)) {
      val (offset, path) = item.split(':')
      val element = result.element as DtsNode

      assertEquals(offset.toInt(), element.startOffset)
      assertEquals(path, element.getDtsPath().toString())
    }
  }
}