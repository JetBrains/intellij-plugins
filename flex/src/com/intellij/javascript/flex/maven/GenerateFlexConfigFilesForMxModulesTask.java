package com.intellij.javascript.flex.maven;

import com.intellij.facet.FacetManager;
import com.intellij.lang.javascript.flex.FlexFacet;
import com.intellij.lang.javascript.flex.build.FlexBuildConfiguration;
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

import static com.intellij.lang.javascript.flex.build.FlexCompilerConfigFileUtil.*;

public class GenerateFlexConfigFilesForMxModulesTask extends MavenProjectsProcessorBasicTask {

  private final Module myModule;

  // compilation of modules should not overwrtite report files prodiced by main application compilation so some compiler options should be excluded
  private static final String[] ELEMENTS_TO_REMOVE = {"dump-config", "link-report", "resource-bundle-list"};

  public GenerateFlexConfigFilesForMxModulesTask(final Module module,
                                                 final MavenProject mavenProject,
                                                 final MavenProjectsTree mavenProjectsTree) {
    super(mavenProject, mavenProjectsTree);
    myModule = module;
  }

  public void perform(final Project project,
                      final MavenEmbeddersManager embeddersManager,
                      final MavenConsole console,
                      final MavenProgressIndicator indicator)
    throws MavenProcessCanceledException {
    indicator.checkCanceled();

    for (final FlexFacet flexFacet : FacetManager.getInstance(myModule).getFacetsByType(FlexFacet.ID)) {
      if (FlexMojos3FacetImporter.isMxModuleFacet(flexFacet)) {
        indicator.checkCanceled();

        final Element mainConfigRootElement = getClonedRootElementOfMainConfigFile(myModule);
        if (mainConfigRootElement == null) return;

        final FlexBuildConfiguration config = FlexBuildConfiguration.getInstance(flexFacet);
        indicator.setText("Generating Flex compiler configuration file for " + config.OUTPUT_FILE_NAME);

        final String configFilePath = config.CUSTOM_CONFIG_FILE;
        final String compilerConfigXmlSuffix =
          FlexMojos3FacetImporter.getCompilerConfigXmlSuffix(FlexMojos3FacetImporter.getFlexmojosPlugin(myMavenProject));
        assert configFilePath.endsWith(compilerConfigXmlSuffix);
        final String outputFilePath = configFilePath.replace(compilerConfigXmlSuffix, ".swf");

        final String mxModuleFilePath = getMxModuleFilePath(myModule, FlexMojos3FacetImporter.getMxModuleFileRelativePath(flexFacet));
        changeInputAndOutputFilePaths(mainConfigRootElement, mxModuleFilePath, outputFilePath);
        removeChildrenWithNames(mainConfigRootElement, ELEMENTS_TO_REMOVE);
        // TODO: to be fully equivalent to flexmojos we need also to add 'load-externs' parameter to module config file, 'link-report' parameter to main application config file and care about compilation order
        // and similar but more complicated thing with resource-bundle-list / include-resource-bundle ?  
        try {
          JDOMUtil.writeDocument(mainConfigRootElement.getDocument(), configFilePath,
                                 CodeStyleSettingsManager.getSettings(project).getLineSeparator());
        }
        catch (IOException e) {/**/}
      }
    }
  }

  @Nullable
  private static Element getClonedRootElementOfMainConfigFile(final Module module) {
    for (final FlexFacet flexFacet : FacetManager.getInstance(module).getFacetsByType(FlexFacet.ID)) {
      if (flexFacet.getName().equals(FlexFacetImporter.FLEX_FACET_DEFAULT_NAME)) {
        final FlexBuildConfiguration config = FlexBuildConfiguration.getInstance(flexFacet);
        final VirtualFile configFile = LocalFileSystem.getInstance().findFileByPath(config.CUSTOM_CONFIG_FILE);
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
    }
    return null;
  }

  @NotNull
  private static String getMxModuleFilePath(final Module module, final String mxModuleFileRelativePath) {
    for (final VirtualFile root : ModuleRootManager.getInstance(module).getSourceRoots()) {
      final VirtualFile file = VfsUtil.findRelativeFile(mxModuleFileRelativePath, root);
      if (file != null) {
        return file.getPath();
      }
    }
    return mxModuleFileRelativePath;
  }

  private static void changeInputAndOutputFilePaths(final Element configElement,
                                                    final String mxModuleFilePath,
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

    pathElement.setText(mxModuleFilePath);

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
