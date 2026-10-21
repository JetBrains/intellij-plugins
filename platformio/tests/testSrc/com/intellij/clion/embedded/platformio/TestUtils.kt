package com.intellij.clion.embedded.platformio

import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.ProjectKeys
import com.intellij.openapi.externalSystem.model.project.ProjectData
import com.jetbrains.cidr.cpp.external.system.model.ExternalModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull

internal object TestUtils {
  fun DataNode<ProjectData>.findExternalModule(): DataNode<ExternalModule> {
    assertEquals(ProjectKeys.PROJECT, key, "Project is expected in ProjectData")
    assertEquals(1, children.size, "Only one module is expected")
    val moduleNode = children.first()
    assertEquals(ProjectKeys.MODULE, moduleNode.key, "Module is expected in project")
    val externalModule = moduleNode.children
      .firstOrNull { it.data is ExternalModule }
    assertNotNull(externalModule, "ExternalModule not found in module")
    @Suppress("UNCHECKED_CAST")
    return externalModule!! as DataNode<ExternalModule>
  }
}