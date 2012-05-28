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
import java.util.Map;

public class Flexmojos5Configurator extends Flexmojos4Configurator {
  public Flexmojos5Configurator(final Module module,
                                final MavenModifiableModelsProvider modifiableModelsProvider,
                                final FlexProjectConfigurationEditor flexEditor,
                                final MavenProjectsTree mavenTree,
                                final Map<MavenProject, String> mavenProjectToModuleName,
                                final MavenProject mavenProject,
                                final MavenPlugin flexmojosPlugin,
                                final List<String> compiledLocales,
                                final List<String> runtimeLocales,
                                final FlexConfigInformer informer) {
    super(module, modifiableModelsProvider, flexEditor, mavenTree, mavenProjectToModuleName, mavenProject, flexmojosPlugin, compiledLocales,
          runtimeLocales, informer);
  }

  protected void appendGenerateConfigTask(final List<MavenProjectsProcessorTask> postTasks, final String configFilePath) {
    final Project project = myModule.getProject();

    Flexmojos5GenerateConfigTask existingTask = null;
    for (MavenProjectsProcessorTask postTask : postTasks) {
      if (postTask.getClass() == Flexmojos5GenerateConfigTask.class) {
        existingTask = (Flexmojos5GenerateConfigTask)postTask;
        break;
      }
    }

    if (existingTask == null) {
      ChangeListManager.getInstance(project).addFilesToIgnore(
        IgnoredBeanFactory.ignoreUnderDirectory(getCompilerConfigsDir(project), project));
      existingTask = new Flexmojos5GenerateConfigTask(myMavenTree);
      postTasks.add(existingTask);
    }

    existingTask.submit(myMavenProject, myModule, configFilePath);
  }
}
