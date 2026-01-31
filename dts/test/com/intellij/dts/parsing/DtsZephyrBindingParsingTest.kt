package com.intellij.dts.parsing

import com.intellij.dts.DtsTestBase
import com.intellij.dts.zephyr.binding.BindingFile
import com.intellij.dts.zephyr.binding.BindingSource
import com.intellij.dts.zephyr.binding.DtsZephyrBindingProvider
import com.intellij.dts.zephyr.binding.parseExternalBindings
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.SafeConstructor

class DtsZephyrBindingParsingTest : DtsTestBase() {
  private val yaml = Yaml(SafeConstructor(LoaderOptions()))

  override fun getBasePath(): String = "parser/zephyrBinding"

  fun `test default values`() = doTest()

  fun `test enum values`() = doTest()

  fun `test zephyr espressif,esp32-pinctrl`() = doZephyrTest("espressif,esp32-pinctrl")

  fun `test zephyr espressif,esp32-ledc`() = doZephyrTest("espressif,esp32-ledc")

  private fun loadBinding(): BindingFile {
    val binding = getTestFixture("yaml")

    return BindingFile(null, yaml.load(binding))
  }

  private fun doTest() {
    val source = BindingSource(mapOf(testName to loadBinding()), null)
    val binding = parseExternalBindings(source).values().first()

    compareWithTestFixture("txt", binding.toString())
  }

  private fun doZephyrTest(compatible: String) {
    addZephyr()

    val provider = DtsZephyrBindingProvider.of(project)
    val binding = provider.getBindings(compatible).first()

    compareWithTestFixture("txt", binding.toString())
  }
}