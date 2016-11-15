/*
 * Copyright 2000-2016 JetBrains s.r.o.
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
import java.util.jar.JarFile

abstract class OsgiBuildTestCase : JpsBuildTestCase() {
  private val instrumental = setOf("Bnd-LastModified", "Tool", "Created-By", "Bundle-ManifestVersion", "Require-Capability")
  private val needSorting = setOf("Export-Package", "Import-Package")

  fun module(name: String, osgi: Boolean = true): JpsModule {
    val module = addModule(name)

    val contentRoot = JpsPathUtil.pathToUrl(getAbsolutePath(name))
    module.contentRootsList.addUrl(contentRoot)
    module.addSourceRoot("${contentRoot}/src", JavaSourceRootType.SOURCE)
    module.addSourceRoot("${contentRoot}/res", JavaResourceRootType.RESOURCE)
    JpsJavaExtensionService.getInstance().getOrCreateModuleExtension(module).outputUrl = "${contentRoot}/out"

    if (osgi) {
      val extension = JpsOsmorcModuleExtensionImpl(OsmorcModuleExtensionProperties())
      extension.properties.myJarFileLocation = "${name}.jar"
      module.container.setChild(JpsOsmorcModuleExtension.ROLE, extension)
    }

    return module
  }

  fun extension(module: JpsModule) = JpsOsmorcExtensionService.getExtension(module)!! as JpsOsmorcModuleExtensionImpl

  fun bndBuild(module: JpsModule) {
    val properties = extension(module).properties
    properties.myManifestGenerationMode = ManifestGenerationMode.Bnd
    properties.myBndFileLocation = "bnd.bnd"
  }

  fun ideaBuild(module: JpsModule) {
    val properties = extension(module).properties
    properties.myManifestGenerationMode = ManifestGenerationMode.OsmorcControlled
    properties.myBundleSymbolicName = "main"
    properties.myBundleVersion = "1.0.0"
  }

  fun BuildResult.assertBundleCompiled(module: JpsModule) {
    assertSuccessful()
    assertCompiled(OsmorcBuilder.ID, "${module.name}/${extension(module).properties.myJarFileLocation}")
  }

  fun assertJar(module: JpsModule, expected: Set<String>) {
    JarFile(extension(module).jarFileLocation).use {
      val actual = it.entries().asSequence().filter { !it.isDirectory }.map { it.name }.toSet()
      assertEquals(expected, actual)
    }
  }

  fun assertManifest(module: JpsModule, toCheck: Set<String>) {
    JarFile(extension(module).jarFileLocation).use {
      val expected = toCheck + "Manifest-Version=1.0"
      val actual = it.manifest!!.mainAttributes!!.asSequence()
          .map { it.key.toString() to it.value.toString() }
          .filter { it.first !in instrumental }
          .map { "${it.first}=${if (it.first in needSorting) it.second.split(',').sorted().joinToString(",") else it.second}" }
          .toSet()
      assertEquals(expected, actual)
    }
  }
}