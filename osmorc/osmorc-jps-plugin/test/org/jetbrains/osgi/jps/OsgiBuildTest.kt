// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.osgi.jps

import org.jetbrains.jps.model.module.JpsModule
import org.jetbrains.osgi.jps.model.ManifestGenerationMode
import org.jetbrains.osgi.jps.model.OsmorcJarContentEntry
import java.io.File

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
    buildAllModules().assertSuccessful()

    assertJar(myModule, setOf("META-INF/MANIFEST.MF", "main/Main.class"))
    assertManifest(myModule, setOf("Bundle-Name=main",
                                   "Bundle-SymbolicName=main",
                                   "Bundle-Version=1.0.0",
                                   "Export-Package=main;version=\"1.0.0\"",
                                   "Import-Package=java.lang"))
  }

  fun testBndProjectWithoutPackages() {
    bndBuild(myModule)
    createFile("main/bnd.bnd", "Bundle-SymbolicName: main\nBundle-Version: 1.0.0")
    createFile("main/src/main/Main.java", "package main;\n\npublic interface Main { String greeting(); }")
    createFile("main/res/skipped.txt", "(empty)")
    buildAllModules().assertSuccessful()

    assertJar(myModule, setOf("META-INF/MANIFEST.MF"))
    assertManifest(myModule, setOf("Bundle-Name=main", "Bundle-SymbolicName=main", "Bundle-Version=1.0.0"))
  }

  fun testBndtoolsProject() {
    bndBuild(myModule)
    createFile("cnf/build.bnd", "src=src\nbin=out")
    createFile("main/bnd.bnd", "Bundle-SymbolicName: main\nBundle-Version: 1.0.0\nExport-Package: main")
    createFile("main/src/main/Main.java", "package main;\n\npublic interface Main { String greeting(); }")
    buildAllModules().assertSuccessful()

    assertJar(myModule, setOf("META-INF/MANIFEST.MF", "main/Main.class", "OSGI-OPT/src/main/Main.java"))
    assertManifest(myModule, setOf("Bundle-Name=main",
                                   "Bundle-SymbolicName=main",
                                   "Bundle-Version=1.0.0",
                                   "Export-Package=main;version=\"1.0.0\"",
                                   "Import-Package=java.lang"))
  }

  fun testIdeaProjectSimple() {
    ideaBuild(myModule)
    extension(myModule).properties.myAdditionalProperties = mapOf("Export-Package" to "main")
    createFile("main/src/main/Main.java", "package main;\n\npublic class Main { public void main() { util.Util.util(); } }")
    createFile("main/src/util/Util.java", "package util;\n\npublic class Util { public static void util() { } }")
    createFile("main/res/skipped.txt", "(empty)")
    buildAllModules().assertSuccessful()

    assertJar(myModule, setOf("META-INF/MANIFEST.MF", "main/Main.class", "util/Util.class"))
    assertManifest(myModule, setOf("Bundle-Name=main",
                                   "Bundle-SymbolicName=main",
                                   "Bundle-Version=1.0.0",
                                   "Export-Package=main;version=\"1.0.0\"",
                                   "Import-Package=java.lang"))
  }

  fun testIdeaProjectNotSpecified() {
    ideaBuild(myModule)
    createFile("main/src/main/Main.java", "package main;\n\npublic class Main { public void main() { util.Util.util(); } }")
    createFile("main/src/util/Util.java", "package util;\n\npublic class Util { public static void util() { } }")
    createFile("main/res/skipped.txt", "(empty)")
    buildAllModules().assertSuccessful()

    assertJar(myModule, setOf("META-INF/MANIFEST.MF", "main/Main.class", "util/Util.class"))
    assertManifest(myModule, setOf("Bundle-Name=main",
                                   "Bundle-SymbolicName=main",
                                   "Bundle-Version=1.0.0",
                                   "Export-Package=main;version=\"1.0.0\",util;version=\"1.0.0\"",
                                   "Import-Package=java.lang,util"))
  }

  fun testAdditionalContent() {
    ideaBuild(myModule)
    extension(myModule).properties.myAdditionalJARContents.add(OsmorcJarContentEntry(getAbsolutePath("content/readme.txt"), "readme.txt"))
    createFile("content/readme.txt", "Hiya there.")
    createFile("main/src/main/Main.java", "package main;\n\npublic interface Main { String greeting(); }")
    buildAllModules().assertSuccessful()

    assertJar(myModule, setOf("META-INF/MANIFEST.MF", "main/Main.class", "readme.txt"))
    assertManifest(myModule, setOf("Bundle-Name=main",
                                   "Bundle-SymbolicName=main",
                                   "Bundle-Version=1.0.0",
                                   "Export-Package=main;version=\"1.0.0\"",
                                   "Import-Package=java.lang"))
  }

  fun testMavenResources() {
    ideaBuild(myModule)
    extension(myModule).properties.myAdditionalProperties = mapOf("Include-Resource" to "included.txt=${getAbsolutePath("main/res/included.txt")}")
    createFile("main/src/main/Main.java", "package main;\n\npublic interface Main { String greeting(); }")
    createFile("main/res/included.txt", "")
    createFile("main/res/excluded.txt", "")
    buildAllModules().assertSuccessful()

    assertJar(myModule, setOf("META-INF/MANIFEST.MF", "main/Main.class", "included.txt"))
    assertManifest(myModule, setOf("Bundle-Name=main",
                                   "Bundle-SymbolicName=main",
                                   "Bundle-Version=1.0.0",
                                   "Export-Package=main;version=\"1.0.0\"",
                                   "Import-Package=java.lang"))
  }

  fun testCompositeBundle() {
    bndBuild(myModule)
    val subModule = module("util", false)
    myModule.dependenciesList.addModuleDependency(subModule)

    createFile("main/bnd.bnd", "Bundle-SymbolicName: main\nBundle-Version: 1.0.0\nPrivate-Package: main,util")
    createFile("main/src/main/Main.java", "package main;\n\npublic class Main { String greeting() { return util.Util.GREET; } }")
    createFile("util/src/util/Util.java", "package util;\n\npublic interface Util { String GREET = \"Hello\"; }")
    buildAllModules().assertSuccessful()

    assertJar(myModule, setOf("META-INF/MANIFEST.MF", "main/Main.class", "util/Util.class"))
    assertManifest(myModule, setOf("Bundle-Name=main",
                                   "Bundle-SymbolicName=main",
                                   "Bundle-Version=1.0.0",
                                   "Private-Package=main,util",
                                   "Import-Package=java.lang"))
  }

  fun testRebuild() {
    bndBuild(myModule)
    createFile("main/bnd.bnd", "Bundle-SymbolicName: main\nBundle-Version: 1.0.0\nExport-Package: main")
    createFile("main/src/main/Main.java", "package main;\n\npublic interface Main { String greeting(); }")
    buildAllModules().assertBundleCompiled(myModule)
    assertJar(myModule, setOf("META-INF/MANIFEST.MF", "main/Main.class"))
    buildAllModules().assertUpToDate()

    changeFile("main/src/main/Main.java", "package main;\n\npublic interface Main { String greeting(); }\n")
    buildAllModules().assertBundleCompiled(myModule)
    buildAllModules().assertUpToDate()

    changeFile("main/bnd.bnd", "Bundle-SymbolicName: main\nBundle-Version: 1.0.1\nExport-Package: main")
    buildAllModules().assertBundleCompiled(myModule)
    buildAllModules().assertUpToDate()

    rebuildAllModules()
    buildAllModules().assertUpToDate()

    extension(myModule).properties.myAlwaysRebuildBundleJar = true
    buildAllModules().assertBundleCompiled(myModule)
    buildAllModules().assertBundleCompiled(myModule)
  }

  fun testRebuildOnDependencyChange() {
    bndBuild(myModule)
    val subModule = module("sub", false)
    myModule.dependenciesList.addModuleDependency(subModule)

    createFile("main/bnd.bnd", "Bundle-SymbolicName: main\nBundle-Version: 1.0.0\nExport-Package: main;-split-package:=merge-first")
    createFile("main/src/main/Main.java", "package main;\n\npublic class Main { String greeting() { return Sub.GREET; } }")
    createFile("sub/src/main/Sub.java", "package main;\n\npublic interface Sub { String GREET = \"Hello\"; }")
    buildAllModules().assertBundleCompiled(myModule)
    buildAllModules().assertUpToDate()

    changeFile("sub/src/main/Sub.java", "package main;\n\npublic interface Sub { String GREET = \"Hi\"; }")
    buildAllModules().assertBundleCompiled(myModule)
    buildAllModules().assertUpToDate()
  }

  fun testUnusedImport() {
    ideaBuild(myModule)
    extension(myModule).properties.myAdditionalProperties = mapOf("Import-Package" to "org.osgi.*")
    createFile("main/src/main/Main.java", "package main;\n\npublic interface Main { String greeting(); }")
    buildAllModules().assertSuccessful()

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
    buildAllModules().assertSuccessful()

    assertJar(myModule, setOf("META-INF/MANIFEST.MF", "main/Main.class", "util/Util.class"))
    assertManifest(myModule, setOf("Bundle-Name=main", "Bundle-SymbolicName=main", "Bundle-Version=1.0.0", "Export-Package=main;version=\"1.0.0\""))
  }

  fun testEmptyProject() {
    bndBuild(myModule)
    createFile("main/bnd.bnd", "Bundle-SymbolicName: main\nBundle-Version: 1.0.0\nImport-Package: *\nExport-Package: !*")
    buildAllModules().assertSuccessful()

    assertJar(myModule, setOf("META-INF/MANIFEST.MF"))
    assertManifest(myModule, setOf("Bundle-Name=main", "Bundle-SymbolicName=main", "Bundle-Version=1.0.0"))
  }

  fun testErrorProcessingDirective() {
    bndBuild(myModule)
    createFile("main/bnd.bnd", "-fixupmessages: \"No value *\";is:=ignore\nBad-Property\n")
    buildAllModules().assertSuccessful()
  }

  fun testBndtoolsSubBundles() {
    bndBuild(myModule)
    createFile("cnf/build.bnd", "src=src\nbin=out")
    createFile("main/src/main/a/A.java", "package main.a;\npublic class A { }")
    createFile("main/src/main/b/B.java", "package main.b;\npublic class B { }")
    createFile("main/bnd.bnd", "-sub: *.bnd")
    createFile("main/a.bnd", "Bundle-Version=1.0.0\nExport-Package: main.a")
    createFile("main/b.bnd", "Bundle-Version=1.0.1\nExport-Package: main.b")
    buildAllModules().assertBundlesCompiled(myModule, "main.a.jar", "main.b.jar")

    assertFalse(File(extension(myModule).jarFileLocation).exists())
    assertJar(myModule, "main.a.jar", setOf("META-INF/MANIFEST.MF", "OSGI-OPT/src/main/a/A.java", "main/a/A.class"))
    assertManifest(myModule, "main.a.jar", setOf("Bundle-Name=main.a",
                                                 "Bundle-SymbolicName=main.a",
                                                 "Bundle-Version=1.0.0",
                                                 "Export-Package=main.a;version=\"1.0.0\"",
                                                 "Import-Package=java.lang"))
    assertJar(myModule, "main.b.jar", setOf("META-INF/MANIFEST.MF", "OSGI-OPT/src/main/b/B.java", "main/b/B.class"))
    assertManifest(myModule, "main.b.jar", setOf("Bundle-Name=main.b",
                                                 "Bundle-SymbolicName=main.b",
                                                 "Bundle-Version=1.0.1",
                                                 "Export-Package=main.b;version=\"1.0.1\"",
                                                 "Import-Package=java.lang"))
  }

  fun testSubBundles() {
    bndBuild(myModule)
    createFile("main/src/main/a/A.java", "package main.a;\npublic class A { }")
    createFile("main/src/main/b/B.java", "package main.b;\npublic class B { }")
    createFile("main/bnd.bnd", "-sub: *.bnd")
    createFile("main/a.bnd", "Bundle-Version=1.0.0\nExport-Package: main.a")
    createFile("main/b.bnd", "Bundle-Version=1.0.1\nExport-Package: main.b")
    buildAllModules().assertBundlesCompiled(myModule, "main.a.jar", "main.b.jar")

    assertFalse(File(extension(myModule).jarFileLocation).exists())
    assertJar(myModule, "main.a.jar", setOf("META-INF/MANIFEST.MF", "main/a/A.class"))
    assertManifest(myModule, "main.a.jar", setOf("Bundle-Name=main.a",
                                                 "Bundle-SymbolicName=main.a",
                                                 "Bundle-Version=1.0.0",
                                                 "Export-Package=main.a;version=\"1.0.0\"",
                                                 "Import-Package=java.lang"))
    assertJar(myModule, "main.b.jar", setOf("META-INF/MANIFEST.MF", "main/b/B.class"))
    assertManifest(myModule, "main.b.jar", setOf("Bundle-Name=main.b",
                                                 "Bundle-SymbolicName=main.b",
                                                 "Bundle-Version=1.0.1",
                                                 "Export-Package=main.b;version=\"1.0.1\"",
                                                 "Import-Package=java.lang"))
  }
}