package com.intellij.javascript.flex.maven;

import com.intellij.lang.javascript.flex.FlexFacet;
import com.intellij.lang.javascript.flex.build.FlexCompilerProjectConfiguration;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.IgnoredBeanFactory;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.importing.MavenModifiableModelsProvider;
import org.jetbrains.idea.maven.project.*;

import java.util.List;

public class FlexMojos4FacetImporter extends FlexMojos3FacetImporter {
  private int projectsToImportCount;

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
  public void preProcess(Module module,
                         MavenProject mavenProject,
                         MavenProjectChanges changes,
                         MavenModifiableModelsProvider modifiableModelsProvider) {
    super.preProcess(module, mavenProject, changes, modifiableModelsProvider);

    if (FlexCompilerProjectConfiguration.getInstance(module.getProject()).GENERATE_FLEXMOJOS_CONFIGS) {
      projectsToImportCount++;
    }
  }

  @Override
  protected void addGenerateFlexConfigTask(List<MavenProjectsProcessorTask> postTasks, FlexFacet facet,
                                           MavenProject mavenProject, MavenProjectsTree mavenTree) {
    final Project project = facet.getModule().getProject();
    if (FlexCompilerProjectConfiguration.getInstance(project).GENERATE_FLEXMOJOS_CONFIGS) {
      Flexmojos4GenerateFlexConfigTask generateFlexConfigTask = null;
      for (MavenProjectsProcessorTask postTask : postTasks) {
        if (postTask instanceof Flexmojos4GenerateFlexConfigTask) {
          generateFlexConfigTask = (Flexmojos4GenerateFlexConfigTask)postTask;
          break;
        }
      }

      if (generateFlexConfigTask == null) {
        try {
          ChangeListManager.getInstance(project)
            .addFilesToIgnore(IgnoredBeanFactory.ignoreUnderDirectory(getCompilerConfigsDir(project), project));
          generateFlexConfigTask = new Flexmojos4GenerateFlexConfigTask(mavenProject, mavenTree, projectsToImportCount);
          postTasks.add(generateFlexConfigTask);
        }
        finally {
          projectsToImportCount = 0;
        }
      }

      generateFlexConfigTask.generate(mavenProject);
    }
  }
  
  private static String getCompilerConfigsDir(Project project) {
    //noinspection ConstantConditions
    return project.getBaseDir().getPath() + "/.idea/flexmojos";
  }

  @Override
  protected boolean isGenerateFlexConfigFilesForMxModules() {
    return false;
  }

  @Override
  protected String getCompilerConfigFile(Module module, MavenProject mavenProject, String suffix) {
    return getCompilerConfigsDir(module.getProject()) + "/" + mavenProject.getMavenId().getArtifactId() + "-" +
           mavenProject.getMavenId().getGroupId() + suffix + "-config.xml";
  }
}