package com.intellij.dts.parsing

import com.intellij.dts.DtsTestBase
import com.intellij.dts.zephyr.binding.DtsZephyrBindingParser
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.SafeConstructor

class DtsZephyrBindingParsingTest : DtsTestBase() {
  private val yaml = Yaml(SafeConstructor(LoaderOptions()))

  override fun getBasePath(): String = "parser/zephyrBinding"

  fun `test default values`() = doTest()

  private fun loadBinding(): DtsZephyrBindingParser.Source {
    val binding = getTestFixture("yaml")

    return DtsZephyrBindingParser.Source("", yaml.load(binding))
  }

  private fun doTest() {
    val sources = mapOf(testName to loadBinding())
    val parser = DtsZephyrBindingParser(sources, null)
    val binding = parser.parse(testName)

    compareWithTestFixture("txt", binding.toString())
  }
}