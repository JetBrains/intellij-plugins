package org.osmorc.bnd

import aQute.bnd.build.Workspace
import com.intellij.compiler.impl.javaCompiler.javac.JavacConfiguration
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.roots.JdkOrderEntry
import com.intellij.openapi.roots.LanguageLevelProjectExtension
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ModuleSourceOrderEntry
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.pom.java.LanguageLevel
import com.intellij.testFramework.IdeaTestCase
import org.jetbrains.osgi.jps.model.ManifestGenerationMode
import org.junit.Assume.assumeTrue
import org.osmorc.facet.OsmorcFacet

import java.io.File

import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BndProjectImporterTest : IdeaTestCase() {
  var myImporter: BndProjectImporter by Delegates.notNull()

  override fun setUp() {
    super.setUp()

    val path = myProject.getBasePath()!!
    File(path, "cnf/ext").mkdirs()
    FileUtil.writeToFile(File(path, "cnf/build.bnd"), "javac.source: 1.8\njavac.target: 1.8")
    File(path, "hello.provider/src").mkdirs()
    FileUtil.writeToFile(File(path, "hello.provider/bnd.bnd"), "")
    File(path, "hello.consumer/src").mkdirs()
    FileUtil.writeToFile(File(path, "hello.consumer/bnd.bnd"), "-buildpath: hello.provider")
    File(path, "hello.tests/src").mkdirs()
    FileUtil.writeToFile(File(path, "hello.tests/bnd.bnd"), "-nobundles: true\n-testpath: hello.provider,hello.consumer")

    val workspace = Workspace.getWorkspace(File(path), Workspace.CNFDIR)
    myImporter = BndProjectImporter(myProject, workspace, BndProjectImporter.getWorkspaceProjects(workspace))
  }

  override fun setUpModule() { }

  fun testRootModule() {
    val rootModule: Module

    val model = ModuleManager.getInstance(myProject).getModifiableModel()
    try {
      rootModule = myImporter.createRootModule(model)
      model.commit()
    }
    catch (e: Throwable) {
      model.dispose()
      throw e
    }

    val rootManager = ModuleRootManager.getInstance(rootModule)
    assertEquals(1, rootManager.getContentRootUrls().size())
    assertEquals(0, rootManager.getSourceRootUrls().size())
    assertNull(OsmorcFacet.getInstance(rootModule))
  }

  fun testProjectSetup() {
    myImporter.setupProject()

    val projectLevel = LanguageLevelProjectExtension.getInstance(myProject).getLanguageLevel()
    assertEquals(LanguageLevel.JDK_1_8, projectLevel)

    val javacOptions = JavacConfiguration.getOptions(myProject, javaClass<JavacConfiguration>())
    assertTrue(javacOptions.DEBUGGING_INFO)
    assertEquals("", javacOptions.ADDITIONAL_OPTIONS_STRING)
  }

  fun testImport() {
    myImporter.resolve()

    val modules = ModuleManager.getInstance(myProject).getModules()
    assertEquals(3, modules.size())
    assertEquals(setOf("hello.provider", "hello.consumer", "hello.tests"), modules.map { it.getName() }.toSet())

    modules.forEach {
      val rootManager = ModuleRootManager.getInstance(it)
      assertEquals(1, rootManager.getContentRootUrls().size(), it.getName())
      assertEquals(2, rootManager.getSourceRootUrls().size(), it.getName())
      assertEquals(3, rootManager.getExcludeRootUrls().size(), it.getName())

      val dependencies = getDependencies(it)
      when (it.getName()) {
        "hello.provider" -> assertEquals(listOf("<jdk>", "<src>"), dependencies)
        "hello.consumer" -> assertEquals(listOf("<jdk>", "<src>", "hello.provider"), dependencies)
        "hello.tests" -> assertEquals(listOf("<jdk>", "<src>", "hello.provider", "hello.consumer"), dependencies)
      }

      val facet = OsmorcFacet.getInstance(it)
      if (it.getName() != "hello.tests") {
        assertNotNull(facet, it.getName())

        val config = facet.getConfiguration()
        assertEquals(ManifestGenerationMode.Bnd, config.getManifestGenerationMode())
        assertEquals("bnd.bnd", config.getBndFileLocation(), it.getName())
        assertEquals("${VfsUtilCore.urlToPath(rootManager.getContentRootUrls()[0])}/generated/${it.getName()}.jar", config.getJarFileLocation())
      }
      else {
        assertNull(facet)
      }
    }
  }

  private fun getDependencies(it: Module): List<String> {
    val dependencies: MutableList<String> = arrayListOf()
    ModuleRootManager.getInstance(it).orderEntries().forEach {
      dependencies.add(when (it) {
        is ModuleSourceOrderEntry -> "<src>"
        is JdkOrderEntry -> "<jdk>"
        else -> it.getPresentableName()
      })
    }
    return dependencies
  }
}
