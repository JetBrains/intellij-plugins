// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.osgi.jps

import org.jetbrains.jps.builders.BuildResult
import org.jetbrains.jps.builders.JpsBuildTestCase
import org.jetbrains.jps.model.java.JavaResourceRootType
import org.jetbrains.jps.model.java.JavaSourceRootType
import org.jetbrains.jps.model.java.JpsJavaExtensionService
import org.jetbrains.jps.model.module.JpsModule
import org.jetbrains.osgi.jps.build.OsmorcBuilder
import org.jetbrains.osgi.jps.model.JpsOsmorcExtensionService
import org.jetbrains.osgi.jps.model.JpsOsmorcModuleExtension
import org.jetbrains.osgi.jps.model.ManifestGenerationMode
import org.jetbrains.osgi.jps.model.impl.JpsOsmorcModuleExtensionImpl
import org.jetbrains.osgi.jps.model.impl.OsmorcModuleExtensionProperties
import java.io.File
import java.util.jar.JarFile

abstract class OsgiBuildTestCase : JpsBuildTestCase() {
  private val instrumental = setOf("Bnd-LastModified", "Tool", "Created-By", "Bundle-ManifestVersion", "Require-Capability")
  private val needSorting = setOf("Export-Package", "Import-Package")

  fun module(name: String, osgi: Boolean = true): JpsModule {
    val module = addModule(name)

    val contentRoot = getUrl(name)
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

  fun BuildResult.assertBundlesCompiled(module: JpsModule, vararg bundles: String) {
    assertSuccessful()
    val bundlesDir = File("${module.name}/${extension(module).properties.myJarFileLocation}").parent
    val bundlePaths = bundles.map { "${bundlesDir}/${it}" }.toTypedArray()
    assertCompiled(OsmorcBuilder.ID, *bundlePaths)
  }

  fun assertJar(module: JpsModule, expected: Set<String>) =
    assertJar(File(extension(module).jarFileLocation), expected)

  fun assertJar(module: JpsModule, bundle: String, expected: Set<String>) =
    assertJar(File(File(extension(module).jarFileLocation).parent, bundle), expected)

  fun assertManifest(module: JpsModule, toCheck: Set<String>) =
    assertManifest(File(extension(module).jarFileLocation), toCheck)

  fun assertManifest(module: JpsModule, bundle: String, toCheck: Set<String>) =
    assertManifest(File(File(extension(module).jarFileLocation).parent, bundle), toCheck)

  private fun assertJar(file: File, expected: Set<String>) {
    val actual = JarFile(file).use { jar -> jar.entries().asSequence().filter { !it.isDirectory }.map { it.name }.toSet() }
    assertEquals(expected, actual)
  }

  private fun assertManifest(file: File, toCheck: Set<String>) {
    val expected = toCheck + "Manifest-Version=1.0"
    val actual = JarFile(file).use { jar ->
      jar.manifest!!.mainAttributes!!.asSequence()
        .map { it.key.toString() to it.value.toString() }
        .filter { it.first !in instrumental }
        .map { "${it.first}=${if (it.first in needSorting) it.second.split(',').sorted().joinToString(",") else it.second}" }
        .toSet()
    }
    assertEquals(expected, actual)
  }
}