package com.intellij.javascript.flex.maven;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.project.*;
import org.jetbrains.idea.maven.utils.MavenProcessCanceledException;
import org.jetbrains.idea.maven.utils.MavenProgressIndicator;

import java.io.IOException;
import java.util.Collection;

import static com.intellij.lang.javascript.flex.build.FlexCompilerConfigFileUtil.*;

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
  private final Collection<RLMInfo> myRlmInfos;

  // compilation of modules should not overwrtite report files produced by main application compilation so some compiler options should be excluded
  private static final String[] ELEMENTS_TO_REMOVE = {"dump-config", "link-report", "resource-bundle-list"};

  /**
   * @param rlmInfos Pairs where <b>first</b> is relative path to RLM main file, <b>second</b> is absolute path to its compiler config file
   */
  public RuntimeModulesGenerateConfigTask(final Module module,
                                          final MavenProject mavenProject,
                                          final MavenProjectsTree mavenProjectsTree,
                                          final String mainConfigFilePath,
                                          final Collection<RLMInfo> rlmInfos) {
    super(mavenProject, mavenProjectsTree);
    myModule = module;
    myMainConfigFilePath = mainConfigFilePath;
    myRlmInfos = rlmInfos;
  }

  public void perform(final Project project,
                      final MavenEmbeddersManager embeddersManager,
                      final MavenConsole console,
                      final MavenProgressIndicator indicator) throws MavenProcessCanceledException {
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
        JDOMUtil.writeDocument(mainConfigRootElement.getDocument(), info.myConfigFilePath,
                               CodeStyleSettingsManager.getSettings(project).getLineSeparator());
      }
      catch (IOException e) {/**/}
    }
  }

  @Nullable
  private static Element getClonedRootElementOfMainConfigFile(final String filePath) {
    final VirtualFile configFile = LocalFileSystem.getInstance().findFileByPath(filePath);
    if (configFile != null) {
      try {
        final Document document = (Document)JDOMUtil.loadDocument(configFile.getInputStream()).clone();
        final Element rootElement = document.getRootElement();
        if (rootElement.getName().equals(FLEX_CONFIG)) {
          return rootElement;
        }
      }
      catch (JDOMException e) {/*ignore*/}
      catch (IOException e) {/*ignore*/}
    }
    return null;
  }

  @NotNull
  private static String findAbsolutePath(final Module module, final String relativeToSourceRoot) {
    for (final VirtualFile root : ModuleRootManager.getInstance(module).getSourceRoots()) {
      final VirtualFile file = VfsUtil.findRelativeFile(relativeToSourceRoot, root);
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
