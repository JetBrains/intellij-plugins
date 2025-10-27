package org.jetbrains.idea.perforce

import com.intellij.openapi.application.PluginPathManager
import junit.framework.AssertionFailedError
import java.io.File

object P4TestUtil {
  @Suppress("IO_FILE_USAGE")
  @JvmStatic
  fun getResource(resourceName: String): File {
    val prefixedResourceName = resourceName.takeIf { it.startsWith("/") } ?: "/$resourceName"
    val resource = javaClass.getResource(prefixedResourceName)
    if (resource == null) {
      throw AssertionFailedError("$resourceName doesn't exist!")
    }

    return when (resource.protocol) {
      "file" -> File(resource.file)
      "jar" -> {
        PluginPathManager.getPluginHome("PerforceIntegration")
          .toPath().resolve("testResources$prefixedResourceName").toFile()
      }
      else -> throw AssertionFailedError("Unsupported protocol: " + resource.protocol)
    }
  }
}
