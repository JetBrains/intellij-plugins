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
import org.jetbrains.osgi.jps.model.OsmorcJarContentEntry
import kotlin.properties.Delegates

class OsgiBuildTest : OsgiBuildTestCase() {
  var myModule: JpsModule by Delegates.notNull()

  override fun setUp() {
    super.setUp()
    myModule = module("main")
  }

  fun testPlainBndProject() {
    bndBuild(myModule)
    createFile("main/bnd.bnd", "Bundle-SymbolicName: main\nBundle-Version: 1.0.0\nExport-Package: main")
    createFile("main/src/main/Main.java", "package main;\n\npublic interface Main { String greeting(); }")
    createFile("main/res/skipped.txt", "(empty)")
    makeAll().assertSuccessful()

    assertJar(myModule, setOf("META-INF/MANIFEST.MF", "main/Main.class"))
    assertManifest(myModule, setOf("Bundle-Name=main", "Bundle-SymbolicName=main", "Bundle-Version=1.0.0", "Export-Package=main;version=\"1.0.0\""))
  }

  fun testBndProjectNotSpecified() {
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

  fun testPlainIdeaProject() {
    ideaBuild(myModule)
    createFile("main/src/main/Main.java", "package main;\n\npublic interface Main { String greeting(); }")
    createFile("main/res/skipped.txt", "(empty)")
    makeAll().assertSuccessful()

    assertJar(myModule, setOf("META-INF/MANIFEST.MF", "main/Main.class"))
    assertManifest(myModule, setOf("Bundle-Name=main", "Bundle-SymbolicName=main", "Bundle-Version=1.0.0", "Export-Package=main;version=\"1.0.0\""))
  }

  fun testIdeaProjectNotSpecified() {
    ideaBuild(myModule)
    extension(myModule).getProperties().myAdditionalProperties = emptyMap()
    createFile("main/src/main/Main.java", "package main;\n\npublic interface Main { String greeting(); }")
    createFile("main/res/skipped.txt", "(empty)")
    makeAll().assertSuccessful()

    assertJar(myModule, setOf("META-INF/MANIFEST.MF"))
    assertManifest(myModule, setOf("Bundle-Name=main", "Bundle-SymbolicName=main", "Bundle-Version=1.0.0"))
  }

  fun testIdeaProjectWithImpl() {
    ideaBuild(myModule)
    extension(myModule).getProperties().myAdditionalProperties.put("Private-Package", "impl")
    createFile("main/src/main/Main.java", "package main;\n\npublic interface Main { String greeting(); }")
    createFile("main/src/impl/MainImpl.java", "package impl;\n\npublic class MainImpl implements main.Main { public String greeting() {return \"\";} }")
    makeAll().assertSuccessful()

    assertJar(myModule, setOf("META-INF/MANIFEST.MF", "main/Main.class", "impl/MainImpl.class"))
    assertManifest(myModule, setOf("Bundle-Name=main", "Bundle-SymbolicName=main", "Bundle-Version=1.0.0", "Export-Package=main;version=\"1.0.0\"", "Import-Package=main"))
  }

  fun testAdditionalContent() {
    ideaBuild(myModule)
    val entry = OsmorcJarContentEntry(getAbsolutePath("content/readme.txt"), "readme.txt")
    extension(myModule).getProperties().myAdditionalJARContents.add(entry)
    createFile("content/readme.txt", "Hiya there.")
    createFile("main/src/main/Main.java", "package main;\n\npublic interface Main { String greeting(); }")
    makeAll().assertSuccessful()

    assertJar(myModule, setOf("META-INF/MANIFEST.MF", "main/Main.class", "readme.txt"))
    assertManifest(myModule, setOf("Bundle-Name=main", "Bundle-SymbolicName=main", "Bundle-Version=1.0.0", "Export-Package=main;version=\"1.0.0\""))
  }

  fun testIdeaProjectPartiallySpecified() {
    ideaBuild(myModule)
    extension(myModule).getProperties().myAdditionalProperties = mapOf("Private-Package" to "util")
    createFile("main/src/main/Main.java", "package main;\npublic class Main { public void main() { util.Util.util(); } }")
    createFile("main/src/util/Util.java", "package util;\npublic class Util { public static void util() { } }")
    createFile("main/res/skipped.txt", "(empty)")
    makeAll().assertSuccessful()

    assertJar(myModule, setOf("META-INF/MANIFEST.MF", "util/Util.class"))
    assertManifest(myModule, setOf("Bundle-Name=main", "Bundle-SymbolicName=main", "Bundle-Version=1.0.0"))
  }

  fun testMavenProject() {
    ideaBuild(myModule)
    createMavenConfig(myModule)
    createFile("main/src/main/Main.java", "package main;\npublic class Main { public void main() { util.Util.util(); } }")
    createFile("main/src/util/Util.java", "package util;\npublic class Util { public static void util() { } }")
    createFile("main/res/readme.txt", "Hiya there.")
    makeAll().assertSuccessful()

    assertJar(myModule, setOf("META-INF/MANIFEST.MF", "main/Main.class", "util/Util.class", "readme.txt"))
    assertManifest(myModule, setOf("Bundle-Name=main", "Bundle-SymbolicName=main", "Bundle-Version=1.0.0", "Export-Package=main;version=\"1.0.0\""))
  }

  fun testEmptyMavenProjectNotSpecified() {
    ideaBuild(myModule)
    extension(myModule).getProperties().myAdditionalProperties = emptyMap()
    createMavenConfig(myModule)
    createFile("main/src/main/Main.java", "package main;\n\npublic interface Main { String greeting(); }")
    createFile("main/res/readme.txt", "Hiya there.")
    makeAll().assertSuccessful()

    assertJar(myModule, setOf("META-INF/MANIFEST.MF", "main/Main.class", "readme.txt"))
    assertManifest(myModule, setOf("Bundle-Name=main", "Bundle-SymbolicName=main", "Bundle-Version=1.0.0", "Export-Package=main;version=\"1.0.0\""))
  }

  fun testMavenProjectPartiallySpecified() {
    ideaBuild(myModule)
    createMavenConfig(myModule)
    extension(myModule).getProperties().myAdditionalProperties = mapOf("Private-Package" to "util")
    createFile("main/src/main/Main.java", "package main;\npublic class Main { public void main() { util.Util.util(); } }")
    createFile("main/src/util/Util.java", "package util;\npublic class Util { public static void util() { } }")
    createFile("main/res/readme.txt", "Hiya there.")
    makeAll().assertSuccessful()

    assertJar(myModule, setOf("META-INF/MANIFEST.MF", "main/Main.class", "util/Util.class", "readme.txt"))
    assertManifest(myModule, setOf("Bundle-Name=main", "Bundle-SymbolicName=main", "Bundle-Version=1.0.0",
        "Export-Package=main;version=\"1.0.0\",util;version=\"1.0.0\""))
  }

  fun testCompositeBundle() {
    bndBuild(myModule)
    val subModule = module("util", false)
    myModule.getDependenciesList().addModuleDependency(subModule)

    createFile("main/bnd.bnd", "Bundle-SymbolicName: main\nBundle-Version: 1.0.0\nPrivate-Package: main,util")
    createFile("main/src/main/Main.java", "package main;\npublic class Main { String greeting() { return util.Util.GREET; } }")
    createFile("util/src/util/Util.java", "package util;\npublic interface Util { String GREET = \"Hello\"; }")
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

    extension(myModule).getProperties().myAlwaysRebuildBundleJar = true
    makeAll().assertBundleCompiled(myModule)
    makeAll().assertBundleCompiled(myModule)
  }

  fun testRebuildOnDependencyChange() {
    bndBuild(myModule)
    val subModule = module("sub", false)
    myModule.getDependenciesList().addModuleDependency(subModule)

    createFile("main/bnd.bnd", "Bundle-SymbolicName: main\nBundle-Version: 1.0.0\nExport-Package: main;-split-package:=merge-first")
    createFile("main/src/main/Main.java", "package main;\npublic class Main { String greeting() { return Sub.GREET; } }")
    createFile("sub/src/main/Sub.java", "package main;\npublic interface Sub { String GREET = \"Hello\"; }")
    makeAll().assertBundleCompiled(myModule)
    makeAll().assertUpToDate()

    changeFile("sub/src/main/Sub.java", "package main;\npublic interface Sub { String GREET = \"Hi\"; }")
    makeAll().assertBundleCompiled(myModule)
    makeAll().assertUpToDate()
  }
}