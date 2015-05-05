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
import org.jetbrains.jps.model.java.JavaSourceRootType
import org.jetbrains.jps.model.java.JpsJavaExtensionService
import org.jetbrains.jps.model.java.JavaResourceRootType
import org.jetbrains.jps.model.module.JpsModule
import org.jetbrains.jps.util.JpsPathUtil
import org.jetbrains.osgi.jps.model.JpsOsmorcModuleExtension
import org.jetbrains.osgi.jps.model.ManifestGenerationMode
import org.jetbrains.osgi.jps.model.OsmorcJarContentEntry
import org.jetbrains.osgi.jps.model.impl.JpsOsmorcModuleExtensionImpl
import org.jetbrains.osgi.jps.model.impl.OsmorcModuleExtensionProperties

import java.util.Enumeration
import java.util.jar.JarFile

import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class OsgiBuildTest : JpsBuildTestCase() {
  var myExtension: JpsOsmorcModuleExtensionImpl by Delegates.notNull()
  var myModule: JpsModule by Delegates.notNull()

  override fun setUp() {
    super.setUp()

    myModule = addModule("main")
    val contentRoot = JpsPathUtil.pathToUrl(getAbsolutePath("main"))
    myModule.getContentRootsList().addUrl(contentRoot)
    myModule.addSourceRoot(contentRoot + "/src", JavaSourceRootType.SOURCE)
    myModule.addSourceRoot(contentRoot + "/res", JavaResourceRootType.RESOURCE)
    JpsJavaExtensionService.getInstance().getOrCreateModuleExtension(myModule).setOutputUrl(contentRoot + "/out")

    myExtension = JpsOsmorcModuleExtensionImpl(OsmorcModuleExtensionProperties())
    myExtension.getProperties().myJarFileLocation = "main.jar"
    myModule.getContainer().setChild<JpsOsmorcModuleExtension>(JpsOsmorcModuleExtension.ROLE, myExtension)
  }

  private fun bndBuild() {
    myExtension.getProperties().myManifestGenerationMode = ManifestGenerationMode.Bnd
    myExtension.getProperties().myBndFileLocation = "bnd.bnd"
  }

  private fun ideaBuild() {
    myExtension.getProperties().myManifestGenerationMode = ManifestGenerationMode.OsmorcControlled
    myExtension.getProperties().myBundleSymbolicName = "main"
    myExtension.getProperties().myBundleVersion = "1.0.0"
    myExtension.getProperties().myAdditionalProperties = mapOf("Export-Package" to "main")
  }


  fun testPlainBndProject() {
    bndBuild()
    createFile("main/bnd.bnd", "Bundle-SymbolicName: main\nBundle-Version: 1.0.0\nExport-Package: main")
    createFile("main/src/main/Main.java", "package main;\n\npublic interface Main { String greeting(); }")
    makeAll().assertSuccessful()

    assertJar(setOf("META-INF/MANIFEST.MF", "main/Main.class"))
    assertManifest(setOf("Bundle-Name=main", "Bundle-SymbolicName=main", "Bundle-Version=1.0.0", "Export-Package=main;version=\"1.0.0\""))
  }

  fun testBndtoolsProject() {
    bndBuild()
    createFile("cnf/build.bnd", "src=src\nbin=out")
    createFile("main/bnd.bnd", "Bundle-SymbolicName: main\nBundle-Version: 1.0.0\nExport-Package: main")
    createFile("main/src/main/Main.java", "package main;\n\npublic interface Main { String greeting(); }")
    makeAll().assertSuccessful()

    assertJar(setOf("META-INF/MANIFEST.MF", "main/Main.class", "OSGI-OPT/src/main/Main.java"))
    assertManifest(setOf("Bundle-Name=main", "Bundle-SymbolicName=main", "Bundle-Version=1.0.0", "Export-Package=main;version=\"1.0.0\""))
  }

  fun testPlainIdeaProject() {
    ideaBuild()
    createFile("main/src/main/Main.java", "package main;\n\npublic interface Main { String greeting(); }")
    createFile("main/res/readme.txt", "Hiya there.")
    makeAll().assertSuccessful()

    assertJar(setOf("META-INF/MANIFEST.MF", "main/Main.class", "readme.txt"))
    assertManifest(setOf("Bundle-Name=main", "Bundle-SymbolicName=main", "Bundle-Version=1.0.0", "Export-Package=main;version=\"1.0.0\""))
  }

  fun testIdeaProjectWithImpl() {
    ideaBuild()
    createFile("main/src/main/Main.java", "package main;\n\npublic interface Main { String greeting(); }")
    createFile("main/src/impl/MainImpl.java", "package impl;\n\npublic class MainImpl implements main.Main { public String greeting() {return \"\";} }")
    makeAll().assertSuccessful()

    assertJar(setOf("META-INF/MANIFEST.MF", "main/Main.class", "impl/MainImpl.class"))
    assertManifest(setOf("Bundle-Name=main", "Bundle-SymbolicName=main", "Bundle-Version=1.0.0", "Export-Package=main;version=\"1.0.0\"", "Import-Package=main"))
  }

  fun testAdditionalContent() {
    ideaBuild()
    myExtension.getProperties().myAdditionalJARContents.add(OsmorcJarContentEntry(getAbsolutePath("content/readme.txt"), "readme.txt"))
    createFile("content/readme.txt", "Hiya there.")
    createFile("main/src/main/Main.java", "package main;\n\npublic interface Main { String greeting(); }")
    makeAll().assertSuccessful()

    assertJar(setOf("META-INF/MANIFEST.MF", "main/Main.class", "readme.txt"))
    assertManifest(setOf("Bundle-Name=main", "Bundle-SymbolicName=main", "Bundle-Version=1.0.0", "Export-Package=main;version=\"1.0.0\""))
  }

  fun testRebuild() {
    bndBuild()
    createFile("main/bnd.bnd", "Bundle-SymbolicName: main\nBundle-Version: 1.0.0\nExport-Package: main")
    createFile("main/src/main/Main.java", "package main;\n\npublic interface Main { String greeting(); }")
    makeAll().assertSuccessful()
    assertJar(setOf("META-INF/MANIFEST.MF", "main/Main.class"))
    makeAll().assertUpToDate()

    changeFile("main/src/main/Main.java", "package main;\n\npublic interface Main { String greeting(); }\n")
    makeAll().assertSuccessful()
    makeAll().assertUpToDate()

    changeFile("main/bnd.bnd", "Bundle-SymbolicName: main\nBundle-Version: 1.0.1\nExport-Package: main")
    makeAll().assertSuccessful()
    makeAll().assertUpToDate()

    rebuildAll()
    makeAll().assertUpToDate()
  }


  private fun assertJar(expected: Set<String>) {
    JarFile(myExtension.getJarFileLocation()).use {
      val names = it.entries().asSequence().filter { !it.isDirectory() }.map { it.getName() }.toSet()
      assertEquals(expected, names)
    }
  }

  private fun assertManifest(expected: Set<String>) {
    val instrumental = setOf("Bnd-LastModified", "Tool", "Created-By")
    val standard = setOf("Manifest-Version", "Bundle-ManifestVersion", "Require-Capability")

    JarFile(myExtension.getJarFileLocation()).use {
      val actual = it.getManifest()!!.getMainAttributes()!!.filter {
        if (it.key.toString() in instrumental) false
        else if (it.key.toString() in standard) { assertNotNull(it.getValue()); false }
        else true
      }.map { "${it.key}=${it.value}" }.toSet()
      assertEquals(expected, actual)
    }
  }

  private fun<T> Enumeration<T>.asSequence(): Sequence<T> = object : Sequence<T> {
    override fun iterator(): Iterator<T> = this@asSequence.iterator()
  }

  private fun JarFile.use(block: (JarFile) -> Unit) {
    try { block(this) } finally { close() }
  }
}
