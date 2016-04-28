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
package org.osmorc.maven

import com.intellij.facet.FacetTypeId
import org.jetbrains.idea.maven.importing.FacetImporterTestCase
import org.osgi.framework.Constants
import org.osmorc.facet.OsmorcFacet
import org.osmorc.facet.OsmorcFacetConfiguration
import org.osmorc.facet.OsmorcFacetType

class OsgiMavenImporterTest : FacetImporterTestCase<OsmorcFacet>() {
  override fun getFacetTypeId(): FacetTypeId<OsmorcFacet> = OsmorcFacetType.ID


  fun testSimpleImport() {
    importProject(pomContents("simple", "", ""))
    assertModules("simple")
    val configuration = assertConfiguration("simple")
    assertTrue(configuration.isOsmorcControlsManifest())
    assertEquals("1.0.1", configuration.getBundleVersion())
  }

  fun testWrongPackaging() {
    importProject(pomContents("simple", "", "").replace("<packaging>bundle</packaging>", "<packaging>pom</packaging>"))
    assertModules("simple")
    assertNull(findFacet("simple"))
  }

  fun testSymbolicNameInference() {
    importProject(pomContents("osmorc-simple", "", ""))
    assertModules("osmorc-simple")
    val configuration = assertConfiguration("osmorc-simple")
    assertTrue(configuration.isOsmorcControlsManifest())
    assertEquals("1.0.1", configuration.getBundleVersion())
  }

  fun testExplicitBundleVersion() {
    val instructions = "<configuration><instructions><Bundle-Version>1.2.3</Bundle-Version></instructions></configuration>"
    importProject(pomContents("simple", instructions, ""))
    assertModules("simple")
    val configuration = assertConfiguration("simple")
    assertTrue(configuration.isOsmorcControlsManifest())
    assertEquals("1.2.3", configuration.getBundleVersion())
  }

  fun testUseExistingManifest() {
    val instructions = "<configuration><instructions><_include>\${project.basedir}/foo/Manifest.MF</_include></instructions></configuration>"
    importProject(pomContents("simple", instructions, ""))
    assertModules("simple")
    val configuration = assertConfiguration("simple")
    assertTrue(configuration.isManifestManuallyEdited())
    assertFalse(configuration.isUseProjectDefaultManifestFileLocation())
    assertTrue(configuration.getManifestLocation().endsWith("/foo/Manifest.MF"))
  }

  fun testDefaultOutputPath() {
    val instructions = "<configuration><instructions><Bundle-Version>2.0.0</Bundle-Version></instructions></configuration>"
    importProject(pomContents("simple", instructions, ""))
    assertModules("simple")
    val configuration = assertConfiguration("simple")
    assertTrue(configuration.isOsmorcControlsManifest())
    assertTrue(configuration.getJarFileLocation().endsWith("simple-1.0.1.jar"))
  }

  fun testSpecificOutputPath() {
    importProject(pomContents("simple", "", "<finalName>\${artifactId}-special-\${version}</finalName>"))
    assertModules("simple")
    val configuration = assertConfiguration("simple")
    assertTrue(configuration.isOsmorcControlsManifest())
    assertTrue(configuration.getJarFileLocation().endsWith("simple-special-1.0.1.jar"))
  }

  fun testVersionCleanerSupport() {
    val instructions =
        "<configuration><instructions><Bundle-Version>\${my.version.clean}</Bundle-Version></instructions></configuration>" +
        "<executions><execution>" +
        "  <id>versions</id><goals><goal>cleanVersions</goal></goals>" +
        "  <configuration><versions><my.version.clean>\${project.version}</my.version.clean></versions></configuration>" +
        "</execution></executions>"
    importProject(pomContents("simple", instructions, ""))
    assertModules("simple")
    val configuration = assertConfiguration("simple")
    assertTrue(configuration.isOsmorcControlsManifest())
    assertEquals("1.0.1", configuration.getBundleVersion())
  }

  fun testDescription() {
    importProject("<description>bundle description</description>\n" + pomContents("simple", "", ""))
    assertModules("simple")
    val configuration = assertConfiguration("simple")
    assertTrue(configuration.isOsmorcControlsManifest())
    assertEquals("bundle description", configuration.getAdditionalPropertiesAsMap()[Constants.BUNDLE_DESCRIPTION])
  }

  fun testExplicitProjectName() {
    importProject("<name>explicit</name>\n" + pomContents("simple", "", ""))
    assertModules("simple")
    val configuration = assertConfiguration("simple", "explicit")
    assertTrue(configuration.isOsmorcControlsManifest())
  }


  private fun pomContents(artifactId: String, pluginConfig: String, buildConfig: String): String = """
    <groupId>org.osmorc</groupId>
    <artifactId>${artifactId}</artifactId>
    <version>1.0.1</version>
    <packaging>bundle</packaging>
    <build>
      ${buildConfig}
      <plugins>
        <plugin>
          <groupId>org.apache.felix</groupId>
          <artifactId>maven-bundle-plugin</artifactId>
          <version>2.5.3</version>
          ${pluginConfig}
        </plugin>
      </plugins>
    </build>"""

  private fun assertConfiguration(name: String): OsmorcFacetConfiguration {
    return assertConfiguration(name, name)
  }

  private fun assertConfiguration(moduleName: String, bundleName: String): OsmorcFacetConfiguration {
    val facet = getFacet(moduleName)
    val configuration = facet.getConfiguration()
    assertNotNull(configuration)
    assertEquals(bundleName, configuration.getAdditionalPropertiesAsMap().get(Constants.BUNDLE_NAME))
    assertEquals("org.osmorc.simple", configuration.getBundleSymbolicName())
    return configuration
  }
}
