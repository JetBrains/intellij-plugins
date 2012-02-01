package com.intellij.javascript.flex.maven;

import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexProjectConfigurationEditor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.IgnoredBeanFactory;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.importing.MavenModifiableModelsProvider;
import org.jetbrains.idea.maven.model.MavenPlugin;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsProcessorTask;
import org.jetbrains.idea.maven.project.MavenProjectsTree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.intellij.javascript.flex.maven.RuntimeModulesGenerateConfigTask.RLMInfo;

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
  @Nullable
  protected String getCompilerConfigFilePath(final String rlmName) {
    final Element configurationElement = myFlexmojosPlugin.getConfigurationElement();
    final String classifier =
      configurationElement == null ? null : configurationElement.getChildTextNormalize("classifier", configurationElement.getNamespace());

    String suffix = "";
    if (rlmName != null) {
      suffix = "-" + rlmName;
    }
    else if (classifier != null) {
      suffix = "-" + classifier;
    }
    return getCompilerConfigsDir(myModule.getProject()) + "/" + myMavenProject.getMavenId().getArtifactId() + "-" +
           myMavenProject.getMavenId().getGroupId() + suffix + ".xml";
  }

  protected Collection<RLMInfo> getRLMInfos() {
    final Element configurationElement = myFlexmojosPlugin.getConfigurationElement();
    final Element modulesElement = configurationElement == null
                                   ? null : configurationElement.getChild("modules", configurationElement.getNamespace());

    if (modulesElement == null) {
      return Collections.emptyList();
    }

    final List<RLMInfo> result = new ArrayList<RLMInfo>();
    //noinspection unchecked
    for (final Element moduleElement : (Iterable<Element>)modulesElement.getChildren()) {
      if (moduleElement.getChildren().size() > 0) {
        final String mainClassRelativePath = moduleElement.getChildTextNormalize("sourceFile", moduleElement.getNamespace());
        final String finalName = moduleElement.getChildTextNormalize("finalName", moduleElement.getNamespace());
        final String destinationPath = moduleElement.getChildTextNormalize("destinationPath", moduleElement.getNamespace());
        final String mainClass = FileUtil.getNameWithoutExtension(mainClassRelativePath.replace('/', '.'));
        final String rlmName = StringUtil.notNullize(finalName, StringUtil.getShortName(mainClass));
        // indeed, rlmName.toLowerCase() is specific to flexmojos 4
        final String outputFileName = finalName != null ? finalName + ".swf"
                                                        : myMavenProject.getFinalName() + "-" + rlmName.toLowerCase() + ".swf";
        final String outputFolderPath = FileUtil.toSystemIndependentName(myMavenProject.getBuildDirectory() +
                                                                         (destinationPath == null ? "" : ("/" + destinationPath)));
        final String configFilePath = getCompilerConfigFilePath(rlmName);
        result.add(new RLMInfo(rlmName, mainClass, mainClassRelativePath, outputFileName, outputFolderPath, configFilePath));
      }
      else {
        final String path = moduleElement.getTextNormalize();
        if (path.endsWith(".mxml") || path.endsWith(".as")) {
          final String mainClassRelativePath = FileUtil.toSystemIndependentName(path);
          final String mainClass = FileUtil.getNameWithoutExtension(mainClassRelativePath.replace('/', '.'));
          final String rlmName = StringUtil.getShortName(mainClass);
          // indeed, rlmName.toLowerCase() is specific to flexmojos 4
          final String outputFileName = myMavenProject.getFinalName() + "-" + rlmName.toLowerCase() + ".swf";
          final String outputFolderPath = FileUtil.toSystemIndependentName(myMavenProject.getBuildDirectory());
          final String configFilePath = getCompilerConfigFilePath(rlmName);
          result.add(new RLMInfo(rlmName, mainClass, mainClassRelativePath, outputFileName, outputFolderPath, configFilePath));
        }
      }
    }
    return result;
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
