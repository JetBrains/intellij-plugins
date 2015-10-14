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
package org.jetbrains.osgi.project

import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import org.jetbrains.osgi.jps.model.ManifestGenerationMode
import org.osmorc.LightOsgiFixtureTestCase
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BundleManifestCacheTest : LightOsgiFixtureTestCase() {
  private lateinit var myCache: BundleManifestCache

  override fun setUp() {
    super.setUp()
    myCache = BundleManifestCache.getInstance(project)
  }


  fun testLibManifest() {
    assertManifest(myCache.getManifest(findClass("org.osgi.framework.launch.FrameworkFactory")))
    assertManifest(myCache.getManifest(findClass("org.apache.felix.framework.FrameworkFactory")))
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
    assertManifest(myCache.getManifest(myModule))
  }

  fun testModuleGenerated() {
    myConfiguration.manifestGenerationMode = ManifestGenerationMode.OsmorcControlled
    myConfiguration.setBundleSymbolicName("test")
    assertManifest(myCache.getManifest(myModule))
  }

  fun testModuleBnd() {
    myConfiguration.manifestGenerationMode = ManifestGenerationMode.Bnd
    myFixture.addFileToProject(myConfiguration.bndFileLocation, "Bundle-SymbolicName: test\n")
    assertManifest(myCache.getManifest(myModule))
  }

  fun testModuleBndImplicitBSN() {
    myConfiguration.manifestGenerationMode = ManifestGenerationMode.Bnd
    myFixture.addFileToProject(myConfiguration.bndFileLocation, "Bundle-Version: 1.0.0\n")
    assertManifest(myCache.getManifest(myModule))
  }

  fun testModuleCacheUpdate() {
    myConfiguration.manifestGenerationMode = ManifestGenerationMode.OsmorcControlled
    assertNotNull(myCache.getManifest(myModule))

    myConfiguration.manifestGenerationMode = ManifestGenerationMode.Bnd
    assertNull(myCache.getManifest(myModule))

    myFixture.addFileToProject(myConfiguration.bndFileLocation, "Bundle-SymbolicName: test\n")
    assertNotNull(myCache.getManifest(myModule))
  }


  private fun findClass(className: String): PsiClass =
    JavaPsiFacade.getInstance(project).findClass(className, myModule.moduleWithLibrariesScope)!!

  private fun assertManifest(manifest: BundleManifest?) {
    assertNotNull(manifest)
    assertTrue(StringUtil.isNotEmpty(manifest!!.bundleSymbolicName))
  }
}