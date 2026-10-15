package com.intellij.dts.documentation

import com.intellij.dts.DTS_TEST_ROOT_PATH
import com.intellij.dts.dtsTimeoutRunBlocking
import com.intellij.dts.core.zephyr.binding.DtsZephyrBundledBindings
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import com.networknt.schema.InputFormat
import com.networknt.schema.SchemaRegistry
import com.networknt.schema.SpecificationVersion
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.nio.file.Files
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.time.Duration.Companion.seconds

@TestApplication
@TestDataPath($$"$PROJECT_ROOT/contrib/dts/tests/testData/documentation/bundled")
class DtsBundledDocumentationTest : DtsDocumentationTest() {
  @BeforeEach
  fun awaitBundledBindings() {
    timeoutRunBlocking(30.seconds) {
      DtsZephyrBundledBindings.getInstance().awaitInit()
    }
  }

  @Test
  fun `test only ascii in bundled bindings`() {
    for (file in DTS_TEST_ROOT_PATH.resolve("resources/bindings").listDirectoryEntries()) {
      for (character in Files.readString(file)) {
        if (character.code > 0x7e) {
          fail("invalid character '$character' in binding: ${file.fileName}")
        }
      }
    }
  }

  @Test
  fun `test bundled bindings match schema`() {
    val registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_7)
    val schema = registry.getSchema(dts.getFixture("documentation/BindingSchema.json"))

    for (file in DTS_TEST_ROOT_PATH.resolve("resources/bindings").listDirectoryEntries()) {
      for (result in schema.validate(Files.readString(file), InputFormat.YAML)) {
        fail("${file.name}: ${result.message}")
      }
    }
  }

  @Test
  fun `test chosen binding`() = dtsTimeoutRunBlocking { doTest() }

  @Test
  fun `test chosen bootargs binding`() = dtsTimeoutRunBlocking { doTest() }

  @Test
  fun `test cpus binding`() = dtsTimeoutRunBlocking { doTest() }

  @Test
  fun `test cpus #size-cells binding`() = dtsTimeoutRunBlocking { doTest() }

  @Test
  fun `test cpus child binding`() = dtsTimeoutRunBlocking { doTest() }

  @Test
  fun `test cpus child clock-frequency binding`() = dtsTimeoutRunBlocking { doTest() }

  @Test
  fun `test aliases binding`() = dtsTimeoutRunBlocking { doTest() }

  @Test
  fun `test memory binding`() = dtsTimeoutRunBlocking { doTest() }

  @Test
  fun `test memory@0 binding`() = dtsTimeoutRunBlocking { doTest() }

  @Test
  fun `test reserved-memory binding`() = dtsTimeoutRunBlocking { doTest() }
}