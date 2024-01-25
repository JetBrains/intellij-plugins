package com.intellij.dts.documentation

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.intellij.dts.DTS_TEST_ROOT_PATH
import com.intellij.dts.zephyr.binding.DtsZephyrBundledBindings
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import kotlinx.coroutines.runBlocking
import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.readText

class DtsBundledDocumentationTest : DtsDocumentationTest() {
  override fun getBasePath(): String = "documentation/bundled"

  override fun setUp() {
    super.setUp()

    runBlocking {
      DtsZephyrBundledBindings.getInstance().awaitInit()
    }
  }

  fun `test only ascii in bundled bindings`() {
    for (file in Path.of(DTS_TEST_ROOT_PATH, "resources/bindings").listDirectoryEntries()) {
      for (character in file.readText()) {
        if (character.code > 0x7e) {
          fail("invalid character '$character' in binding: ${file.fileName}")
        }
      }
    }
  }

  fun `test bundled bindings match schema`() {
    val factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7)
    val mapper = ObjectMapper(YAMLFactory())
    val yamlFactory = JsonSchemaFactory.builder(factory).objectMapper(mapper).build()
    val schema = yamlFactory.getSchema(getFixture("documentation/BindingSchema.json"))

    for (file in Path.of(DTS_TEST_ROOT_PATH, "resources/bindings").listDirectoryEntries()) {
      for (result in schema.validate(mapper.readTree(file.readText()))) {
        fail("${file.name}: ${result.message}")
      }
    }
  }

  fun `test chosen binding`() = doTest()

  fun `test chosen bootargs binding`() = doTest()

  fun `test cpus binding`() = doTest()

  fun `test cpus #size-cells binding`() = doTest()

  fun `test cpus child binding`() = doTest()

  fun `test cpus child clock-frequency binding`() = doTest()

  fun `test aliases binding`() = doTest()

  fun `test memory binding`() = doTest()

  fun `test memory@0 binding`() = doTest()

  fun `test reserved-memory binding`() = doTest()
}