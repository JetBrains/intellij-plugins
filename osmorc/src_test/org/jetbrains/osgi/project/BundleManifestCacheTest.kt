// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.osgi.project

import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import org.jetbrains.osgi.jps.model.ManifestGenerationMode
import org.osmorc.LightOsgiFixtureTestCase

class BundleManifestCacheTest : LightOsgiFixtureTestCase() {
  private lateinit var myCache: BundleManifestCache

  override fun setUp() {
    super.setUp()
    myCache = BundleManifestCache.getInstance()
  }


  fun testLibManifest() {
    assertManifest(myCache.getManifest(findClass("aQute.bnd.repository.fileset.FileSetRepository")))
    assertManifest(myCache.getManifest(findClass("aQute.lib.fileset.FileSet")))
  }

  fun testNonBundleLibManifest() {
    assertNotNull(myCache.getManifest(findClass("org.codehaus.plexus.util.IOUtil")))
  }

  fun testJdkManifest() {
    val manifest = myCache.getManifest(findClass("javax.swing.Icon"))
    assertNotNull(manifest)
    assertNotNull(manifest?.bundleSymbolicName)
    assertEquals("javax.swing", manifest?.getExportedPackage("javax.swing"))
  }

  fun testModuleManual() {
    myConfiguration.manifestGenerationMode = ManifestGenerationMode.Manually
    myFixture.addFileToProject(myConfiguration.manifestLocation, "Bundle-SymbolicName: test\n")
    assertManifest(myCache.getManifest(module))
  }

  fun testModuleGenerated() {
    myConfiguration.manifestGenerationMode = ManifestGenerationMode.OsmorcControlled
    myConfiguration.setBundleSymbolicName("test")
    assertManifest(myCache.getManifest(module))
  }

  fun testModuleBnd() {
    myConfiguration.manifestGenerationMode = ManifestGenerationMode.Bnd
    myFixture.addFileToProject(myConfiguration.bndFileLocation, "Bundle-SymbolicName: test\n")
    assertManifest(myCache.getManifest(module))
  }

  fun testModuleBndImplicitBSN() {
    myConfiguration.manifestGenerationMode = ManifestGenerationMode.Bnd
    myFixture.addFileToProject(myConfiguration.bndFileLocation, "Bundle-Version: 1.0.0\n")
    assertManifest(myCache.getManifest(module))
  }

  fun testModuleCacheUpdate() {
    myConfiguration.manifestGenerationMode = ManifestGenerationMode.OsmorcControlled
    assertNotNull(myCache.getManifest(module))

    myConfiguration.manifestGenerationMode = ManifestGenerationMode.Bnd
    assertNull(myCache.getManifest(module))

    myFixture.addFileToProject(myConfiguration.bndFileLocation, "Bundle-SymbolicName: test\n")
    assertNotNull(myCache.getManifest(module))
  }


  private fun findClass(className: String): PsiClass =
    JavaPsiFacade.getInstance(project).findClass(className, module.moduleWithLibrariesScope)!!

  private fun assertManifest(manifest: BundleManifest?) {
    assertNotNull(manifest)
    assertTrue(StringUtil.isNotEmpty(manifest!!.bundleSymbolicName))
  }
}
