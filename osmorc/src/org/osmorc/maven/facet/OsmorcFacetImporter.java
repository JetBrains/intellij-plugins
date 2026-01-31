/*
 * Copyright (c) 2007-2009, Osmorc Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright notice, this list
 *       of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this
 *       list of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *     * Neither the name of 'Osmorc Development Team' nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without specific
 *       prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.osmorc.maven.facet;

import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.text.StringUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.importing.FacetImporter;
import org.jetbrains.idea.maven.importing.MavenRootModelAdapter;
import org.jetbrains.idea.maven.model.MavenId;
import org.jetbrains.idea.maven.model.MavenPlugin;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectChanges;
import org.jetbrains.idea.maven.project.MavenProjectsProcessorTask;
import org.jetbrains.idea.maven.project.MavenProjectsTree;
import org.jetbrains.idea.maven.project.SupportedRequestType;
import org.jetbrains.idea.maven.utils.MavenJDOMUtil;
import org.jetbrains.osgi.jps.model.ManifestGenerationMode;
import org.jetbrains.osgi.jps.model.OutputPathType;
import org.osgi.framework.Constants;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.facet.OsmorcFacetConfiguration;
import org.osmorc.facet.OsmorcFacetType;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The OsmorcFacetImporter reads Maven metadata and import OSGi-specific settings as an Osmorc facet.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thomä</a>
 */
public final class OsmorcFacetImporter extends FacetImporter<OsmorcFacet, OsmorcFacetConfiguration, OsmorcFacetType> {
  private static final String INCLUDE_MANIFEST = "_include";

  public OsmorcFacetImporter() {
    super("org.apache.felix", "maven-bundle-plugin", OsmorcFacetType.getInstance());
  }

  @Override
  public boolean isApplicable(MavenProject mavenProject) {
    return super.isApplicable(mavenProject) && "bundle".equals(mavenProject.getPackaging());
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

    // first off, we get the defaults
    MavenId id = mavenProject.getMavenId();
    conf.setBundleSymbolicName(id.getGroupId() + "." + id.getArtifactId());
    conf.setBundleVersion(ImporterUtil.cleanupVersion(id.getVersion()));

    MavenPlugin plugin = mavenProject.findPlugin(myPluginGroupID, myPluginArtifactID);
    if (plugin == null) {
      return;
    }

    // Check if there are any overrides set up in the maven plugin settings
    conf.setBundleSymbolicName(computeSymbolicName(mavenProject)); // IDEA-63243

    Map<String, String> props = new LinkedHashMap<>();  // to preserve the order of elements
    Map<String, String> modelMap = mavenProject.getModelMap();

    String description = modelMap.get("description");
    if (!StringUtil.isEmptyOrSpaces(description)) {
      props.put(Constants.BUNDLE_DESCRIPTION, description);
    }

    String licenses = modelMap.get("licenses");
    if (!StringUtil.isEmptyOrSpaces(licenses)) {
      props.put("Bundle-License", licenses);
    }

    String vendor = modelMap.get("organization.name");
    if (!StringUtil.isEmpty(vendor)) {
      props.put(Constants.BUNDLE_VENDOR, vendor);
    }

    String docUrl = modelMap.get("organization.url");
    if (!StringUtil.isEmptyOrSpaces(docUrl)) {
      props.put(Constants.BUNDLE_DOCURL, docUrl);
    }

    // load versions if any
    Map<String, String> versions = cleanVersions(plugin);

    // now find any additional properties that might have been set up:
    Element instructionsNode = getConfig(mavenProject, "instructions");
    if (instructionsNode != null) {
      boolean useExistingManifest = false;

      for (Element child : instructionsNode.getChildren()) {
        String name = child.getName();
        String value = child.getTextTrim();

        value = value.replaceAll("\\p{Blank}*[\r\n]\\p{Blank}*", "");
        value = substituteVersions(value, versions);

        if (INCLUDE_MANIFEST.equals(name)) {
          conf.setManifestLocation(value);
          conf.setManifestGenerationMode(ManifestGenerationMode.Manually);
          conf.setUseProjectDefaultManifestFileLocation(false);
          useExistingManifest = true;
        }
        else if (Constants.BUNDLE_VERSION.equals(name)) {
          conf.setBundleVersion(value);
        }
        else if (Constants.BUNDLE_ACTIVATOR.equals(name)) {
          conf.setBundleActivator(value);
        }
        else if (!StringUtil.isEmpty(value) && !Constants.BUNDLE_SYMBOLICNAME.equals(name)) {
          if (StringUtil.startsWithChar(name, '_')) {
            name = "-" + name.substring(1);  // sanitize instructions
          }

          props.put(name, value);
        }
      }

      if (!useExistingManifest) {
        conf.setManifestLocation("");
        conf.setManifestGenerationMode(ManifestGenerationMode.OsmorcControlled);
        conf.setUseProjectDefaultManifestFileLocation(true);
      }
    }

    // check if bundle name exists, if not compute it (IDEA-63244)
    if (!props.containsKey(Constants.BUNDLE_NAME)) {
      props.put(Constants.BUNDLE_NAME, computeBundleName(mavenProject));
    }

    // now post-process the settings, to make Embed-Dependency work
    ImporterUtil.postProcessAdditionalProperties(props, mavenProject, module.getProject());

    // Fix for IDEA-63242 - don't merge it with the existing settings, overwrite them
    conf.importAdditionalProperties(props, true);

    // Fix for IDEA-66235 - inherit jar filename from maven
    String jarFileName = mavenProject.getFinalName() + ".jar";

    // FiX for IDEA-67088, preserve existing output path settings on reimport.
    switch (conf.getOutputPathType()) {
      case OsgiOutputPath -> conf.setJarFileLocation(jarFileName, OutputPathType.OsgiOutputPath);
      case SpecificOutputPath -> {
        String path = new File(conf.getJarFilePath(), jarFileName).getPath();
        conf.setJarFileLocation(path, OutputPathType.SpecificOutputPath);
      }
      case CompilerOutputPath -> conf.setJarFileLocation(jarFileName, OutputPathType.CompilerOutputPath);
    }
  }

