package org.jetbrains.qodana.yaml

import com.fasterxml.jackson.databind.ObjectMapper
import org.jetbrains.qodana.staticAnalysis.inspections.config.QodanaYamlReader
import org.junit.Assert.assertEquals
import org.junit.Test

class QodanaYamlSchemaTest {
  @Test
  fun `root properties should be exactly as described in schema`() {
    val schemaRoots = ObjectMapper()
      .readTree(javaClass.getResource(QODANA_YAML_SCHEMA_RESOURCE))
      .get("properties")
      .fieldNames()
      .asSequence()
      .sorted()
      .toList()
    val rootProps = QodanaYamlReader.rootProps.sorted()

    assertEquals(schemaRoots, rootProps)
  }
}
