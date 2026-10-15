package com.intellij.dts.ide

import com.intellij.dts.dtsFixture
import com.intellij.dts.dtsTimeoutRunBlocking
import com.intellij.dts.core.lang.psi.DtsNode
import com.intellij.dts.core.lang.psi.getDtsPath
import com.intellij.openapi.application.readAction
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.util.startOffset
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@TestApplication
class DtsSearchTest {
  companion object {
    private val projectFixture = projectFixture(openAfterCreation = true)
  }

  private val dts by dtsFixture(projectFixture, extension = "dts")

  private val myFixture get() = dts.codeInsight

  private fun refNode(path: String) = "&{$path<caret>} {};"

  private fun refProp(path: String) = "prop = &{$path<caret>};"

  private fun include() = "/include/ \"include.dtsi\""

  @Test
  fun `test find root node`() = dtsTimeoutRunBlocking {
    doTest(
      """
              / {};  
            
              ${refNode("/")}
          """,
      expected = listOf("0:/"),
    )
  }

  @Test
  fun `test find root nodes`() = dtsTimeoutRunBlocking {
    doTest(
      """
              / {};  
              / {};  
              / {};  
            
              ${refNode("/")}
          """,
      expected = listOf("16:/", "8:/", "0:/"),
    )
  }

  @Test
  fun `test does not find root node`() = dtsTimeoutRunBlocking {
    doTest(
      """
              ${refNode("/")} 
              / {};
          """,
    )
  }

  @Test
  fun `test find included root node`() = dtsTimeoutRunBlocking {
    doTest(
      """
              ${include()}  
              ${refNode("/")}  
          """,
      include = "/ {};",
      expected = listOf("0:/"),
    )
  }

  @Test
  fun `test does not find included root node`() = dtsTimeoutRunBlocking {
    doTest(
      """
              ${refNode("/")} 
              ${include()}   
          """,
      include = "/ {};",
    )
  }

  @Test
  fun `test find parent node from prop`() = dtsTimeoutRunBlocking {
    doTest(
      """
              / {
                  ${refProp("/")}
              }; 
          """,
      expected = listOf("0:/"),
    )
  }

  @Test
  fun `test find sibling node from prop`() = dtsTimeoutRunBlocking {
    doTest(
      """
              / {
                  ${refProp("/sibling")}
                  sibling {};
              }; 
          """,
      expected = listOf("32:/sibling"),
    )
  }

  @Test
  fun `test find nested node`() = dtsTimeoutRunBlocking {
    doTest(
      """
              / { nested {}; };
              ${refNode("/nested")}
          """,
      expected = listOf("4:/nested"),
    )
  }

  @Test
  fun `test does not find nested node`() = dtsTimeoutRunBlocking {
    doTest(
      """
              ${refNode("/nested")}
              / { nested {}; };
          """,
    )
  }

  @Test
  fun `test find included nested node`() = dtsTimeoutRunBlocking {
    doTest(
      """
              ${include()}
              ${refNode("/nested")}
          """,
      include = "/ { nested {}; };",
      expected = listOf("4:/nested"),
    )
  }

  @Test
  fun `test find first nested node`() = dtsTimeoutRunBlocking {
    doTest(
      """
              / {
                  nested {};
                  nested {};
              };
              ${refNode("/nested")}
          """,
      expected = listOf("23:/nested", "8:/nested"),
    )
  }

  @Test
  fun `test find deeply nested node`() = dtsTimeoutRunBlocking {
    doTest(
      """
              / {
                n1 { n2 { n3 { n4 { node {}; }; }; }; };
              };
              ${refNode("/n1/n2/n3/n4/node")}
          """,
      expected = listOf("26:/n1/n2/n3/n4/node"),
    )
  }

  @Test
  fun `test find nested node in ref`() = dtsTimeoutRunBlocking {
    doTest(
      """
              / {};
              &{/} { nested {}; };
              ${refNode("/nested")}
          """,
      expected = listOf("13:/nested"),
    )
  }

  @Test
  fun `test find nested nodes in ref`() = dtsTimeoutRunBlocking {
    doTest(
      """
              / {};
              &{/} { nested {}; };
              &{/} { nested {}; };
              &{/} { nested {}; };
              ${refNode("/nested")}
          """,
      expected = listOf("55:/nested", "34:/nested", "13:/nested"),
    )
  }

  // Fails because the reference in the included file cannot be resolved. In
  // order to resolve the reference in the included file some kind of context
  // would be required. Which allows the included file to access statements
  // from the including file.
  fun `failing test find included nested node in ref`() = dtsTimeoutRunBlocking {
    doTest(
      """
              / {};
              ${include()} 
              ${refNode("/nested")}
          """,
      include = "&{/} { nested {}; };",
      expected = listOf("13:/nested"),
    )
  }

  @Test
  fun `test find nested node in label ref`() = dtsTimeoutRunBlocking {
    doTest(
      """
              / {
                  l: label {};
              };
              &l { nested {}; };
              ${refNode("/label/nested")}
          """,
      expected = listOf("29:/label/nested"),
    )
  }

  @Test
  fun `test find nested nodes in label ref`() = dtsTimeoutRunBlocking {
    doTest(
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
  }

  private suspend fun doTest(
    text: String,
    include: String? = null,
    expected: List<String> = emptyList(),
  ) {
    dts.configureByText(text.trimIndent())

    if (include != null) {
      dts.addFile("include.dtsi", include)
    }

    val reference = myFixture.getReferenceAtCaretPosition() as PsiPolyVariantReference
    val results = readAction { reference.multiResolve(false) }

    assertThat(results).hasSize(expected.size)

    for ((item, result) in expected.zip(results)) {
      val (offset, path) = item.split(':')
      val element = result.element as DtsNode

      assertEquals(offset.toInt(), readAction { element.startOffset })
      assertEquals(path, readAction { element.getDtsPath().toString() })
    }
  }
}