package com.intellij.lang.javascript.flex.flashbuilder;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetType;
import com.intellij.facet.ModifiableFacetModel;
import com.intellij.lang.javascript.flex.*;
import com.intellij.lang.javascript.flex.build.FlexBuildConfiguration;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathMacros;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.impl.ProjectMacrosUtil;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.*;
import gnu.trove.THashMap;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FlashBuilderModuleImporter {

  private static final String LIBRARY_NAME_PREFIX_OLD = "Imported from Flash/Flex Builder: "; // for backward consistency
  private static final String LIBRARY_NAME_PREFIX = FlexBundle.message("flash.builder.library.prefix") + " ";
  private static final String CORE_RESOURCES_PREFS_REL_PATH =
    "/.metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.core.resources.prefs";
  private static final String PATHVARIABLE_DOT = "pathvariable.";

  private final Project myIdeaProject;
  private final Collection<String> myAllImportedModuleNames;
  private final FlashBuilderSdkFinder mySdkFinder;
  private final Set<String> myPathVariables;
  private boolean myPathVariablesInitialized = false;

  public FlashBuilderModuleImporter(final Project ideaProject,
                                    final Collection<String> allImportedModuleNames,
                                    final FlashBuilderSdkFinder sdkFinder,
                                    final Set<String> pathVariables) {
    myIdeaProject = ideaProject;
    myAllImportedModuleNames = allImportedModuleNames;
    mySdkFinder = sdkFinder;
    myPathVariables = pathVariables;
  }

  public void setupModule(final ModifiableRootModel rootModel, final FlashBuilderProject flashBuilderProject) {
    rootModel.inheritSdk();
    setupRoots(rootModel, flashBuilderProject);
    setupOutput(rootModel, flashBuilderProject);
    setupDependencies(rootModel, flashBuilderProject);
    setupFacets(rootModel, flashBuilderProject);
  }

  private void setupFacets(final ModifiableRootModel rootModel, final FlashBuilderProject flashBuilderProject) {
    final Sdk flexSdk = flashBuilderProject.isSdkUsed() ? mySdkFinder.findSdk(flashBuilderProject) : null;

    final ModifiableFacetModel facetModel = FacetManager.getInstance(rootModel.getModule()).createModifiableModel();
    for (final Facet flexFacet : facetModel.getFacetsByType(FlexFacet.ID)) {
      facetModel.removeFacet(flexFacet);
    }

    final FlexFacet mainFacet = createFlexFacet(rootModel.getModule(), facetModel, suggestMainFacetName(flashBuilderProject));
    setupMainFacetConfig(FlexBuildConfiguration.getInstance(mainFacet), flashBuilderProject, flexSdk, rootModel.getSourceRoots());
    setupOtherAppsAndModulesFacets(rootModel, facetModel, flashBuilderProject, flexSdk);

    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      public void run() {
        facetModel.commit();
      }
    });

    if (flashBuilderProject.isSdkUsed()) {
      mainFacet.getFlexConfiguration().setFlexSdk(mySdkFinder.findSdk(flashBuilderProject), rootModel);
    }
  }

  private void setupOtherAppsAndModulesFacets(final ModifiableRootModel rootModel,
                                              final ModifiableFacetModel facetModel,
                                              final FlashBuilderProject flashBuilderProject, final Sdk flexSdk) {
    int appNumber = 2;
    for (final String className : flashBuilderProject.getApplicationClassNames()) {
      final FlexFacet appFacet = createFlexFacet(rootModel.getModule(), facetModel, suggestAppFacetName(flashBuilderProject, appNumber++));
      setupAppConfig(rootModel, FlexBuildConfiguration.getInstance(appFacet), flashBuilderProject, flexSdk, className,
                     null);
    }

    int moduleNumber = 1;
    for (final Pair<String, String> sourcePathAndDestPath : flashBuilderProject.getModules()) {
      final String moduleClassName = getModuleClassName(flashBuilderProject, sourcePathAndDestPath.first, rootModel.getSourceRootUrls());
      final FlexFacet moduleFacet = createFlexFacet(rootModel.getModule(), facetModel, suggestModuleFacetName(flashBuilderProject,
                                                                                                              moduleNumber++));
      setupAppConfig(rootModel, FlexBuildConfiguration.getInstance(moduleFacet), flashBuilderProject, flexSdk,
                     moduleClassName, sourcePathAndDestPath.second);
    }
  }

  private String getModuleClassName(final FlashBuilderProject flashBuilderProject,
                                    final String moduleSourcePath,
                                    final String[] sourceRootUrls) {
    final String mainClassPathUrl =VfsUtil.pathToUrl(getAbsolutePathWithLinksHandled(flashBuilderProject, moduleSourcePath));
    for (final String sourceRootUrl : sourceRootUrls) {
      if (mainClassPathUrl.startsWith(sourceRootUrl + "/")) {
        return FlashBuilderProjectLoadUtil.getClassName(mainClassPathUrl.substring(sourceRootUrl.length() + 1));
      }
    }

    return FlashBuilderProjectLoadUtil.getClassName(mainClassPathUrl.substring(mainClassPathUrl.lastIndexOf('/') + 1));
  }

  private static String suggestMainFacetName(final FlashBuilderProject flashBuilderProject) {
    final String type = flashBuilderProject.getCompilerOutputType();
    return flashBuilderProject.getProjectType().toString() + " " + type
           + (FlexBuildConfiguration.APPLICATION.equals(type) && flashBuilderProject.getApplicationClassNames().size() > 0 ? " 1" : "");
  }

  private static String suggestAppFacetName(final FlashBuilderProject flashBuilderProject, final int number) {
    return flashBuilderProject.getProjectType().toString() + " " + FlexBuildConfiguration.APPLICATION + " " + number;
  }

  private static String suggestModuleFacetName(final FlashBuilderProject flashBuilderProject, final int number) {
    return flashBuilderProject.getProjectType().toString() + " Module " + number;
  }

  private static FlexFacet createFlexFacet(final Module module, final ModifiableFacetModel facetModel, final String facetName) {
    final FacetType<FlexFacet, FlexFacetConfiguration> facetType = FlexFacetType.getInstance();
    final FlexFacet flexFacet = facetType.createFacet(module, facetName, facetType.createDefaultConfiguration(), null);
    facetModel.addFacet(flexFacet);
    return flexFacet;
  }

  public static boolean isImportedFromFlashBuilder(final Library library) {
    final String libraryName = library.getName();
    return libraryName != null && (libraryName.startsWith(LIBRARY_NAME_PREFIX_OLD) || libraryName.startsWith(LIBRARY_NAME_PREFIX));
  }

  private void setupRoots(final ModifiableRootModel rootModel, final FlashBuilderProject flashBuilderProject) {
    final String mainContentEntryUrl = VfsUtil.pathToUrl(flashBuilderProject.getProjectRootPath());
    final ContentEntry mainContentEntry = rootModel.addContentEntry(mainContentEntryUrl);
    final Collection<ContentEntry> otherContentEntries = new ArrayList<ContentEntry>();

    final Collection<String> sourcePaths = flashBuilderProject.getSourcePaths();
    if (sourcePaths.isEmpty()) {
      final VirtualFile contentRoot = mainContentEntry.getFile();
      final String mainClass = flashBuilderProject.getMainAppClassName();
      if (contentRoot != null &&
          !StringUtil.isEmpty(mainClass) &&
          (contentRoot.findChild(mainClass + ".mxml") != null || contentRoot.findChild(mainClass + ".as") != null)) {
        mainContentEntry.addSourceFolder(mainContentEntry.getUrl(), false);
      }
    }
    else {
      OUTER:
      for (final String _sourcePath : sourcePaths) {
        final String sourcePath = getAbsolutePathWithLinksHandled(flashBuilderProject, _sourcePath);
        final String sourceUrl = VfsUtil.pathToUrl(sourcePath);
        if (FileUtil.isAncestor(new File(mainContentEntryUrl), new File(sourceUrl), false)) {
          mainContentEntry.addSourceFolder(sourceUrl, false);
        }
        else {
          for (final ContentEntry otherContentEntry : otherContentEntries) {
            if (FileUtil.isAncestor(new File(mainContentEntryUrl), new File(sourceUrl), false)) {
              otherContentEntry.addSourceFolder(sourceUrl, false);
              continue OUTER;
            }
          }

          final ContentEntry newContentEntry = rootModel.addContentEntry(sourceUrl);
          newContentEntry.addSourceFolder(sourceUrl, false);
          otherContentEntries.add(newContentEntry);
        }
      }
    }
  }

  private void setupOutput(final ModifiableRootModel rootModel, final FlashBuilderProject flashBuilderProject) {
    final CompilerModuleExtension compilerModuleExtension =
      (CompilerModuleExtension)rootModel.getModuleExtension(CompilerModuleExtension.class).getModifiableModel(true);
    Disposer.register(rootModel.getModule(), compilerModuleExtension);
    compilerModuleExtension.inheritCompilerOutputPath(false);
    final String outputFolderUrl =
      VfsUtil.pathToUrl(getAbsolutePathWithLinksHandled(flashBuilderProject, flashBuilderProject.getOutputFolderPath()));
    compilerModuleExtension.setCompilerOutputPath(outputFolderUrl);
    compilerModuleExtension.setCompilerOutputPathForTests(outputFolderUrl);
    compilerModuleExtension.commit();
  }

  private void setupMainFacetConfig(final FlexBuildConfiguration config,
                                    final FlashBuilderProject flashBuilderProject,
                                    final @Nullable Sdk flexSdk,
                                    final VirtualFile[] sourceRoots) {
    commonSetupConfig(config, flashBuilderProject, flexSdk);
    config.OUTPUT_TYPE = flashBuilderProject.getCompilerOutputType();
    config.MAIN_CLASS = flashBuilderProject.getMainAppClassName();
    config.OUTPUT_FILE_NAME = FlexBuildConfiguration.APPLICATION.equals(config.OUTPUT_TYPE)
                              ? StringUtil.getShortName(config.MAIN_CLASS) + ".swf"
                              : flashBuilderProject.getName() + ".swc";

    OUTER:
    for (final Pair<String, String> namespaceAndManifestPath : flashBuilderProject.getNamespacesAndManifestPaths()) {
      final String manifestPath = namespaceAndManifestPath.second;
      for (final VirtualFile sourceRoot : sourceRoots) {
        VirtualFile manifestFile;
        if ((manifestFile = sourceRoot.findFileByRelativePath(manifestPath)) != null) {
          addNamespaceAndManifestFileInfo(config, namespaceAndManifestPath.first, manifestFile.getPath());
          break OUTER;
        }
      }
      addNamespaceAndManifestFileInfo(config, namespaceAndManifestPath.first,
                                      getAbsolutePathWithLinksHandled(flashBuilderProject, FileUtil.toSystemIndependentName(manifestPath)));
    }

    for (final String path : flashBuilderProject.getCssFilesToCompile()) {
      config.CSS_FILES_LIST.add(getAbsolutePathWithLinksHandled(flashBuilderProject, path));
    }
  }

  private static void setupAppConfig(final ModuleRootModel rootModel,
                                     final FlexBuildConfiguration config,
                                     final FlashBuilderProject flashBuilderProject,
                                     final @Nullable Sdk flexSdk,
                                     final String className,
                                     final @Nullable String outputRelativePath) {
    commonSetupConfig(config, flashBuilderProject, flexSdk);
    config.OUTPUT_TYPE = FlexBuildConfiguration.APPLICATION;
    config.MAIN_CLASS = className;
    if (outputRelativePath == null) {
      config.OUTPUT_FILE_NAME = StringUtil.getShortName(config.MAIN_CLASS) + ".swf";
      config.USE_FACET_COMPILE_OUTPUT_PATH = false;
    }
    else {
      final int lastSlashIndex = outputRelativePath.lastIndexOf("/");
      config.OUTPUT_FILE_NAME = outputRelativePath.substring(lastSlashIndex + 1);
      config.USE_FACET_COMPILE_OUTPUT_PATH = lastSlashIndex > 0;
      final String standardOutput = VfsUtil.urlToPath(rootModel.getModuleExtension(CompilerModuleExtension.class).getCompilerOutputUrl());
      config.FACET_COMPILE_OUTPUT_PATH =
        standardOutput + (lastSlashIndex > 0 ? ("/" + outputRelativePath.substring(0, lastSlashIndex)) : "");
      config.FACET_COMPILE_OUTPUT_PATH_FOR_TESTS = standardOutput;
    }
  }

  private static void commonSetupConfig(final FlexBuildConfiguration config,
                                        final FlashBuilderProject flashBuilderProject,
                                        final Sdk flexSdk) {
    config.DO_BUILD = true;
    config.USE_DEFAULT_SDK_CONFIG_FILE = true;
    config.USE_CUSTOM_CONFIG_FILE = false;
    config.USE_FACET_COMPILE_OUTPUT_PATH = false;

    final String targetPlayerVersion = flashBuilderProject.getTargetPlayerVersion();
    if (flexSdk != null && TargetPlayerUtils.isTargetPlayerApplicable(flexSdk)) {
      config.TARGET_PLAYER_VERSION =
        StringUtil.isEmpty(targetPlayerVersion) ? TargetPlayerUtils.getTargetPlayerVersion(flexSdk) : targetPlayerVersion;
    }
    config.ADDITIONAL_COMPILER_OPTIONS = flashBuilderProject.getAdditionalCompilerOptions();
  }

  private static void addNamespaceAndManifestFileInfo(final FlexBuildConfiguration config, final String namespace, final String manifest) {
    final FlexBuildConfiguration.NamespaceAndManifestFileInfo info = new FlexBuildConfiguration.NamespaceAndManifestFileInfo();
    info.NAMESPACE = namespace;
    info.MANIFEST_FILE_PATH = manifest;
    info.INCLUDE_IN_SWC = true;
    config.NAMESPACE_AND_MANIFEST_FILE_INFO_LIST.add(info);
  }

  private void setupDependencies(final ModifiableRootModel rootModel, final FlashBuilderProject flashBuilderProject) {
    final LibraryTable libraryTable = rootModel.getModuleLibraryTable();
    for (final Library library : libraryTable.getLibraries()) {
      if (isImportedFromFlashBuilder(library)) {
        libraryTable.removeLibrary(library);
      }
    }

    for (final String libraryPathOrig : flashBuilderProject.getLibraryPaths()) {
      final boolean moduleDependency = setupModuleDependencyIfApplicable(rootModel, libraryPathOrig, myAllImportedModuleNames);

      if (!moduleDependency) {
        final String libraryPath = getAbsolutePathWithLinksHandled(flashBuilderProject, libraryPathOrig);
        final int slashIndex = libraryPath.lastIndexOf('/');
        final int dotIndex = libraryPath.lastIndexOf('.');
        final String libraryName =
          dotIndex > slashIndex ? libraryPath.substring(slashIndex + 1, dotIndex) : libraryPath.substring(slashIndex + 1);
        final Library library = libraryTable.createLibrary(LIBRARY_NAME_PREFIX + libraryName);
        final Library.ModifiableModel libraryModel = library.getModifiableModel();
        if (libraryPath.toLowerCase().endsWith(".swc")) {
          libraryModel.addRoot(VirtualFileManager.constructUrl(JarFileSystem.PROTOCOL, libraryPath) + JarFileSystem.JAR_SEPARATOR,
                               OrderRootType.CLASSES);
        }
        else {
          libraryModel.addJarDirectory(VfsUtil.pathToUrl(libraryPath), false);
        }

        for (final String librarySourcePath : flashBuilderProject.getLibrarySourcePaths(libraryPathOrig)) {
          libraryModel
            .addRoot(VfsUtil.pathToUrl(getAbsolutePathWithLinksHandled(flashBuilderProject, librarySourcePath)), OrderRootType.SOURCES);
        }
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
          public void run() {
            libraryModel.commit();
          }
        });
      }
    }
  }

  private static boolean setupModuleDependencyIfApplicable(final ModifiableRootModel rootModel,
                                                           final String libraryPath,
                                                           final Collection<String> allImportedModuleNames) {
    for (final String moduleName : allImportedModuleNames) {
      if (!moduleName.equals(rootModel.getModule().getName()) && libraryPath.startsWith("/" + moduleName + "/")) {
        rootModel.addInvalidModuleEntry(moduleName);
        return true;
      }
    }
    return false;
  }

  /**
   * First call of this method should be later than mySdkFinder.findSdk()
   */
  private String getAbsolutePathWithLinksHandled(final FlashBuilderProject project, final String path) {
    if (!myPathVariablesInitialized) {
      initPathVariables();
      myPathVariablesInitialized = true;
    }

    final int slashIndex = path.indexOf('/');
    final String potentialLink = slashIndex >= 0 ? path.substring(0, slashIndex) : path;

    if (potentialLink.startsWith("${") && potentialLink.endsWith("}")) {
      final String pathVarName = potentialLink.substring(2, potentialLink.length() - 1);
      final PathMacros pathMacros = PathMacros.getInstance();
      final String pathValue = pathMacros.getValue(pathVarName);

      if (pathValue != null) {
        return pathValue + (slashIndex >= 0 ? path.substring(slashIndex) : "");
      }
      else {
        return "$" + pathVarName + "$" + (slashIndex >= 0 ? path.substring(slashIndex) : "");
      }
    }
    else {
      final Map<String, String> linkedResources = project.getLinkedResources();
      if (!linkedResources.isEmpty()) {
        final String linkValue = linkedResources.get(potentialLink);
        if (linkValue != null) {
          final PathMacros pathMacros = PathMacros.getInstance();
          if (pathMacros.getValue(potentialLink) == null) {
            pathMacros.setMacro(potentialLink, linkValue);
          }
          return linkValue + (slashIndex >= 0 ? path.substring(slashIndex) : "");
        }
      }
    }

    return FileUtil.isAbsolute(path) ? path : project.getProjectRootPath() + '/' + path;
  }

  private void initPathVariables() {
    final PathMacros pathMacros = PathMacros.getInstance();

    final Map<String, String> myEclipsePathVariables = loadEclipsePathVariables(mySdkFinder.getWorkspacePath());

    for (final String pathVariable : myPathVariables) {
      final String pathValue = myEclipsePathVariables.get(pathVariable);
      if (pathValue != null) {
        pathMacros.setMacro(pathVariable, pathValue);
      }
    }

    ProjectMacrosUtil.checkNonIgnoredMacros(myIdeaProject, myPathVariables);
  }

  private static Map<String, String> loadEclipsePathVariables(final String workspacePath) {
    final Map<String, String> eclipsePathVariables = new THashMap<String, String>();
    final VirtualFile prefsFile = LocalFileSystem.getInstance().findFileByPath(workspacePath + CORE_RESOURCES_PREFS_REL_PATH);
    if (prefsFile == null) return eclipsePathVariables;

    final Properties properties = new Properties();
    try {
      properties.load(prefsFile.getInputStream());
      for (final Map.Entry<Object, Object> entry : properties.entrySet()) {
        final String key = (String)entry.getKey();
        if (key.startsWith(PATHVARIABLE_DOT)) {
          eclipsePathVariables.put(key.substring(PATHVARIABLE_DOT.length()), (String)entry.getValue());
        }
      }
    }
    catch (IOException e) {/*ignore*/}
    return eclipsePathVariables;
  }
}
