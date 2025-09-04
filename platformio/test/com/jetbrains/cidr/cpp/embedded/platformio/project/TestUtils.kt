package com.jetbrains.cidr.cpp.embedded.platformio.project

import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.ProjectKeys
import com.intellij.openapi.externalSystem.model.project.ProjectData
import com.jetbrains.cidr.external.system.model.ExternalModule
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull

internal object TestUtils {
  fun DataNode<ProjectData>.findExternalModule(): DataNode<ExternalModule> {
    assertEquals("Project is expected in ProjectData", ProjectKeys.PROJECT, key)
    assertEquals("Only one module is expected", 1, children.size)
    val moduleNode = children.first()
    assertEquals("Module is expected in project", ProjectKeys.MODULE, moduleNode.key)
    val externalModule = moduleNode.children
      .firstOrNull { it.data is ExternalModule }
    assertNotNull("ExternalModule not found in module", externalModule)
    @Suppress("UNCHECKED_CAST")
    return externalModule!! as DataNode<ExternalModule>
  }
}