  /**
   * Computes the Bundle-Name value from the data given in the maven project.
   */
  private @NotNull String computeBundleName(MavenProject mavenProject) {
    String bundleName = findConfigValue(mavenProject, "instructions." + Constants.BUNDLE_NAME);
    if (!StringUtil.isEmpty(bundleName)) {
      return bundleName;
    }

    String projectName = mavenProject.getName();
    if (!StringUtil.isEmpty(projectName)) {
      return projectName;
    }

    String artifactId = mavenProject.getMavenId().getArtifactId();
    if (!StringUtil.isEmpty(artifactId)) {
      return artifactId;
    }

    return computeSymbolicName(mavenProject);
  }

  /**
   * Computes the Bundle-SymbolicName value from the data given in the maven project.
   */
  private @NotNull String computeSymbolicName(MavenProject mavenProject) {
    String bundleSymbolicName = findConfigValue(mavenProject, "instructions." + Constants.BUNDLE_SYMBOLICNAME);
    if (!StringUtil.isEmpty(bundleSymbolicName)) {
      return bundleSymbolicName;
    }

    MavenId mavenId = mavenProject.getMavenId();
    String groupId = mavenId.getGroupId();
    String artifactId = mavenId.getArtifactId();
    if (groupId == null || artifactId == null) {
      return "";
    }

    // if artifactId is equal to last section of groupId then groupId is returned (org.apache.maven:maven -> org.apache.maven)
    String lastSectionOfGroupId = groupId.substring(groupId.lastIndexOf(".") + 1);
    if (lastSectionOfGroupId.endsWith(artifactId)) {
      return groupId;
    }

    // if artifactId starts with last section of groupId that portion is removed (org.apache.maven:maven-core -> org.apache.maven.core)
    String doubledNamePart = lastSectionOfGroupId + "-";
    if (artifactId.startsWith(doubledNamePart) && artifactId.length() > doubledNamePart.length()) {
      return groupId + "." + artifactId.substring(doubledNamePart.length());
    }

    return groupId + "." + artifactId;
  }

  private static @Nullable Map<String, String> cleanVersions(MavenPlugin plugin) {
    Element versionsNode = MavenJDOMUtil.findChildByPath(plugin.getGoalConfiguration("cleanVersions"), "versions");
    if (versionsNode == null) return null;

    Map<String, String> versions = new HashMap<>();
    for (Element child : versionsNode.getChildren()) {
      String name = child.getName();
      String value = child.getValue();
      if (!StringUtil.isEmpty(value)) {
        versions.put(name, ImporterUtil.cleanupVersion(value));
      }
    }
    return versions;
  }

  private static @NotNull String substituteVersions(@NotNull String value, @Nullable Map<String, String> versions) {
    if (versions != null) {
      for (Map.Entry<String, String> entry : versions.entrySet()) {
        String property = "${" + entry.getKey() + "}";
        value = StringUtil.replace(value, property, entry.getValue());
      }
    }
    return value;
  }

  @Override
  public void getSupportedDependencyTypes(Collection<? super String> result, SupportedRequestType type) {
    result.add("bundle");
  }
}
