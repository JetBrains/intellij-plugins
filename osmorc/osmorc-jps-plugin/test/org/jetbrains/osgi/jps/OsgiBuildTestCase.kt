/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.osgi.jps

import org.jetbrains.jps.builders.BuildResult
import org.jetbrains.jps.builders.JpsBuildTestCase
import org.jetbrains.jps.model.java.JavaResourceRootType
import org.jetbrains.jps.model.java.JavaSourceRootType
import org.jetbrains.jps.model.java.JpsJavaExtensionService
import org.jetbrains.jps.model.module.JpsModule
import org.jetbrains.jps.util.JpsPathUtil
import org.jetbrains.osgi.jps.build.OsmorcBuilder
import org.jetbrains.osgi.jps.model.JpsOsmorcExtensionService
import org.jetbrains.osgi.jps.model.JpsOsmorcModuleExtension
import org.jetbrains.osgi.jps.model.ManifestGenerationMode
import org.jetbrains.osgi.jps.model.impl.JpsOsmorcModuleExtensionImpl
import org.jetbrains.osgi.jps.model.impl.OsmorcModuleExtensionProperties
import java.util.*
import java.util.jar.JarFile

abstract class OsgiBuildTestCase : JpsBuildTestCase() {
  fun module(name: String, osgi: Boolean = true): JpsModule {
    val module = addModule(name)

    val contentRoot = JpsPathUtil.pathToUrl(getAbsolutePath(name))
    module.getContentRootsList().addUrl(contentRoot)
    module.addSourceRoot("${contentRoot}/src", JavaSourceRootType.SOURCE)
    module.addSourceRoot("${contentRoot}/res", JavaResourceRootType.RESOURCE)
    JpsJavaExtensionService.getInstance().getOrCreateModuleExtension(module).setOutputUrl("${contentRoot}/out")

    if (osgi) {
      val extension = JpsOsmorcModuleExtensionImpl(OsmorcModuleExtensionProperties())
      extension.getProperties().myJarFileLocation = "${name}.jar"
      module.getContainer().setChild(JpsOsmorcModuleExtension.ROLE, extension)
    }

    return module
  }

  fun extension(module: JpsModule) = JpsOsmorcExtensionService.getExtension(module)!! as JpsOsmorcModuleExtensionImpl

  fun bndBuild(module: JpsModule) {
    val properties = extension(module).getProperties()
    properties.myManifestGenerationMode = ManifestGenerationMode.Bnd
    properties.myBndFileLocation = "bnd.bnd"
  }

  fun ideaBuild(module: JpsModule) {
    val properties = extension(module).getProperties()
    properties.myManifestGenerationMode = ManifestGenerationMode.OsmorcControlled
    properties.myBundleSymbolicName = "main"
    properties.myBundleVersion = "1.0.0"
    properties.myAdditionalProperties = mapOf("Export-Package" to "main")
  }

  fun BuildResult.assertBundleCompiled(module: JpsModule) {
    assertSuccessful()
    assertCompiled(OsmorcBuilder.ID, "${module.getName()}/${extension(module).getProperties().myJarFileLocation}")
  }

  fun assertJar(module: JpsModule, expected: Set<String>) {
    val actual: MutableSet<String> = HashSet()

    JarFile(extension(module).getJarFileLocation()).use {
      val entries = it.entries()
      while (entries.hasMoreElements()) {
        val entry = entries.nextElement()
        if (!entry.isDirectory()) {
          actual.add(entry.getName())
        }
      }
    }

    assertEquals(expected, actual)
  }

  fun assertManifest(module: JpsModule, expected: Set<String>) {
    val instrumental = setOf("Bnd-LastModified", "Tool", "Created-By")
    val required = setOf("Manifest-Version", "Bundle-ManifestVersion", "Require-Capability")

    JarFile(extension(module).getJarFileLocation()).use {
      val actual = it.getManifest()!!.getMainAttributes()!!.filter {
        if (it.key.toString() in instrumental) false
        else if (it.key.toString() in required) {
          assertNotNull(it.value); false }
        else true
      }.map { "${it.key}=${it.value}" }.toSet()
      assertEquals(expected, actual)
    }
  }

  private fun JarFile.use(block: (JarFile) -> Unit) {
    try { block(this) } finally { close() }
  }
}