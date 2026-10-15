package com.intellij.dts.resolve

import com.intellij.dts.DtsTestBase
import com.intellij.dts.lang.symbols.DtsBindingSymbol
import com.intellij.openapi.application.readAction

class DtsBindingReferenceTest : DtsTestBase() {
  fun `test one compatible string`() = dtsTimeoutRunBlocking {
    doTest(
      compatible = listOf("espressif,esp32-spi<caret>"),
      bindingPath = "WORKING_DIRECTORY/zephyr/dts/bindings/spi/espressif,esp32-spi.yaml",
    )
  }

  fun `test two compatible string`() = dtsTimeoutRunBlocking {
    doTest(
      compatible = listOf("espressif,esp32-spi", "nordic,nrf-spi<caret>"),
      bindingPath = "WORKING_DIRECTORY/zephyr/dts/bindings/spi/nordic,nrf-spi.yaml",
    )
  }

  private suspend fun doTest(
    compatible: List<String>,
    bindingPath: String,
  ) {
    addZephyr()

    val compatibleValue = compatible.joinToString { "\"$it\"" }

    configureByText("""
      / {
        node {
          compatible = $compatibleValue; 
        };
      };
    """)

    val reference = myFixture.findSingleReferenceAtCaret()
    val target = readAction { reference.resolveReference().filterIsInstance<DtsBindingSymbol>().first() }

    assertEquals(bindingPath, makeRelativeToWorkingDirectory(target.binding.path!!))
  }
}