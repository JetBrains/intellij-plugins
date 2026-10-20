package com.intellij.dts.resolve

import com.intellij.dts.core.lang.symbols.DtsBindingSymbol
import com.intellij.dts.dtsFixture
import com.intellij.dts.dtsProjectFixture
import com.intellij.dts.dtsTimeoutRunBlocking
import com.intellij.openapi.application.readAction
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.tempPathFixture
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.invariantSeparatorsPathString

@TestApplication
class DtsBindingReferenceTest {
  private val tempPathFixture = tempPathFixture()
  private val projectFixture = dtsProjectFixture(tempPathFixture, withZephyr = true, openAfterCreation = true)

  private val dts by dtsFixture(projectFixture)

  private val myFixture get() = dts.codeInsight

  @Test
  fun `test one compatible string`() = dtsTimeoutRunBlocking {
    doTest(
      compatible = listOf("espressif,esp32-spi<caret>"),
      bindingPath = Path("WORKING_DIRECTORY/zephyr/dts/bindings/spi/espressif,esp32-spi.yaml"),
    )
  }

  @Test
  fun `test two compatible string`() = dtsTimeoutRunBlocking {
    doTest(
      compatible = listOf("espressif,esp32-spi", "nordic,nrf-spi<caret>"),
      bindingPath = Path("WORKING_DIRECTORY/zephyr/dts/bindings/spi/nordic,nrf-spi.yaml"),
    )
  }

  private suspend fun doTest(
    compatible: List<String>,
    bindingPath: Path,
  ) {
    val compatibleValue = compatible.joinToString { "\"$it\"" }

    dts.configureByText("""
      / {
        node {
          compatible = $compatibleValue; 
        };
      };
    """)

    val reference = myFixture.findSingleReferenceAtCaret()
    val target = readAction { reference.resolveReference().filterIsInstance<DtsBindingSymbol>().first() }

    assertEquals(bindingPath.invariantSeparatorsPathString,
                 dts.makeRelativeToWorkingDirectory(target.binding.path!!))
  }
}