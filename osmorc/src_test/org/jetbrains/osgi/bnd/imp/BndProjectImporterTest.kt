// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.osgi.bnd.imp

import aQute.bnd.build.Workspace
import com.intellij.compiler.CompilerConfiguration
import com.intellij.compiler.impl.javaCompiler.javac.JavacConfiguration
import com.intellij.ide.actions.ImportModuleAction
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.roots.JdkOrderEntry
import com.intellij.openapi.roots.LanguageLevelModuleExtension
import com.intellij.openapi.roots.LanguageLevelProjectExtension
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ModuleSourceOrderEntry
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.pom.java.LanguageLevel
import com.intellij.testFramework.JavaProjectTestCase
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.osgi.jps.model.ManifestGenerationMode
import org.osmorc.facet.OsmorcFacet
import java.io.File

class BndProjectImporterTest : JavaProjectTestCase() {
  private lateinit var myProjectDir: String
  private lateinit var myWorkspace: Workspace
  private lateinit var myImporter: BndProjectImporter

  override fun setUp() {
    super.setUp()

    myProjectDir = myProject.basePath!!
    File(myProjectDir, "cnf/ext").mkdirs()
    File(myProjectDir, "cnf/build.bnd").writeText("javac.source: 1.8\njavac.target: 1.8")
    File(myProjectDir, "hello.provider/src").mkdirs()
    File(myProjectDir, "hello.provider/bnd.bnd").writeText("javac.source: 1.7\njavac.target: 1.7")
    File(myProjectDir, "hello.consumer/src").mkdirs()
    File(myProjectDir, "hello.consumer/bnd.bnd").writeText("-buildpath: hello.provider")
    File(myProjectDir, "hello.tests/src").mkdirs()
    File(myProjectDir, "hello.tests/bnd.bnd").writeText("-nobundles: true\n-testpath: hello.provider,hello.consumer")

    myWorkspace = Workspace.getWorkspace(File(myProjectDir), BndProjectImporter.CNF_DIR)
    myImporter = BndProjectImporter(myProject, myWorkspace, BndProjectImporter.getWorkspaceProjects(myWorkspace))
  }

  override fun setUpModule() { }


  fun testProviders() {
    val projectImporters = ImportModuleAction.getProviders(null).map { it.javaClass.simpleName }.toSet()
    assertTrue("BndProjectImportProvider" in projectImporters)
    assertFalse("BndModuleImportProvider" in projectImporters)

    val moduleImporters = ImportModuleAction.getProviders(project).map { it.javaClass.simpleName }.toSet()
    assertFalse("BndProjectImportProvider" in moduleImporters)
    assertTrue("BndModuleImportProvider" in moduleImporters)
  }

  fun testRootModule() {
    val model = ModuleManager.getInstance(myProject).getModifiableModel()
    val rootModule = try {
      val module = myImporter.createRootModule(model)
      runWriteAction { model.commit() }
      module
    }
    catch (t: Throwable) {
      model.dispose()
      throw t
    }
    val rootManager = ModuleRootManager.getInstance(rootModule)
    assertEquals(1, rootManager.contentRootUrls.size)
    assertEquals(0, rootManager.sourceRootUrls.size)
    assertNull(OsmorcFacet.getInstance(rootModule))
  }

  fun testProjectSetup() {
    myImporter.setupProject()

    val sourceLevel = LanguageLevelProjectExtension.getInstance(myProject).languageLevel
    assertEquals(LanguageLevel.JDK_1_8, sourceLevel)

    val targetLevel = CompilerConfiguration.getInstance(myProject).projectBytecodeTarget
    assertEquals("1.8", targetLevel)

    val javacOptions = JavacConfiguration.getOptions(myProject, JavacConfiguration::class.java)
    assertTrue(javacOptions.DEBUGGING_INFO)
    assertEquals("", javacOptions.ADDITIONAL_OPTIONS_STRING)
  }

