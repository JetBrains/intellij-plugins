// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.osmorc.maven.facet;

import aQute.bnd.osgi.Constants;
import aQute.bnd.version.MavenVersion;
import aQute.lib.utf8properties.UTF8Properties;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.importing.FacetImporter;
import org.jetbrains.idea.maven.importing.MavenRootModelAdapter;
import org.jetbrains.idea.maven.model.MavenId;
import org.jetbrains.idea.maven.model.MavenPlugin;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectChanges;
import org.jetbrains.idea.maven.project.MavenProjectsProcessorTask;
import org.jetbrains.idea.maven.project.MavenProjectsTree;
import org.jetbrains.idea.maven.project.SupportedRequestType;
import org.jetbrains.osgi.jps.model.ManifestGenerationMode;
import org.jetbrains.osgi.jps.model.OutputPathType;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.facet.OsmorcFacetConfiguration;
import org.osmorc.facet.OsmorcFacetType;
import org.osmorc.i18n.OsmorcBundle;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads Maven project data and imports OSGi settings from bnd-maven-plugin as an Osmorc facet.
 * <p>
 * The generated manifest will only be similar to the Maven plugin, not the same. The Maven
 * model available here does not export everything required and the two-phase execution
 * (parsing in the facet, Bnd-execution in the JPS build) is not the same as the
 * everything-at-once in an actual Maven build.
 */
public final class OsmorcBndFacetImporter extends FacetImporter<OsmorcFacet, OsmorcFacetConfiguration, OsmorcFacetType> {
  private static final Logger LOG = Logger.getInstance(OsmorcBndFacetImporter.class);
  private static final String BND_PLUGIN_GOAL = "bnd-process";

  public OsmorcBndFacetImporter() {
    super("biz.aQute.bnd", "bnd-maven-plugin", OsmorcFacetType.getInstance());
  }

  @Override
  public boolean isApplicable(MavenProject mavenProject) {
    return super.isApplicable(mavenProject) && !"bundle".equals(mavenProject.getPackaging());
  }

  @Override
  public void getSupportedDependencyTypes(Collection<? super String> result, SupportedRequestType type) {
    result.add("bundle");
  }

  @Override
  protected void reimportFacet(@NotNull IdeModifiableModelsProvider modelsProvider, @NotNull Module module,
                               @NotNull MavenRootModelAdapter mavenRootModelAdapter, @NotNull OsmorcFacet osmorcFacet,
                               @NotNull MavenProjectsTree mavenProjectsTree, @NotNull MavenProject mavenProject,
                               @NotNull MavenProjectChanges changes, @NotNull Map<MavenProject, String> mavenProjectStringMap,
                               @NotNull List<MavenProjectsProcessorTask> mavenProjectsProcessorPostConfigurationTasks) {
    OsmorcFacetConfiguration conf = osmorcFacet.getConfiguration();
    if (conf.isDoNotSynchronizeWithMaven()) {
      return;
    }

    MavenPlugin plugin = mavenProject.findPlugin(myPluginGroupID, myPluginArtifactID);
    if (plugin == null) {
      return;
    }

    conf.setManifestGenerationMode(ManifestGenerationMode.BndMavenPlugin);
    Map<String, String> props = new LinkedHashMap<>();  // to preserve the order of elements

    // load properties from Maven plugin configuration
    try {
      String bndFile = loadProjectProperties(props, mavenProject);
      conf.setBndFileLocation(bndFile);
    }
    catch (Exception e) {
      String message = OsmorcBundle.message("maven.import.error", mavenProject.getPath(), e.getMessage());
      OsmorcBundle.bnd("", message, NotificationType.ERROR).notify(module.getProject());
    }

    // add defaults derived from Maven properties
    // developers and license are not available from the model map
    MavenId mavenId = mavenProject.getMavenId();
    Map<String, String> modelMap = mavenProject.getModelMap();
    props.putIfAbsent(Constants.BUNDLE_SYMBOLICNAME, mavenId.getArtifactId());
    addProperty(props, Constants.BUNDLE_NAME, mavenProject.getName());
    props.computeIfAbsent(Constants.BUNDLE_VERSION, k -> new MavenVersion(mavenId.getVersion()).getOSGiVersion().toString());
    addProperty(props, Constants.BUNDLE_DESCRIPTION, modelMap.get("description"));
    addProperty(props, Constants.BUNDLE_VENDOR, modelMap.get("organization.name"));
    addProperty(props, Constants.BUNDLE_SCM, modelMap.get("scm"));
    addProperty(props, Constants.BUNDLE_DOCURL, modelMap.get("url"));

    // add settings for reproducible build if Maven is configured for it
    String outputTimestamp = mavenProject.getProperties().getProperty("project.build.outputTimestamp");
    if (StringUtil.isNotEmpty(outputTimestamp)) {
      props.putIfAbsent(Constants.NOEXTRAHEADERS, "true");
      props.putIfAbsent(Constants.SNAPSHOT, "SNAPSHOT");
    }

    // Fix for IDEA-63242 - don't merge it with the existing settings, overwrite them
    conf.importAdditionalProperties(props, true);

    // Fix for IDEA-66235 - inherit jar filename from maven
    String jarFileName = mavenProject.getFinalName() + ".jar";

    // Fix for IDEA-67088, preserve existing output path settings on reimport.
    switch (conf.getOutputPathType()) {
      case OsgiOutputPath -> conf.setJarFileLocation(jarFileName, OutputPathType.OsgiOutputPath);
      case SpecificOutputPath -> {
        String path = new File(conf.getJarFilePath(), jarFileName).getPath();
        conf.setJarFileLocation(path, OutputPathType.SpecificOutputPath);
      }
      default -> conf.setJarFileLocation(jarFileName, OutputPathType.CompilerOutputPath);
    }
  }

  private static void addProperty(Map<String, String> headers, String headerName, String value) {
    if (!StringUtil.isEmpty(value)) {
      headers.putIfAbsent(headerName, value);
    }
  }

  private String loadProjectProperties(Map<String, String> props, MavenProject mavenProject) throws IOException {
    VirtualFile pomVirtualFile = mavenProject.getFile();

    // get bnd-maven-plugin configuration from <plugin>/<execution> or fallback to <plugin>
    Element configuration = getGoalConfig(mavenProject, BND_PLUGIN_GOAL);
    if (configuration == null || configuration.getContent().isEmpty()) {
      configuration = getConfig(mavenProject);
    }

    if (configuration != null) {
      // configuration in <bnd> element
      Element bndElement = configuration.getChild("bnd");
      if (bndElement != null) {
        if (LOG.isDebugEnabled()) LOG.debug("Using headers from bnd-element in " + mavenProject.getPath());
        File pomFile = new File(pomVirtualFile.getPath());
        UTF8Properties properties = new UTF8Properties();
        properties.load(bndElement.getValue(), pomFile, null);
        for (Map.Entry<?, ?> e : properties.replaceHere(pomFile.getParentFile()).entrySet()) {
          props.put(e.getKey().toString(), e.getValue().toString());
        }
      }

      // <bndfile> element
      Element bndfileElement = configuration.getChild("bndfile");
      if (bndfileElement != null) {
        String bndFile = bndfileElement.getValue();
        if (LOG.isDebugEnabled()) LOG.debug("Using bnd file from bndfile-element: " + bndFile);
        return bndFile;
      }
    }

    // no bnd file found, use properties from <bnd> element or defaults only
    return null;
  }
}
