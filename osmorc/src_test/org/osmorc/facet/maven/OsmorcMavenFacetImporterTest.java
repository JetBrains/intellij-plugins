package org.osmorc.facet.maven;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.importing.FacetImporter;
import org.jetbrains.idea.maven.importing.FacetImporterTestCase;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.facet.OsmorcFacetConfiguration;
import org.osmorc.facet.OsmorcFacetType;
import org.osmorc.maven.facet.OsmorcFacetImporter;

import java.io.IOException;

/**
 * Test case for testing the {@link org.osmorc.maven.facet.OsmorcFacetImporter}.
 */
public class OsmorcMavenFacetImporterTest extends FacetImporterTestCase<OsmorcFacet, OsmorcFacetType> {
  @Override
  protected FacetImporter<OsmorcFacet, ?, OsmorcFacetType> createImporter() {
    return new OsmorcFacetImporter();
  }

  public void testSimpleImport() throws IOException {
    importProject(pomContents("simple", "", ""));
    assertModules("simple");
    OsmorcFacetConfiguration configuration = assertConfiguration(getFacet("simple"));
    assertEquals("1.0.0", configuration.getBundleVersion());
    assertTrue(configuration.isOsmorcControlsManifest());
  }

  public void testSymbolicNameInference() throws IOException {
    importProject(pomContents("osmorc-simple", "", ""));
    assertModules("osmorc-simple");
    OsmorcFacetConfiguration configuration = assertConfiguration(getFacet("osmorc-simple"));
    assertEquals("1.0.0", configuration.getBundleVersion());
    assertTrue(configuration.isOsmorcControlsManifest());
  }

  public void testExplicitBundleVersion() throws IOException {
    String instructions = "<configuration><instructions><Bundle-Version>1.2.3</Bundle-Version></instructions></configuration>";
    importProject(pomContents("simple", instructions, ""));
    assertModules("simple");
    OsmorcFacetConfiguration configuration = assertConfiguration(getFacet("simple"));
    assertEquals("1.2.3", configuration.getBundleVersion());
  }

  public void testUseExistingManifest() throws IOException {
    String instructions =
      "<configuration><instructions><_include>${project.basedir}/foo/Manifest.MF</_include></instructions></configuration>";
    importProject(pomContents("simple", instructions, ""));
    assertModules("simple");
    OsmorcFacetConfiguration configuration = assertConfiguration(getFacet("simple"));
    assertFalse(configuration.isUseProjectDefaultManifestFileLocation());
    assertTrue(configuration.getManifestLocation().endsWith("/foo/Manifest.MF"));
  }

  public void testDefaultOutputPath() throws IOException {
    String instructions =
      "<configuration><instructions><instructions.Bundle-Version>2.0.0</instructions.Bundle-Version></instructions></configuration>";
    importProject(pomContents("simple", instructions, ""));
    assertModules("simple");
    OsmorcFacetConfiguration configuration = assertConfiguration(getFacet("simple"));
    assertTrue(configuration.getJarFileLocation().endsWith("simple-1.0.1.jar"));
  }

  public void testSpecificOutputPath() throws IOException {
    importProject(pomContents("simple", "", "<finalName>${artifactId}-special-${version}</finalName>"));
    assertModules("simple");
    OsmorcFacetConfiguration configuration = assertConfiguration(getFacet("simple"));
    assertTrue(configuration.getJarFileLocation().endsWith("simple-special-1.0.1.jar"));
  }

  private static String pomContents(@NotNull String artifactId, @NotNull String pluginInstructions, @NotNull String buildInstructions) {
    return "<groupId>org.osmorc</groupId>\n" +
           "<artifactId>" + artifactId + "</artifactId>\n" +
           "<version>1.0.1</version>\n" +
           "<packaging>bundle</packaging>\n" +
           "<build>\n" +
           (buildInstructions.isEmpty() ? "" : "  " + buildInstructions + "\n") +
           "  <plugins>\n" +
           "    <plugin>\n" +
           "      <groupId>org.apache.felix</groupId>\n" +
           "      <artifactId>maven-bundle-plugin</artifactId>\n" +
           "      <version>2.3.4</version>\n" +
           (pluginInstructions.isEmpty() ? "" : "      " + pluginInstructions + "\n") +
           "    </plugin>\n" +
           "  </plugins>\n" +
           "</build>";
  }

  private static OsmorcFacetConfiguration assertConfiguration(OsmorcFacet facet) {
    OsmorcFacetConfiguration configuration = facet.getConfiguration();
    assertNotNull(configuration);
    assertEquals("org.osmorc.simple", configuration.getBundleSymbolicName());
    return configuration;
  }
}
