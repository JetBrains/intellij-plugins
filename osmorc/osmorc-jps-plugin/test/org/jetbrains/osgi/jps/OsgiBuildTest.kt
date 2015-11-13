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

import org.jetbrains.jps.model.module.JpsModule
import org.jetbrains.osgi.jps.model.ManifestGenerationMode
import org.jetbrains.osgi.jps.model.OsmorcJarContentEntry

class OsgiBuildTest : OsgiBuildTestCase() {
  private lateinit var myModule: JpsModule

  override fun setUp() {
    super.setUp()
    myModule = module("main")
  }

  fun testBndProjectSimple() {
    bndBuild(myModule)
    createFile("main/bnd.bnd", "Bundle-SymbolicName: main\nBundle-Version: 1.0.0\nExport-Package: main")
    createFile("main/src/main/Main.java", "package main;\n\npublic interface Main { String greeting(); }")
    createFile("main/res/skipped.txt", "(empty)")
    makeAll().assertSuccessful()

    assertJar(myModule, setOf("META-INF/MANIFEST.MF", "main/Main.class"))
    assertManifest(myModule, setOf("Bundle-Name=main", "Bundle-SymbolicName=main", "Bundle-Version=1.0.0", "Export-Package=main;version=\"1.0.0\""))
  }

  fun testBndProjectWithoutPackages() {
    bndBuild(myModule)
    createFile("main/bnd.bnd", "Bundle-SymbolicName: main\nBundle-Version: 1.0.0")
    createFile("main/src/main/Main.java", "package main;\n\npublic interface Main { String greeting(); }")
    createFile("main/res/skipped.txt", "(empty)")
    makeAll().assertSuccessful()

    assertJar(myModule, setOf("META-INF/MANIFEST.MF"))
    assertManifest(myModule, setOf("Bundle-Name=main", "Bundle-SymbolicName=main", "Bundle-Version=1.0.0"))
  }

  fun testBndtoolsProject() {
    bndBuild(myModule)
    createFile("cnf/build.bnd", "src=src\nbin=out")
    createFile("main/bnd.bnd", "Bundle-SymbolicName: main\nBundle-Version: 1.0.0\nExport-Package: main")
    createFile("main/src/main/Main.java", "package main;\n\npublic interface Main { String greeting(); }")
    makeAll().assertSuccessful()

    assertJar(myModule, setOf("META-INF/MANIFEST.MF", "main/Main.class", "OSGI-OPT/src/main/Main.java"))
    assertManifest(myModule, setOf("Bundle-Name=main", "Bundle-SymbolicName=main", "Bundle-Version=1.0.0", "Export-Package=main;version=\"1.0.0\""))
  }

  fun testIdeaProjectSimple() {
    ideaBuild(myModule)
    extension(myModule).properties.myAdditionalProperties = mapOf("Export-Package" to "main")
    createFile("main/src/main/Main.java", "package main;\n\npublic class Main { public void main() { util.Util.util(); } }")
    createFile("main/src/util/Util.java", "package util;\n\npublic class Util { public static void util() { } }")
    createFile("main/res/skipped.txt", "(empty)")
    makeAll().assertSuccessful()

    assertJar(myModule, setOf("META-INF/MANIFEST.MF", "main/Main.class", "util/Util.class"))
    assertManifest(myModule, setOf("Bundle-Name=main", "Bundle-SymbolicName=main", "Bundle-Version=1.0.0", "Export-Package=main;version=\"1.0.0\""))
  }

  fun testIdeaProjectNotSpecified() {
    ideaBuild(myModule)
    createFile("main/src/main/Main.java", "package main;\n\npublic class Main { public void main() { util.Util.util(); } }")
    createFile("main/src/util/Util.java", "package util;\n\npublic class Util { public static void util() { } }")
    createFile("main/res/skipped.txt", "(empty)")
    makeAll().assertSuccessful()

    assertJar(myModule, setOf("META-INF/MANIFEST.MF", "main/Main.class", "util/Util.class"))
    assertManifest(myModule, setOf("Bundle-Name=main", "Bundle-SymbolicName=main", "Bundle-Version=1.0.0",
            "Export-Package=main;version=\"1.0.0\",util;version=\"1.0.0\"", "Import-Package=util"))
  }

  fun testAdditionalContent() {
    ideaBuild(myModule)
    extension(myModule).properties.myAdditionalJARContents.add(OsmorcJarContentEntry(getAbsolutePath("content/readme.txt"), "readme.txt"))
    createFile("content/readme.txt", "Hiya there.")
    createFile("main/src/main/Main.java", "package main;\n\npublic interface Main { String greeting(); }")
    makeAll().assertSuccessful()

    assertJar(myModule, setOf("META-INF/MANIFEST.MF", "main/Main.class", "readme.txt"))
    assertManifest(myModule, setOf("Bundle-Name=main", "Bundle-SymbolicName=main", "Bundle-Version=1.0.0", "Export-Package=main;version=\"1.0.0\""))
  }

