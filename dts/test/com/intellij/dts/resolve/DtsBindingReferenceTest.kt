package com.intellij.dts.resolve

import com.intellij.dts.DtsTestBase
import com.intellij.dts.lang.symbols.DtsBindingSymbol

class DtsBindingReferenceTest : DtsTestBase() {
  fun `test one compatible string`() = doTest(
    compatible = listOf("espressif,esp32-spi<caret>"),
    bindingPath = "WORKING_DIRECTORY/zephyr/dts/bindings/spi/espressif,esp32-spi.yaml",
  )

  fun `test two compatible string`() = doTest(
    compatible = listOf("espressif,esp32-spi", "nordic,nrf-spi<caret>"),
    bindingPath = "WORKING_DIRECTORY/zephyr/dts/bindings/spi/nordic,nrf-spi.yaml",
  )

  fun doTest(
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
    val target = reference.resolveReference().filterIsInstance<DtsBindingSymbol>().first()

    assertEquals(bindingPath, makeRelativeToWorkingDirectory(target.binding.path!!))
  }
}