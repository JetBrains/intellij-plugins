package com.intellij.javascript.flex.maven;

import com.intellij.lang.javascript.flex.FlexFacet;
import com.intellij.lang.javascript.flex.FlexFacetConfiguration;
import com.intellij.lang.javascript.flex.FlexFacetType;
import com.intellij.lang.javascript.flex.build.FlexBuildConfiguration;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.importing.FacetImporter;
import org.jetbrains.idea.maven.importing.MavenExtraArtifactType;
import org.jetbrains.idea.maven.importing.MavenModifiableModelsProvider;
import org.jetbrains.idea.maven.importing.MavenRootModelAdapter;
import org.jetbrains.idea.maven.model.MavenArtifact;
import org.jetbrains.idea.maven.project.*;

import java.util.*;

public abstract class FlexFacetImporter extends FacetImporter<FlexFacet, FlexFacetConfiguration, FlexFacetType> {
  public static final String FLEX_FACET_DEFAULT_NAME = "Flex";

  private static final List<String> DEPENDENCY_TYPES_FOR_IMPORT = Arrays.asList("swf", "swc", "resource-bundle", "rb.swc");
  private static final List<String> DEPENDENCY_TYPES_FOR_COMPLETION = Arrays.asList("swf", "swc", "resource-bundle", "rb.swc");

  public FlexFacetImporter(String pluginGroupID, String pluginArtifactID) {
    super(pluginGroupID, pluginArtifactID, FlexFacetType.getInstance(), FLEX_FACET_DEFAULT_NAME);
  }

  public boolean isApplicable(MavenProject project) {
    String packaging = project.getPackaging();
    return ("swf".equalsIgnoreCase(packaging) || "swc".equalsIgnoreCase(packaging)) && super.isApplicable(project);
  }

  @Override
  public void getSupportedPackagings(Collection<String> result) {
    Collections.addAll(result, "swf", "swc");
  }

  @Override
  public void getSupportedDependencyTypes(Collection<String> result, SupportedRequestType type) {
    result.addAll(type == SupportedRequestType.FOR_COMPLETION ? DEPENDENCY_TYPES_FOR_COMPLETION : DEPENDENCY_TYPES_FOR_IMPORT);
  }

  @Override
  public void getSupportedDependencyScopes(Collection<String> result) {
    Collections.addAll(result, "merged", "internal", "external", "caching", "rsl");
  }

  @Override
  @Nullable
  public Pair<String, String> getExtraArtifactClassifierAndExtension(MavenArtifact artifact, MavenExtraArtifactType type) {
    if (!DEPENDENCY_TYPES_FOR_IMPORT.contains(artifact.getType())) return null;
    if (type == MavenExtraArtifactType.DOCS) return Pair.create("asdoc", "zip");
    return null;
  }

  @Override
  protected void setupFacet(FlexFacet f, MavenProject p) {
    configFlexFacet(FlexBuildConfiguration.getInstance(f));
  }

  protected void configFlexFacet(FlexBuildConfiguration config) {
    config.DO_BUILD = true;
    config.STATIC_LINK_RUNTIME_SHARED_LIBRARIES = false;
  }

  @Override
  protected void reimportFacet(MavenModifiableModelsProvider modelsProvider,
                               Module module,
                               MavenRootModelAdapter rootModel,
                               FlexFacet facet,
                               MavenProjectsTree mavenTree,
                               MavenProject project,
                               MavenProjectChanges changes,
                               Map<MavenProject, String> mavenProjectToModuleName,
                               List<MavenProjectsProcessorTask> postTasks) {
    reimportFlexFacet(project, module, FlexBuildConfiguration.getInstance(facet), modelsProvider);
  }

  protected void reimportFlexFacet(MavenProject project,
                                   Module module,
                                   FlexBuildConfiguration config,
                                   MavenModifiableModelsProvider modelsProvider) {
    config.OUTPUT_TYPE = getOutputType(project);
    config.OUTPUT_FILE_NAME = getTargetFileName(project);
    config.USE_FACET_COMPILE_OUTPUT_PATH = true;
    config.FACET_COMPILE_OUTPUT_PATH = getTargetOutputPath(project);
    config.FACET_COMPILE_OUTPUT_PATH_FOR_TESTS = FileUtil.toSystemIndependentName(project.getTestOutputDirectory());
  }

  protected String getOutputType(MavenProject project) {
    return isApplication(project) ? FlexBuildConfiguration.APPLICATION : FlexBuildConfiguration.LIBRARY;
  }

  protected boolean isApplication(MavenProject project) {
    return "swf".equalsIgnoreCase(project.getPackaging());
  }
}
