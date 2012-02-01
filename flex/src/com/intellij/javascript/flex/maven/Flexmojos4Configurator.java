package com.intellij.javascript.flex.maven;

import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexProjectConfigurationEditor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.IgnoredBeanFactory;
import org.jetbrains.idea.maven.importing.MavenModifiableModelsProvider;
import org.jetbrains.idea.maven.model.MavenPlugin;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsProcessorTask;
import org.jetbrains.idea.maven.project.MavenProjectsTree;

import java.util.List;

public class Flexmojos4Configurator extends Flexmojos3Configurator {
  public Flexmojos4Configurator(final Module module,
                                final MavenModifiableModelsProvider modifiableModelsProvider,
                                final FlexProjectConfigurationEditor flexEditor,
                                final MavenProjectsTree mavenTree, final MavenProject mavenProject,
                                final MavenPlugin flexmojosPlugin,
                                final List<String> compiledLocales,
                                final List<String> runtimeLocales,
                                final FlexConfigInformer informer) {
    super(module, modifiableModelsProvider, flexEditor, mavenTree, mavenProject, flexmojosPlugin, compiledLocales, runtimeLocales,
          informer);
  }

  public static String getCompilerConfigsDir(final Project project) {
    //noinspection ConstantConditions
    return project.getBaseDir().getPath() + "/.idea/flexmojos";
  }

  @Override
  protected String getCompilerConfigFilePath() {
    final String suffix = myClassifier == null ? "" : "-" + myClassifier;
    return getCompilerConfigsDir(myModule.getProject()) + "/" + myMavenProject.getMavenId().getArtifactId() + "-" +
           myMavenProject.getMavenId().getGroupId() + suffix + ".xml";
  }

  protected void appendGenerateConfigTask(final List<MavenProjectsProcessorTask> postTasks, final String configFilePath) {
    final Project project = myModule.getProject();

    Flexmojos4GenerateConfigTask existingTask = null;
    for (MavenProjectsProcessorTask postTask : postTasks) {
      if (postTask instanceof Flexmojos4GenerateConfigTask) {
        existingTask = (Flexmojos4GenerateConfigTask)postTask;
        break;
      }
    }

    if (existingTask == null) {
      ChangeListManager.getInstance(project).addFilesToIgnore(
        IgnoredBeanFactory.ignoreUnderDirectory(getCompilerConfigsDir(project), project));
      existingTask = new Flexmojos4GenerateConfigTask(myMavenTree);
      postTasks.add(existingTask);
    }

    existingTask.submit(myMavenProject);
  }
}
