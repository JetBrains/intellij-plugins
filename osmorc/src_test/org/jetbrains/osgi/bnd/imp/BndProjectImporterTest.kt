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
package org.jetbrains.osgi.bnd.imp

import aQute.bnd.build.Workspace
import com.intellij.compiler.CompilerConfiguration
import com.intellij.compiler.impl.javaCompiler.javac.JavacConfiguration
import com.intellij.ide.actions.ImportModuleAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.roots.*
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.pom.java.LanguageLevel
import com.intellij.testFramework.IdeaTestCase
import org.jetbrains.osgi.jps.model.ManifestGenerationMode
import org.osmorc.facet.OsmorcFacet
import java.io.File

class BndProjectImporterTest : IdeaTestCase() {
  private lateinit var myWorkspace: Workspace
  private lateinit var myImporter: BndProjectImporter

  override fun setUp() {
    super.setUp()

    val path = myProject.basePath!!
    File(path, "cnf/ext").mkdirs()
    FileUtil.writeToFile(File(path, "cnf/build.bnd"), "javac.source: 1.8\njavac.target: 1.8")
    File(path, "hello.provider/src").mkdirs()
    FileUtil.writeToFile(File(path, "hello.provider/bnd.bnd"), "javac.source: 1.7\njavac.target: 1.7")
    File(path, "hello.consumer/src").mkdirs()
    FileUtil.writeToFile(File(path, "hello.consumer/bnd.bnd"), "-buildpath: hello.provider")
    File(path, "hello.tests/src").mkdirs()
    FileUtil.writeToFile(File(path, "hello.tests/bnd.bnd"), "-nobundles: true\n-testpath: hello.provider,hello.consumer")

    myWorkspace = Workspace.getWorkspace(File(path), BndProjectImporter.CNF_DIR)
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
    val rootModule: Module

    val model = ModuleManager.getInstance(myProject).modifiableModel
    try {
      rootModule = myImporter.createRootModule(model)
      model.commit()
    }
    catch (e: Throwable) {
      model.dispose()
      throw e
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
    myImporter.resolve()

    val modules = ModuleManager.getInstance(myProject).modules
    assertEquals(3, modules.size)
    assertEquals(setOf("hello.provider", "hello.consumer", "hello.tests"), modules.map { it.name }.toSet())

    modules.forEach {
      val rootManager = ModuleRootManager.getInstance(it)
      assertEquals(it.name, 1, rootManager.contentRootUrls.size)
      assertEquals(it.name, 2, rootManager.sourceRootUrls.size)
      assertEquals(it.name, 3, rootManager.excludeRootUrls.size)

      val dependencies = getDependencies(it)
      when (it.name) {
        "hello.provider" -> assertEquals(listOf("<jdk>", "<src>"), dependencies)
        "hello.consumer" -> assertEquals(listOf("<jdk>", "<src>", "hello.provider"), dependencies)
        "hello.tests" -> assertEquals(listOf("<jdk>", "<src>", "hello.provider", "hello.consumer"), dependencies)
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

  fun testReimport() {
    assertNotNull(BndProjectImporter.findWorkspace(myProject))
    BndProjectImporter.reimportWorkspace(myProject)

    assertEquals(LanguageLevel.JDK_1_8, LanguageLevelProjectExtension.getInstance(myProject).languageLevel)
    val module = ModuleManager.getInstance(myProject).findModuleByName("hello.tests")!!
    assertEquals(listOf("<jdk>", "<src>", "hello.provider", "hello.consumer"), getDependencies(module))
    assertNull(OsmorcFacet.getInstance(module))

    FileUtil.writeToFile(File(myProject.basePath!!, "cnf/build.bnd"), "javac.source: 1.7\njavac.target: 1.8")
    FileUtil.writeToFile(File(myProject.basePath!!, "hello.tests/bnd.bnd"), "-testpath: hello.provider")
    BndProjectImporter.reimportWorkspace(myProject)

    assertEquals(LanguageLevel.JDK_1_7, LanguageLevelProjectExtension.getInstance(myProject).languageLevel)
    assertEquals(listOf("<jdk>", "<src>", "hello.provider"), getDependencies(module))
    assertNotNull(OsmorcFacet.getInstance(module))
  }


  private fun getDependencies(it: Module): List<String> {
    val dependencies: MutableList<String> = arrayListOf()
    ModuleRootManager.getInstance(it).orderEntries().forEach {
      dependencies.add(when (it) {
        is ModuleSourceOrderEntry -> "<src>"
        is JdkOrderEntry -> "<jdk>"
        else -> it.presentableName
      })
    }
    return dependencies
  }
}
