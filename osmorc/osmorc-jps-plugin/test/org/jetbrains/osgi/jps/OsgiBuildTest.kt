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

import org.jetbrains.jps.builders.JpsBuildTestCase
import org.jetbrains.jps.model.java.JavaSourceRootProperties
import org.jetbrains.jps.model.java.JavaSourceRootType
import org.jetbrains.jps.model.module.JpsModule
import org.jetbrains.jps.util.JpsPathUtil
import org.jetbrains.osgi.jps.model.JpsOsmorcModuleExtension
import org.jetbrains.osgi.jps.model.ManifestGenerationMode
import org.jetbrains.osgi.jps.model.impl.JpsOsmorcModuleExtensionImpl
import org.jetbrains.osgi.jps.model.impl.OsmorcModuleExtensionProperties

import java.util.Enumeration
import java.util.jar.JarFile

import kotlin.properties.Delegates
import kotlin.test.assertEquals
import java.util.zip.ZipFile

class OsgiBuildTest : JpsBuildTestCase() {
  var myExtension: JpsOsmorcModuleExtension by Delegates.notNull()
  var myModule: JpsModule by Delegates.notNull()

  override fun setUp() {
    super.setUp()

    myModule = addModule("main")

    val properties = OsmorcModuleExtensionProperties()
    properties.myManifestGenerationMode = ManifestGenerationMode.Bnd
    properties.myBndFileLocation = "bnd.bnd"
    properties.myJarFileLocation = "main.jar"
    myExtension = JpsOsmorcModuleExtensionImpl(properties)
    myModule.getContainer().setChild<JpsOsmorcModuleExtension>(JpsOsmorcModuleExtension.ROLE, myExtension)
  }

  fun testBasics() {
    myModule.getContentRootsList().addUrl(JpsPathUtil.pathToUrl(getAbsolutePath("")))
    myModule.addSourceRoot<JavaSourceRootProperties>(JpsPathUtil.pathToUrl(getAbsolutePath("src")), JavaSourceRootType.SOURCE)
    createFile("bnd.bnd", "Bundle-SymbolicName: main\nBundle-Version: 1.0.0\nExport-Package: main")
    createFile("src/main/Main.java", "package main;\n\npublic interface Main { String greeting(); }")
    makeAll().assertSuccessful()

    assertJar(setOf("META-INF/MANIFEST.MF", "main/Main.class"))
    assertManifest(mapOf("Bundle-SymbolicName" to "main", "Bundle-Version" to "1.0.0", "Export-Package" to "main;version=\"1.0.0\""))
  }

  private fun assertJar(expected: Set<String>) {
    JarFile(myExtension.getJarFileLocation()).use {
      val names = it.entries().stream().filter { !it.isDirectory() }.map { it.getName() }.toSet()
      assertEquals(expected, names)
    }
  }

  private fun assertManifest(expected: Map<String, String>) {
    JarFile(myExtension.getJarFileLocation()).use {
      val attributes = it.getManifest()!!.getMainAttributes()!!
      for ((k, v) in expected) {
        assertEquals(v, attributes.getValue(k))
      }
    }
  }

  private fun <T> Enumeration<T>.stream(): Stream<T> {
    return stream { if (hasMoreElements()) nextElement() else null }
  }

  private fun JarFile.use(block: (JarFile) -> Unit) {
    try { block(this) } finally { close() }
  }
}
