// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.flex.maven;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.project.*;
import org.jetbrains.idea.maven.utils.MavenProgressIndicator;
import org.jetbrains.idea.maven.utils.MavenUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

import static com.intellij.flex.build.FlexCompilerConfigFileUtilBase.FLEX_CONFIG;
import static com.intellij.flex.build.FlexCompilerConfigFileUtilBase.PATH_ELEMENT;
import static com.intellij.lang.javascript.flex.build.FlexCompilerConfigFileUtil.FILE_SPECS;
import static com.intellij.lang.javascript.flex.build.FlexCompilerConfigFileUtil.OUTPUT;

public class RuntimeModulesGenerateConfigTask extends MavenProjectsProcessorBasicTask {

  static class RLMInfo {
    final String myRLMName;
    final String myMainClass;
    final String myMainClassRelativePath;
    final String myOutputFileName;
    final String myOutputFolderPath;
    final String myConfigFilePath;

    RLMInfo(final String RLMName, final String mainClass, final String mainClassRelativePath, final String outputFileName,
            final String outputFolderPath, final String configFilePath) {
      myRLMName = RLMName;
      myMainClass = mainClass;
      myMainClassRelativePath = mainClassRelativePath;
      myOutputFileName = outputFileName;
      myOutputFolderPath = outputFolderPath;
      myConfigFilePath = configFilePath;
    }
  }

  private final Module myModule;
  private final String myMainConfigFilePath;
  private final Collection<? extends RLMInfo> myRlmInfos;

  // compilation of modules should not overwrtite report files produced by main application compilation so some compiler options should be excluded
  private static final String[] ELEMENTS_TO_REMOVE = {"dump-config", "link-report", "resource-bundle-list"};

  /**
   * @param rlmInfos Pairs where <b>first</b> is relative path to RLM main file, <b>second</b> is absolute path to its compiler config file
   */
  public RuntimeModulesGenerateConfigTask(final Module module,
                                          final MavenProject mavenProject,
                                          final MavenProjectsTree mavenProjectsTree,
                                          final String mainConfigFilePath,
                                          final Collection<? extends RLMInfo> rlmInfos) {
    super(mavenProject, mavenProjectsTree);
    myModule = module;
    myMainConfigFilePath = mainConfigFilePath;
    myRlmInfos = rlmInfos;
  }

  @Override
  public void perform(final Project project,
                      final MavenEmbeddersManager embeddersManager,
                      final MavenConsole console,
                      final MavenProgressIndicator indicator) {
    final Element mainConfigRootElement = getClonedRootElementOfMainConfigFile(myMainConfigFilePath);
    if (mainConfigRootElement == null) return;

    for (RLMInfo info : myRlmInfos) {
      indicator.setText("Generating Flex compiler configuration file for " + info.myRLMName);

      final String mainClassAbsolutePath = findAbsolutePath(myModule, info.myMainClassRelativePath);
      changeInputAndOutputFilePaths(mainConfigRootElement, mainClassAbsolutePath, info.myOutputFolderPath + "/" + info.myOutputFileName);
      removeChildrenWithNames(mainConfigRootElement, ELEMENTS_TO_REMOVE);
      // TODO: to be fully equivalent to flexmojos we need also to add 'load-externs' parameter to module config file, 'link-report' parameter to main application config file and care about compilation order
      // and similar but more complicated thing with resource-bundle-list / include-resource-bundle ?
      try {
        JDOMUtil.write(mainConfigRootElement, Path.of(info.myConfigFilePath));
      }
      catch (IOException ignored) {/**/}
    }

    MavenUtil.invokeAndWaitWriteAction(project, () -> {
      // need to refresh externally created file
      for (RLMInfo info : myRlmInfos) {
        final VirtualFile file = LocalFileSystem.getInstance().refreshAndFindFileByPath(info.myConfigFilePath);
        if (file != null) {
          file.refresh(false, false);
        }
      }
    });
  }

  @Nullable
  private static Element getClonedRootElementOfMainConfigFile(final String filePath) {
    final VirtualFile configFile = LocalFileSystem.getInstance().findFileByPath(filePath);
    if (configFile != null) {
      try {
        final Element element = JDOMUtil.load(configFile.getInputStream());
        if (element.getName().equals(FLEX_CONFIG)) {
          return element;
        }
      }
      catch (JDOMException ignored) {/*ignore*/}
      catch (IOException ignored) {/*ignore*/}
    }
    return null;
  }

  @NotNull
  private static String findAbsolutePath(final Module module, final String relativeToSourceRoot) {
    for (final VirtualFile root : ModuleRootManager.getInstance(module).getSourceRoots()) {
      final VirtualFile file = VfsUtilCore.findRelativeFile(relativeToSourceRoot, root);
      if (file != null) {
        return file.getPath();
      }
    }
    return relativeToSourceRoot;
  }

  private static void changeInputAndOutputFilePaths(final Element configElement,
                                                    final String mainClassAbsolutePath,
                                                    final String outputFilePath) {
    Element fileSpecsElement = configElement.getChild(FILE_SPECS, configElement.getNamespace());
    if (fileSpecsElement == null) {
      fileSpecsElement = new Element(FILE_SPECS, configElement.getNamespace());
      configElement.addContent(fileSpecsElement);
    }

    Element pathElement = fileSpecsElement.getChild(PATH_ELEMENT, fileSpecsElement.getNamespace());
    if (pathElement == null) {
      pathElement = new Element(PATH_ELEMENT, fileSpecsElement.getNamespace());
      fileSpecsElement.addContent(pathElement);
    }

    pathElement.setText(mainClassAbsolutePath);

    Element outputElement = configElement.getChild(OUTPUT, configElement.getNamespace());
    if (outputElement == null) {
      outputElement = new Element(OUTPUT, configElement.getNamespace());
      configElement.addContent(outputElement);
    }

    outputElement.setText(outputFilePath);
  }

  private static void removeChildrenWithNames(final Element rootElement, final String[] elementNamesToRemove) {
    for (final String elementName : elementNamesToRemove) {
      rootElement.removeChildren(elementName, rootElement.getNamespace());
    }
  }
}
