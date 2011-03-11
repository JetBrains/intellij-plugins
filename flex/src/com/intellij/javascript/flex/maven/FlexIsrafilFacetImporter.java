package com.intellij.javascript.flex.maven;

import com.intellij.lang.javascript.flex.FlexFacet;
import com.intellij.lang.javascript.flex.sdk.FlexSdkType;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.projectRoots.Sdk;
import org.jetbrains.idea.maven.importing.MavenModifiableModelsProvider;
import org.jetbrains.idea.maven.importing.MavenRootModelAdapter;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsProcessorTask;
import org.jetbrains.idea.maven.project.MavenProjectsTree;
import org.jetbrains.idea.maven.project.MavenProjectChanges;

import java.util.List;
import java.util.Map;

public class FlexIsrafilFacetImporter extends FlexFacetImporter {
  public FlexIsrafilFacetImporter() {
    super("net.israfil.mojo", "maven-flex2-plugin");
  }

  @Override
  protected void reimportFacet(MavenModifiableModelsProvider modelsProvider,
                               Module module,
                               MavenRootModelAdapter rootModel,
                               FlexFacet facet,
                               MavenProjectsTree mavenTreel,
                               MavenProject project,
                               MavenProjectChanges changes,
                               Map<MavenProject, String> mavenProjectToModuleName,
                               List<MavenProjectsProcessorTask> postTasks) {
    super.reimportFacet(modelsProvider, module, rootModel, facet, mavenTreel, project, changes, mavenProjectToModuleName, postTasks);

    Sdk flexSdk = FlexSdkUtils.createOrGetSdk(FlexSdkType.getInstance(),
                                              getFlexPath(project));
    facet.getConfiguration().setFlexSdk(flexSdk, rootModel.getRootModel());
  }

  private String getFlexPath(MavenProject mavenProject) {
    String path = findConfigValue(mavenProject, "flexHome");
    if (path == null) path = mavenProject.getProperties().getProperty("flex.home", null);
    if (path == null) return null;
    return FileUtil.toSystemIndependentName(path);
  }
}