  fun testImport() {
    myImporter.resolve(false)

    val modules = ModuleManager.getInstance(myProject).modules
    assertThat(modules.map { it.name }).containsExactlyInAnyOrder("hello.provider", "hello.consumer", "hello.tests")

    modules.forEach {
      val rootManager = ModuleRootManager.getInstance(it)
      assertEquals(it.name, 1, rootManager.contentRootUrls.size)
      assertEquals(it.name, 2, rootManager.sourceRootUrls.size)
      assertEquals(it.name, 3, rootManager.excludeRootUrls.size)

      val dependencies = getDependencies(it)
      when (it.name) {
        "hello.provider" -> assertThat(dependencies).containsExactly("<jdk>", "<src>")
        "hello.consumer" -> assertThat(dependencies).containsExactly("<jdk>", "<src>", "hello.provider")
        "hello.tests" -> assertThat(dependencies).containsExactly("<jdk>", "<src>", "hello.provider", "hello.consumer")
      }

      val sourceLevel = ModuleRootManager.getInstance(it).getModuleExtension(LanguageLevelModuleExtension::class.java).languageLevel
      val expectedSource = if (it.name == "hello.provider") LanguageLevel.JDK_1_7 else LanguageLevel.JDK_1_8
      assertEquals(expectedSource, sourceLevel)

      val targetLevel = CompilerConfiguration.getInstance(myProject).getBytecodeTargetLevel(it)
      val expectedTarget = if (it.name == "hello.provider") "1.7" else "1.8"
      assertEquals(expectedTarget, targetLevel)

      val facet = OsmorcFacet.getInstance(it)
      if (it.name != "hello.tests") {
        assertNotNull(it.name, facet)
        val config = facet!!.configuration
        assertEquals(it.name, ManifestGenerationMode.Bnd, config.manifestGenerationMode)
        assertEquals(it.name, "bnd.bnd", config.bndFileLocation)
        assertEquals(it.name, "${VfsUtilCore.urlToPath(rootManager.contentRootUrls[0])}/generated/${it.name}.jar", config.jarFileLocation)
      }
      else {
        assertNull(facet)
      }
    }
  }

  fun testSubBundles() {
    File(myProjectDir, "hello.provider/bnd.bnd").writeText("-sub: *.bnd")
    File(myProjectDir, "hello.provider/a.bnd").writeText("")
    File(myProjectDir, "hello.provider/b.bnd").writeText("")
    File(myProjectDir, "hello.consumer/bnd.bnd").writeText("-buildpath: hello.provider.a,hello.provider.b")
    File(myProjectDir, "hello.tests/bnd.bnd").writeText("-nobundles: true\n-testpath: hello.provider.a,hello.provider.b")

    assertNotNull(BndProjectImporter.findWorkspace(myProject))
    BndProjectImporter.reimportWorkspace(myProject)

    val consumer = ModuleManager.getInstance(myProject).modules.find { it.name == "hello.consumer" }!!
    assertThat(getDependencies(consumer)).containsExactly("<jdk>", "<src>", "hello.provider")
    val tests = ModuleManager.getInstance(myProject).modules.find { it.name == "hello.tests" }!!
    assertThat(getDependencies(tests)).containsExactly("<jdk>", "<src>", "hello.provider")
  }

  fun testReimport() {
    assertNotNull(BndProjectImporter.findWorkspace(myProject))
    BndProjectImporter.reimportWorkspace(myProject)

    assertEquals(LanguageLevel.JDK_1_8, LanguageLevelProjectExtension.getInstance(myProject).languageLevel)
    val module = ModuleManager.getInstance(myProject).findModuleByName("hello.tests")!!
    assertThat(getDependencies(module)).containsExactly("<jdk>", "<src>", "hello.provider", "hello.consumer")
    assertNull(OsmorcFacet.getInstance(module))

    File(myProjectDir, "cnf/build.bnd").writeText("javac.source: 1.7\njavac.target: 1.8")
    File(myProjectDir, "hello.tests/bnd.bnd").writeText("-testpath: hello.provider")
    BndProjectImporter.reimportWorkspace(myProject)

    assertEquals(LanguageLevel.JDK_1_7, LanguageLevelProjectExtension.getInstance(myProject).languageLevel)
    assertThat(getDependencies(module)).containsExactly("<jdk>", "<src>", "hello.provider")
    assertNotNull(OsmorcFacet.getInstance(module))
  }


  private fun getDependencies(it: Module): List<String> {
    val dependencies: MutableList<String> = arrayListOf()
    ModuleRootManager.getInstance(it).orderEntries().forEach {
      dependencies += when (it) {
        is ModuleSourceOrderEntry -> "<src>"
        is JdkOrderEntry -> "<jdk>"
        else -> it.presentableName
      }
      true
    }
    return dependencies
  }
}