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
package org.osmorc.maven;

import com.intellij.facet.FacetTypeId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.importing.FacetImporterTestCase;
import org.osgi.framework.Constants;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.facet.OsmorcFacetConfiguration;
import org.osmorc.facet.OsmorcFacetType;

import java.io.IOException;

/**
 * Test case for testing the {@link org.osmorc.maven.facet.OsmorcFacetImporter}.
 */
public class OsmorcMavenFacetImporterTest extends FacetImporterTestCase<OsmorcFacet> {
  @Override
  protected FacetTypeId<OsmorcFacet> getFacetTypeId() {
    return OsmorcFacetType.ID;
  }

  public void testSimpleImport() throws IOException {
    importProject(pomContents("simple", "", ""));
    assertModules("simple");
    OsmorcFacetConfiguration configuration = assertConfiguration("simple");
    assertEquals("1.0.1", configuration.getBundleVersion());
    assertTrue(configuration.isOsmorcControlsManifest());
  }

  public void testWrongPackaging() throws IOException {
    importProject(pomContents("simple", "", "").replace("<packaging>bundle</packaging>", "<packaging>pom</packaging>"));
    assertModules("simple");
    assertNull(findFacet("simple"));
  }

  public void testSymbolicNameInference() throws IOException {
    importProject(pomContents("osmorc-simple", "", ""));
    assertModules("osmorc-simple");
    OsmorcFacetConfiguration configuration = assertConfiguration("osmorc-simple");
    assertEquals("1.0.1", configuration.getBundleVersion());
    assertTrue(configuration.isOsmorcControlsManifest());
  }

  public void testExplicitBundleVersion() throws IOException {
    String instructions = "<configuration><instructions><Bundle-Version>1.2.3</Bundle-Version></instructions></configuration>";
    importProject(pomContents("simple", instructions, ""));
    assertModules("simple");
    OsmorcFacetConfiguration configuration = assertConfiguration("simple");
    assertEquals("1.2.3", configuration.getBundleVersion());
  }

  public void testUseExistingManifest() throws IOException {
    String instructions =
      "<configuration><instructions><_include>${project.basedir}/foo/Manifest.MF</_include></instructions></configuration>";
    importProject(pomContents("simple", instructions, ""));
    assertModules("simple");
    OsmorcFacetConfiguration configuration = assertConfiguration("simple");
    assertFalse(configuration.isUseProjectDefaultManifestFileLocation());
    assertTrue(configuration.getManifestLocation().endsWith("/foo/Manifest.MF"));
  }

  public void testDefaultOutputPath() throws IOException {
    String instructions =
      "<configuration><instructions><Bundle-Version>2.0.0</Bundle-Version></instructions></configuration>";
    importProject(pomContents("simple", instructions, ""));
    assertModules("simple");
    OsmorcFacetConfiguration configuration = assertConfiguration("simple");
    assertTrue(configuration.getJarFileLocation().endsWith("simple-1.0.1.jar"));
  }

  public void testSpecificOutputPath() throws IOException {
    importProject(pomContents("simple", "", "<finalName>${artifactId}-special-${version}</finalName>"));
    assertModules("simple");
    OsmorcFacetConfiguration configuration = assertConfiguration("simple");
    assertTrue(configuration.getJarFileLocation().endsWith("simple-special-1.0.1.jar"));
  }

  public void testVersionCleanerSupport() throws IOException {
    String instructions =
      "<configuration><instructions><Bundle-Version>${my.version.clean}</Bundle-Version></instructions></configuration>" +
      "<executions><execution>" +
      "  <id>versions</id><goals><goal>cleanVersions</goal></goals>" +
      "  <configuration><versions><my.version.clean>${project.version}</my.version.clean></versions></configuration>" +
      "</execution></executions>";
    importProject(pomContents("simple", instructions, ""));
    assertModules("simple");
    OsmorcFacetConfiguration configuration = assertConfiguration("simple");
    assertEquals("1.0.1", configuration.getBundleVersion());
  }

  public void testExplicitProjectName() throws IOException {
    importProject("<name>explicit</name>\n" + pomContents("simple", "", ""));
    assertModules("simple");
    assertConfiguration("simple", "explicit");
  }

  private static String pomContents(@NotNull String artifactId, @NotNull String pluginConfig, @NotNull String buildConfig) {
    return "<groupId>org.osmorc</groupId>\n" +
           "<artifactId>" + artifactId + "</artifactId>\n" +
           "<version>1.0.1</version>\n" +
           "<packaging>bundle</packaging>\n" +
           "<build>\n" +
           (buildConfig.isEmpty() ? "" : "  " + buildConfig + "\n") +
           "  <plugins>\n" +
           "    <plugin>\n" +
           "      <groupId>org.apache.felix</groupId>\n" +
           "      <artifactId>maven-bundle-plugin</artifactId>\n" +
           "      <version>2.5.3</version>\n" +
           (pluginConfig.isEmpty() ? "" : "      " + pluginConfig + "\n") +
           "    </plugin>\n" +
           "  </plugins>\n" +
           "</build>";
  }

  private OsmorcFacetConfiguration assertConfiguration(String name) {
    return assertConfiguration(name, name);
  }

  private OsmorcFacetConfiguration assertConfiguration(String moduleName, String bundleName) {
    OsmorcFacet facet = getFacet(moduleName);
    OsmorcFacetConfiguration configuration = facet.getConfiguration();
    assertNotNull(configuration);
    assertEquals(bundleName, configuration.getAdditionalPropertiesAsMap().get(Constants.BUNDLE_NAME));
    assertEquals("org.osmorc.simple", configuration.getBundleSymbolicName());
    return configuration;
  }
}