  fun testMavenResources() {
    ideaBuild(myModule)
    extension(myModule).properties.myAdditionalProperties = mapOf("Include-Resource" to "included.txt=${getAbsolutePath("main/res/included.txt")}")
    createFile("main/src/main/Main.java", "package main;\n\npublic interface Main { String greeting(); }")
    createFile("main/res/included.txt", "")
    createFile("main/res/excluded.txt", "")
    makeAll().assertSuccessful()

    assertJar(myModule, setOf("META-INF/MANIFEST.MF", "main/Main.class", "included.txt"))
    assertManifest(myModule, setOf("Bundle-Name=main", "Bundle-SymbolicName=main", "Bundle-Version=1.0.0", "Export-Package=main;version=\"1.0.0\""))
  }

  fun testCompositeBundle() {
    bndBuild(myModule)
    val subModule = module("util", false)
    myModule.dependenciesList.addModuleDependency(subModule)

    createFile("main/bnd.bnd", "Bundle-SymbolicName: main\nBundle-Version: 1.0.0\nPrivate-Package: main,util")
    createFile("main/src/main/Main.java", "package main;\n\npublic class Main { String greeting() { return util.Util.GREET; } }")
    createFile("util/src/util/Util.java", "package util;\n\npublic interface Util { String GREET = \"Hello\"; }")
    makeAll().assertSuccessful()

    assertJar(myModule, setOf("META-INF/MANIFEST.MF", "main/Main.class", "util/Util.class"))
    assertManifest(myModule, setOf("Bundle-Name=main", "Bundle-SymbolicName=main", "Bundle-Version=1.0.0", "Private-Package=main,util"))
  }

  fun testRebuild() {
    bndBuild(myModule)
    createFile("main/bnd.bnd", "Bundle-SymbolicName: main\nBundle-Version: 1.0.0\nExport-Package: main")
    createFile("main/src/main/Main.java", "package main;\n\npublic interface Main { String greeting(); }")
    makeAll().assertBundleCompiled(myModule)
    assertJar(myModule, setOf("META-INF/MANIFEST.MF", "main/Main.class"))
    makeAll().assertUpToDate()

    changeFile("main/src/main/Main.java", "package main;\n\npublic interface Main { String greeting(); }\n")
    makeAll().assertBundleCompiled(myModule)
    makeAll().assertUpToDate()

    changeFile("main/bnd.bnd", "Bundle-SymbolicName: main\nBundle-Version: 1.0.1\nExport-Package: main")
    makeAll().assertBundleCompiled(myModule)
    makeAll().assertUpToDate()

    rebuildAll()
    makeAll().assertUpToDate()

    extension(myModule).properties.myAlwaysRebuildBundleJar = true
    makeAll().assertBundleCompiled(myModule)
    makeAll().assertBundleCompiled(myModule)
  }

  fun testRebuildOnDependencyChange() {
    bndBuild(myModule)
    val subModule = module("sub", false)
    myModule.dependenciesList.addModuleDependency(subModule)

    createFile("main/bnd.bnd", "Bundle-SymbolicName: main\nBundle-Version: 1.0.0\nExport-Package: main;-split-package:=merge-first")
    createFile("main/src/main/Main.java", "package main;\n\npublic class Main { String greeting() { return Sub.GREET; } }")
    createFile("sub/src/main/Sub.java", "package main;\n\npublic interface Sub { String GREET = \"Hello\"; }")
    makeAll().assertBundleCompiled(myModule)
    makeAll().assertUpToDate()

    changeFile("sub/src/main/Sub.java", "package main;\n\npublic interface Sub { String GREET = \"Hi\"; }")
    makeAll().assertBundleCompiled(myModule)
    makeAll().assertUpToDate()
  }

  fun testUnusedImport() {
    ideaBuild(myModule)
    extension(myModule).properties.myAdditionalProperties = mapOf("Import-Package" to "org.osgi.*")
    createFile("main/src/main/Main.java", "package main;\n\npublic interface Main { String greeting(); }")
    makeAll().assertSuccessful()

    assertJar(myModule, setOf("META-INF/MANIFEST.MF", "main/Main.class"))
    assertManifest(myModule, setOf("Bundle-Name=main", "Bundle-SymbolicName=main", "Bundle-Version=1.0.0", "Export-Package=main;version=\"1.0.0\""))
  }

  fun testManualMode() {
    val properties = extension(myModule).properties
    properties.myManifestGenerationMode = ManifestGenerationMode.Manually
    properties.myManifestLocation = "main/META-INF/MANIFEST.MF"
    properties.myAdditionalProperties = mapOf("Export-Package" to "main")
    createFile("main/src/main/Main.java", "package main;\n\npublic class Main { public void main() { util.Util.util(); } }")
    createFile("main/src/util/Util.java", "package util;\n\npublic class Util { public static void util() { } }")
    createFile("main/META-INF/MANIFEST.MF",
        "Manifest-Version: 1.0\nBundle-Name: main\nBundle-SymbolicName: main\nBundle-Version: 1.0.0\nExport-Package: main;version=\"1.0.0\"\n")
    makeAll().assertSuccessful()

    assertJar(myModule, setOf("META-INF/MANIFEST.MF", "main/Main.class", "util/Util.class"))
    assertManifest(myModule, setOf("Bundle-Name=main", "Bundle-SymbolicName=main", "Bundle-Version=1.0.0", "Export-Package=main;version=\"1.0.0\""))
  }
}