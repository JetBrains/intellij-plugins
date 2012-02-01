package com.intellij.javascript.flex.maven;

import com.intellij.lang.javascript.flex.FlexFacet;
import com.intellij.lang.javascript.flex.build.FlexCompilerProjectConfiguration;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.IgnoredBeanFactory;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsProcessorTask;
import org.jetbrains.idea.maven.project.MavenProjectsTree;

import java.util.List;

public class FlexMojos4FacetImporter extends FlexMojos3FacetImporter {
  @Override
  protected boolean isApplicable(char majorVersion) {
    return majorVersion >= '4';
  }

  @Override
  protected String getCompilerConfigXmlSuffix() {
    return "-configs.xml";
  }

  @Nullable
  protected Element getLocalesElement(MavenProject mavenProject, boolean compiled) {
    return getConfig(mavenProject, "locales" + (compiled ? "Compiled" : "Runtime"));
  }

  @Override
  protected void addGenerateFlexConfigTask(List<MavenProjectsProcessorTask> postTasks, FlexFacet facet,
                                           MavenProject mavenProject, MavenProjectsTree mavenTree) {
    final Project project = facet.getModule().getProject();
    if (!FlexCompilerProjectConfiguration.getInstance(project).GENERATE_FLEXMOJOS_CONFIGS) {
      return;
    }

    Flexmojos4GenerateConfigTask existingTask = null;
    for (MavenProjectsProcessorTask postTask : postTasks) {
      if (postTask instanceof Flexmojos4GenerateConfigTask) {
        existingTask = (Flexmojos4GenerateConfigTask)postTask;
        break;
      }
    }

    if (existingTask == null) {
      ChangeListManager.getInstance(project)
        .addFilesToIgnore(IgnoredBeanFactory.ignoreUnderDirectory(getCompilerConfigsDir(project), project));
      existingTask = new Flexmojos4GenerateConfigTask(mavenTree);
      postTasks.add(existingTask);
    }

    existingTask.submit(mavenProject);
  }
  
  public static String getCompilerConfigsDir(Project project) {
    //noinspection ConstantConditions
    return project.getBaseDir().getPath() + "/.idea/flexmojos";
  }

  @Override
  protected boolean isGenerateFlexConfigFilesForMxModules() {
    return false;
  }

  @Override
  protected String getCompilerConfigFile(Project project, MavenProject mavenProject, String suffix) {
    return getCompilerConfigsDir(project) + "/" + mavenProject.getMavenId().getArtifactId() + "-" +
           mavenProject.getMavenId().getGroupId() + suffix + ".xml";
  }
}