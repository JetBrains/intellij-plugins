package com.intellij.dts.parsing

import com.intellij.dts.dtsFixture
import com.intellij.dts.dtsTimeoutRunBlocking
import com.intellij.dts.core.zephyr.binding.BindingFile
import com.intellij.dts.core.zephyr.binding.BindingSource
import com.intellij.dts.core.zephyr.binding.DtsZephyrBindingProvider
import com.intellij.dts.core.zephyr.binding.parseExternalBindings
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import org.junit.jupiter.api.Test
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.SafeConstructor

@TestApplication
@TestDataPath($$"$PROJECT_ROOT/contrib/dts/tests/testData/parser/zephyrBinding")
class DtsZephyrBindingParsingTest {
  companion object {
    private val projectFixture = projectFixture(openAfterCreation = true)
  }

  private val dts by dtsFixture(projectFixture)

  private val yaml = Yaml(SafeConstructor(LoaderOptions()))

  @Test
  fun `test default values`() = doTest()

  @Test
  fun `test enum values`() = doTest()

  @Test
  fun `test zephyr espressif,esp32-pinctrl`() = dtsTimeoutRunBlocking {
    doZephyrTest("espressif,esp32-pinctrl")
  }

  @Test
  fun `test zephyr espressif,esp32-ledc`() = dtsTimeoutRunBlocking {
    doZephyrTest("espressif,esp32-ledc")
  }

  private fun loadBinding(): BindingFile {
    val binding = dts.getTestFixture("yaml")

    return BindingFile(null, yaml.load(binding))
  }

  private fun doTest() {
    val source = BindingSource(mapOf(dts.testName to loadBinding()), null)
    val binding = parseExternalBindings(source).values().first()

    dts.compareWithTestFixture("txt", binding.toString())
  }

  private suspend fun doZephyrTest(compatible: String) {
    dts.addZephyr()

    val provider = DtsZephyrBindingProvider.of(dts.project)
    val binding = provider.getBindings(compatible).first()

    dts.compareWithTestFixture("txt", binding.toString())
  }
}