// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.flashbuilder;

import com.intellij.flex.model.bc.OutputType;
import com.intellij.flex.model.bc.TargetPlatform;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathUtil;
import gnu.trove.THashMap;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public final class FlashBuilderProjectLoadUtil {

  private static final String PROJECT_NAME_TAG = "<projectDescription><name>";
  private static final String ACTION_SCRIPT_PROPERTIES_TAG = "actionScriptProperties";
  private static final String FXP_PROPERTIES_TAG = "fxpProperties";
  private static final String COMPILER_TAG = "compiler";
  private static final String SWC_TAG = "swc";
  private static final String SOURCE_FOLDER_PATH_ATTR = "sourceFolderPath";
  private static final String COMPILER_SOURCE_PATH_TAG = "compilerSourcePath";
  private static final String LINKED_TAG = "linked";
  private static final String COMPILER_SOURCE_PATH_ENTRY_TAG = "compilerSourcePathEntry";
  private static final String PATH_ATTR = "path";
  private static final String PATH_ENTRY_ELEMENT = "pathEntry";
  private static final String LOCATION_ATTR = "location";
  private static final String OUTPUT_FOLDER_LOCATION_ATTR = "outputFolderLocation";
  private static final String OUTPUT_FOLDER_PATH_ATTR = "outputFolderPath";
  private static final String MAIN_APP_PATH_ATTR = "mainApplicationPath";
  private static final String TARGET_PLAYER_VERSION_ATTR = "targetPlayerVersion";
  private static final String ADDITIONAL_COMPILER_ARGUMENTS_ATTR = "additionalCompilerArguments";
  private static final String HTML_GENERATE_ATTR = "htmlGenerate";
  private static final String LIBRARY_PATH_TAG = "libraryPath";
  private static final String LIBRARY_PATH_ENTRY_TAG = "libraryPathEntry";
  private static final String LIBRARY_KIND_ATTR = "kind";
  private static final String SWC_FOLDER_KIND = "1";
  private static final String SWC_FILE_KIND = "3";
  private static final String USE_SDK_KIND = "4";
  private static final String ANE_KIND = "5";
  private static final String FLEX_SDK_ATTR = "flexSDK";
  private static final String USE_APOLLO_CONFIG_ATTR = "useApolloConfig";
  private static final String PROJECT_DESCRIPTION_TAG = "projectDescription";
  private static final String NAME_TAG = "name";
  private static final String LINKED_RESOURCES_TAG = "linkedResources";
  private static final String LINK_TAG = "link";
  private static final String LOCATION_TAG = "location";
  private static final String FLEX_LIB_PROPERTIES_TAG = "flexLibProperties";
  private static final String NAMESPACE_MANIFESTS_TAG = "namespaceManifests";
  private static final String NAMESPACE_MANIFEST_ENTRY_TAG = "namespaceManifestEntry";
  private static final String MANIFEST_ATTR = "manifest";
  private static final String NAMESPACE_ATTR = "namespace";
  private static final String APPLICATIONS_ELEMENT = "applications";
  private static final String APPLICATION_ELEMENT = "application";
  private static final String MODULES_ELEMENT = "modules";
  private static final String MODULE_ELEMENT = "module";
  private static final String DEST_PATH_ATTR = "destPath";
  private static final String OPTIMIZE_ATTR = "optimize";
  private static final String APPLICATION_ATTR = "application";
  private static final String SOURCE_PATH_ATTR = "sourcePath";
  private static final String BUILD_CSS_FILES_ELEMENT = "buildCSSFiles";
  private static final String BUILD_CSS_FILE_ENTRY_ELEMENT = "buildCSSFileEntry";
  private static final String BUILD_TARGETS_ELEMENT = "buildTargets";
  private static final String BUILD_TARGET_ELEMENT = "buildTarget";
  private static final String BUILD_TARGET_NAME_ATTR = "buildTargetName";
  private static final String MULTI_PLATFORM_SETTINGS_ELEMENT = "multiPlatformSettings";
  private static final String ENABLED_ATTR = "enabled";
  private static final String ANDROID_PLATFORM_ATTR_VALUE = "com.adobe.flexide.multiplatform.android.platform";
  private static final String IOS_PLATFORM_ATTR_VALUE = "com.adobe.flexide.multiplatform.ios.platform";
  private static final String BLACKBERRY_PLATFORM_ATTR_VALUE = "com.qnx.flexide.multiplatform.qnx.platform";
  private static final String USE_MULTIPLATFORM_CONFIG_ATTR = "useMultiPlatformConfig";
  private static final String PROVISIONING_FILE_ATTR = "provisioningFile";
  private static final String AIR_SETTINGS_ELEMENT = "airSettings";
  private static final String AIR_EXCLUDES_ELEMENT = "airExcludes";
  private static final String AIR_CERTIFICATE_ATTR = "airCertificatePath";
  private static final String INCLUDE_RESOURCES_ELEMENT = "includeResources";
  private static final String RESOURCE_ENTRY_ELEMENT = "resourceEntry";
  private static final String DEFAULT_VALUE = "default";
  private static final String PLATFORM_ID_1 = "platformId";
  private static final String PLATFORM_ID_2 = "platformID";
  private static final String USE_FLASH_SDK = "useFlashSDK";

  private static final String FLEXUNIT_LIB_MACRO = "${FLEXUNIT_LIB_LOCATION}";
  private static final String FLEXUNIT_LOCALE_MACRO = "${FLEXUNIT_LOCALE_LOCATION}";

  private static final String THEME_ELEMENT = "theme";
  public static final String DEFAULT_THEME_ATTR = "themeIsDefault";
  private static final String THEME_LOCATION_ATTR = "themeLocation";

  private FlashBuilderProjectLoadUtil() {
  }

  public static FlashBuilderProject getDummyFBProject(final String name) {
    assert ApplicationManager.getApplication().isUnitTestMode();
    final FlashBuilderProject fbProject = new FlashBuilderProject();
    fbProject.setName(name);
    fbProject.setOutputType(OutputType.Library);
    return fbProject;
  }

  @NotNull
  static String readProjectName(final String dotProjectFilePath) {
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(dotProjectFilePath);
      final String name = FlexUtils.findXMLElement(fis, PROJECT_NAME_TAG);
      if (!StringUtil.isEmptyOrSpaces(name)) {
        return name;
      }
    }
    catch (IOException ignored) {/*ignore*/}
    finally {
      if (fis != null) {
        try {
          fis.close();
        }
        catch (IOException ignored) {/*ignore*/}
      }
    }
    return PathUtil.getFileName(PathUtil.getParentPath(dotProjectFilePath));
  }

  public static List<FlashBuilderProject> loadProjects(final Collection<String> dotProjectFilePaths, final boolean isArchive) {
    final List<FlashBuilderProject> flashBuilderProjects = new ArrayList<>(dotProjectFilePaths.size());
    for (final String dotProjectFilePath : dotProjectFilePaths) {
      final VirtualFile dotProjectFile = LocalFileSystem.getInstance().findFileByPath(dotProjectFilePath);
      if (dotProjectFile != null) {
        flashBuilderProjects.add(loadProject(dotProjectFile, isArchive));
      }
    }
    return flashBuilderProjects;
  }

  public static FlashBuilderProject loadProject(final VirtualFile dotProjectFile, final boolean isArchive) {
    final FlashBuilderProject project = new FlashBuilderProject();

    loadProjectNameAndLinkedResources(project, dotProjectFile);
    loadOutputType(project, dotProjectFile);
    loadProjectRoot(project, dotProjectFile);

    final Map<String, String> pathReplacementMap =
      isArchive ? loadMapFromDotFxpPropertiesFile(dotProjectFile) : Collections.emptyMap();
    loadInfoFromDotActionScriptPropertiesFile(project, dotProjectFile, pathReplacementMap);
    loadInfoFromDotFlexLibPropertiesFile(project, dotProjectFile);

    return project;
  }

  private static void loadProjectNameAndLinkedResources(final FlashBuilderProject project, final VirtualFile dotProjectFile) {
    try {
      final Element projectDescription = JDOMUtil.load(dotProjectFile.getInputStream());
      if (!PROJECT_DESCRIPTION_TAG.equals(projectDescription.getName())) return;

      final String projectName = projectDescription.getChildText(NAME_TAG, projectDescription.getNamespace());
      project.setName(StringUtil.notNullize(projectName, FlexBundle.message("unnamed")));

      for (final Element linkedResourcesElement : projectDescription
        .getChildren(LINKED_RESOURCES_TAG, projectDescription.getNamespace())) {
        for (final Element linkElement : linkedResourcesElement
          .getChildren(LINK_TAG, linkedResourcesElement.getNamespace())) {

          final String linkName = linkElement.getChildText(NAME_TAG, linkElement.getNamespace());
          final String linkLocation = linkElement.getChildText(LOCATION_TAG, linkElement.getNamespace());

          if (!StringUtil.isEmptyOrSpaces(linkName) && !StringUtil.isEmptyOrSpaces(linkLocation)) {
            project.addLinkedResource(linkName, FileUtil.toSystemIndependentName(linkLocation));
          }
        }
      }
    }
    catch (JDOMException ignored) {/*ignore*/}
    catch (IOException ignored) {/*ignore*/}
  }

  private static Map<String, String> loadMapFromDotFxpPropertiesFile(final VirtualFile dotProjectFile) {
    final Map<String, String> result = new THashMap<>();

    final VirtualFile dir = dotProjectFile.getParent();
    assert dir != null;
    final VirtualFile dotFxpPropertiesFile = dir.findChild(FlashBuilderImporter.DOT_FXP_PROPERTIES);
    if (dotFxpPropertiesFile != null) {
      try {
        final Element fxpPropertiesElement = JDOMUtil.load(dotFxpPropertiesFile.getInputStream());
        if (!FXP_PROPERTIES_TAG.equals(fxpPropertiesElement.getName())) return Collections.emptyMap();

        final Element swcElement = fxpPropertiesElement.getChild(SWC_TAG);
        if (swcElement != null) {
          for (final Element linkedElement : swcElement.getChildren(LINKED_TAG)) {
            final String location = linkedElement.getAttributeValue(LOCATION_ATTR);
            final String path = linkedElement.getAttributeValue(PATH_ATTR);
            if (!StringUtil.isEmptyOrSpaces(location) && !StringUtil.isEmptyOrSpaces(path)) {
              result.put(location, path);
            }
          }
        }
      }
      catch (JDOMException ignored) {/*ignore*/}
      catch (IOException ignored) {/*ignore*/}
    }

    return result;
  }

  private static void loadInfoFromDotActionScriptPropertiesFile(final FlashBuilderProject project,
                                                                final VirtualFile dotProjectFile,
                                                                final Map<String, String> pathReplacementMap) {
    final VirtualFile dir = dotProjectFile.getParent();
    assert dir != null;
    final VirtualFile dotActionScriptPropertiesFile = dir.findChild(FlashBuilderImporter.DOT_ACTION_SCRIPT_PROPERTIES);
    if (dotActionScriptPropertiesFile != null) {
      try {
        final Element actionScriptPropertiesElement = JDOMUtil.load(dotActionScriptPropertiesFile.getInputStream());
        if (!ACTION_SCRIPT_PROPERTIES_TAG.equals(actionScriptPropertiesElement.getName())) return;
        loadMainClassName(project, actionScriptPropertiesElement);

        final Element compilerElement = actionScriptPropertiesElement.getChild(COMPILER_TAG);
        if (compilerElement != null) {
          loadProjectType(project, dotProjectFile, compilerElement, actionScriptPropertiesElement);
          loadSourcePaths(project, compilerElement);
          loadOutputFolderPath(project, compilerElement);
          loadTargetPlayerVersion(project, compilerElement);
          loadAdditionalCompilerArguments(project, compilerElement);
          project.setUseHtmlWrapper("true".equals(compilerElement.getAttributeValue(HTML_GENERATE_ATTR)));
          loadDependenciesAndCheckIfSdkUsed(project, compilerElement, pathReplacementMap);
          if (project.isSdkUsed()) {
            loadSdkName(project, compilerElement);
          }
        }

        if (project.getOutputType() == OutputType.Application) {
          loadApplications(project, actionScriptPropertiesElement);
          loadModules(project, actionScriptPropertiesElement);
          loadCssFilesToCompile(project, actionScriptPropertiesElement);

          if (!project.isPureActionScript()) {
            loadTheme(project, actionScriptPropertiesElement);
          }
        }
      }
      catch (JDOMException ignored) {/*ignore*/}
      catch (IOException ignored) {/*ignore*/}
    }
  }

  private static void loadInfoFromDotFlexLibPropertiesFile(final FlashBuilderProject project, final VirtualFile dotProjectFile) {
    final VirtualFile dotFlexLibPropertiesFile = dotProjectFile.getParent().findChild(FlashBuilderImporter.DOT_FLEX_LIB_PROPERTIES);
    if (dotFlexLibPropertiesFile != null) {
      try {
        final Element flexLibPropertiesElement = JDOMUtil.load(dotFlexLibPropertiesFile.getInputStream());
        if (!FLEX_LIB_PROPERTIES_TAG.equals(flexLibPropertiesElement.getName())) return;

        if (project.getTargetPlatform() == TargetPlatform.Desktop &&
            "true".equals(flexLibPropertiesElement.getAttributeValue(USE_MULTIPLATFORM_CONFIG_ATTR))) {
          project.setTargetPlatform(TargetPlatform.Mobile);
        }

        for (final Element namespaceManifestsElement : flexLibPropertiesElement
          .getChildren(NAMESPACE_MANIFESTS_TAG, flexLibPropertiesElement.getNamespace())) {
          for (final Element namespaceManifestEntryElement : namespaceManifestsElement
            .getChildren(NAMESPACE_MANIFEST_ENTRY_TAG, namespaceManifestsElement.getNamespace())) {
            final String namespace = namespaceManifestEntryElement.getAttributeValue(NAMESPACE_ATTR);
            final String manifestPath = namespaceManifestEntryElement.getAttributeValue(MANIFEST_ATTR);
            if (!StringUtil.isEmpty(manifestPath) && !StringUtil.isEmpty(namespace)) {
              project.addNamespaceAndManifestPath(namespace, FileUtil.toSystemIndependentName(manifestPath));
            }
          }
        }

        for (final Element includeResourcesElement : flexLibPropertiesElement
          .getChildren(INCLUDE_RESOURCES_ELEMENT, flexLibPropertiesElement.getNamespace())) {
          for (final Element resourceEntryElement : includeResourcesElement
            .getChildren(RESOURCE_ENTRY_ELEMENT, includeResourcesElement.getNamespace())) {

            final String sourcePath = resourceEntryElement.getAttributeValue(SOURCE_PATH_ATTR);
            if (!StringUtil.isEmpty(sourcePath)) {
              project.addFileIncludedInSwc(FileUtil.toSystemIndependentName(sourcePath));
            }
          }
        }
      }
      catch (JDOMException ignored) {/*ignore*/}
      catch (IOException ignored) {/*ignore*/}
    }
  }

  private static void loadProjectType(final FlashBuilderProject flashBuilderProject,
                                      final VirtualFile dotProjectFile,
                                      final Element compilerElement, Element parentElement) {
    final VirtualFile dir = dotProjectFile.getParent();
    assert dir != null;

    final VirtualFile flexLibPropertiesFile = dir.findChild(FlashBuilderImporter.DOT_FLEX_LIB_PROPERTIES);
    final boolean airSdk = "true".equals(compilerElement.getAttributeValue(USE_FLASH_SDK));
    flashBuilderProject.setAirSdk(airSdk);
    flashBuilderProject.setPureActionScript(airSdk || dir.findChild(FlashBuilderImporter.DOT_FLEX_PROPERTIES) == null &&
                                                      flexLibPropertiesFile == null);
    if (flexLibPropertiesFile == null) {
      for (final Element buildTargetsElement : parentElement
                                                                     .getChildren(BUILD_TARGETS_ELEMENT, parentElement.getNamespace())) {
        for (final Element buildTargetElement : buildTargetsElement
                                                                      .getChildren(BUILD_TARGET_ELEMENT, parentElement.getNamespace())) {
          final String buildTarget = buildTargetElement.getAttributeValue(BUILD_TARGET_NAME_ATTR);
          final String platformId1 = buildTargetElement.getAttributeValue(PLATFORM_ID_1);
          final String platformId2 = getMultiPlatformId(buildTargetElement);

          if (ANDROID_PLATFORM_ATTR_VALUE.equals(buildTarget) ||
              ANDROID_PLATFORM_ATTR_VALUE.equals(platformId1) ||
              ANDROID_PLATFORM_ATTR_VALUE.equals(platformId2)) {
            flashBuilderProject.setTargetPlatform(TargetPlatform.Mobile);
            flashBuilderProject.setAndroidSupported(isPlatformEnabled(buildTargetElement));
            loadSigningOptions(flashBuilderProject, buildTargetElement, TargetPlatform.Mobile, false);
            loadFilesExcludedFromPackage(flashBuilderProject, buildTargetElement, TargetPlatform.Mobile, false);
          }
          else if (IOS_PLATFORM_ATTR_VALUE.equals(buildTarget) ||
                   IOS_PLATFORM_ATTR_VALUE.equals(platformId1) ||
                   IOS_PLATFORM_ATTR_VALUE.equals(platformId2)) {
            flashBuilderProject.setTargetPlatform(TargetPlatform.Mobile);
            flashBuilderProject.setIosSupported(isPlatformEnabled(buildTargetElement));
            loadSigningOptions(flashBuilderProject, buildTargetElement, TargetPlatform.Mobile, true);
            loadFilesExcludedFromPackage(flashBuilderProject, buildTargetElement, TargetPlatform.Mobile, true);
          }
          else if (BLACKBERRY_PLATFORM_ATTR_VALUE.equals(buildTarget) ||
                   BLACKBERRY_PLATFORM_ATTR_VALUE.equals(platformId1) ||
                   BLACKBERRY_PLATFORM_ATTR_VALUE.equals(platformId2)) {
            flashBuilderProject.setTargetPlatform(TargetPlatform.Mobile);
          }
          else {
            loadSigningOptions(flashBuilderProject, buildTargetElement, TargetPlatform.Desktop, false);
            loadFilesExcludedFromPackage(flashBuilderProject, buildTargetElement, TargetPlatform.Desktop, false);
          }
        }
      }
    }
    else {
      // if this is Mobile library its target platform will be read a bit later in loadInfoFromDotFlexLibPropertiesFile()}
    }

    if (flashBuilderProject.getTargetPlatform() == TargetPlatform.Mobile) {
      return;
    }

    if ("true".equals(compilerElement.getAttributeValue(USE_APOLLO_CONFIG_ATTR))) {
      flashBuilderProject.setTargetPlatform(TargetPlatform.Desktop);
    }
    else {
      flashBuilderProject.setTargetPlatform(TargetPlatform.Web);
    }
  }

  private static boolean isPlatformEnabled(final Element buildTargetElement) {
    final Element multiPlatformSettings = buildTargetElement.getChild(MULTI_PLATFORM_SETTINGS_ELEMENT, buildTargetElement.getNamespace());
    return multiPlatformSettings != null && "true".equals(multiPlatformSettings.getAttributeValue(ENABLED_ATTR));
  }

  @Nullable
  private static String getMultiPlatformId(final Element buildTargetElement) {
    final Element multiPlatformSettings = buildTargetElement.getChild(MULTI_PLATFORM_SETTINGS_ELEMENT, buildTargetElement.getNamespace());
    return multiPlatformSettings == null ? null : multiPlatformSettings.getAttributeValue(PLATFORM_ID_2);
  }

  private static void loadSigningOptions(final FlashBuilderProject fbProject,
                                         final Element buildTargetElement,
                                         final TargetPlatform targetPlatform,
                                         final boolean iOS) {
    final Element airSettingsElement = buildTargetElement.getChild(AIR_SETTINGS_ELEMENT, buildTargetElement.getNamespace());
    if (airSettingsElement == null) return;

    final String certPath = airSettingsElement.getAttributeValue(AIR_CERTIFICATE_ATTR);
    if (certPath != null) {
      if (targetPlatform == TargetPlatform.Desktop) {
        fbProject.setDesktopCertPath(FileUtil.toSystemIndependentName(certPath));
      }
      else {
        if (iOS) {
          fbProject.setIOSCertPath(FileUtil.toSystemIndependentName(certPath));
        }
        else {
          fbProject.setAndroidCertPath(FileUtil.toSystemIndependentName(certPath));
        }
      }
    }

    if (targetPlatform == TargetPlatform.Mobile && iOS) {
      final String provisioningPath = buildTargetElement.getAttributeValue(PROVISIONING_FILE_ATTR);
      if (provisioningPath != null) {
        fbProject.setIOSProvisioningPath(provisioningPath);
      }
    }
  }

  private static void loadFilesExcludedFromPackage(final FlashBuilderProject fbProject,
                                                   final Element buildTargetElement,
                                                   final TargetPlatform targetPlatform,
                                                   final boolean iOS) {
    final Element airSettingsElement = buildTargetElement.getChild(AIR_SETTINGS_ELEMENT, buildTargetElement.getNamespace());
    final Element airExcludesElement = airSettingsElement == null
                                       ? null
                                       : airSettingsElement.getChild(AIR_EXCLUDES_ELEMENT, airSettingsElement.getNamespace());
    if (airExcludesElement == null) return;

    for (Element pathEntryElement : airExcludesElement
                                                          .getChildren(PATH_ENTRY_ELEMENT, airExcludesElement.getNamespace())) {
      final String path = pathEntryElement.getAttributeValue(PATH_ATTR);
      if (!StringUtil.isEmptyOrSpaces(path)) {
        if (targetPlatform == TargetPlatform.Mobile) {
          if (iOS) {
            fbProject.addPathExcludedFromIOSPackaging(path);
          }
          else {
            fbProject.addPathExcludedFromAndroidPackaging(path);
          }
        }
        else if (targetPlatform == TargetPlatform.Desktop) {
          fbProject.addPathExcludedFromDesktopPackaging(path);
        }
      }
    }
  }

  private static void loadOutputType(final FlashBuilderProject project, final VirtualFile dotProjectFile) {
    final VirtualFile dir = dotProjectFile.getParent();
    assert dir != null;
    project.setOutputType(dir.findChild(FlashBuilderImporter.DOT_FLEX_LIB_PROPERTIES) == null
                          ? OutputType.Application
                          : OutputType.Library);
  }

  private static void loadProjectRoot(final FlashBuilderProject project, final VirtualFile dotProjectFile) {
    final VirtualFile dir = dotProjectFile.getParent();
    assert dir != null;
    project.setProjectRootPath(dir.getPath());
  }

  private static void loadSourcePaths(final FlashBuilderProject project, final Element compilerElement) {
    final String sourceFolderPath = compilerElement.getAttributeValue(SOURCE_FOLDER_PATH_ATTR);
    if (!StringUtil.isEmptyOrSpaces(sourceFolderPath)) {
      project.addSourcePath(FileUtil.toSystemIndependentName(sourceFolderPath));
    }
    for (final Element compilerSourcePathElement : compilerElement.getChildren(COMPILER_SOURCE_PATH_TAG)) {
      for (final Element compilerSourcePathEntryElement : compilerSourcePathElement
        .getChildren(COMPILER_SOURCE_PATH_ENTRY_TAG)) {
        final String sourcePath = compilerSourcePathEntryElement.getAttributeValue(PATH_ATTR);
        if (!StringUtil.isEmptyOrSpaces(sourcePath)) {
          project.addSourcePath(FileUtil.toSystemIndependentName(sourcePath));
        }
      }
    }
  }

  private static void loadOutputFolderPath(final FlashBuilderProject project, final Element compilerElement) {
    final String outputFolderLocation = compilerElement.getAttributeValue(OUTPUT_FOLDER_LOCATION_ATTR);
    if (!StringUtil.isEmptyOrSpaces(outputFolderLocation)) {
      project.setOutputFolderPath(FileUtil.toSystemIndependentName(outputFolderLocation));
    }
    else {
      final String outputFolderPath = compilerElement.getAttributeValue(OUTPUT_FOLDER_PATH_ATTR);
      if (!StringUtil.isEmptyOrSpaces(outputFolderPath)) {
        project.setOutputFolderPath(FileUtil.toSystemIndependentName(outputFolderPath));
      }
    }
  }

  private static void loadMainClassName(final FlashBuilderProject project, final Element actionScriptPropertiesElement) {
    final String mainAppPath = actionScriptPropertiesElement.getAttributeValue(MAIN_APP_PATH_ATTR);
    if (mainAppPath != null) {
      project.setMainAppClassName(getClassName(mainAppPath));
    }
  }

  public static String getClassName(final @NotNull String path) {
    final String fqn = path.replace('/', '.').trim();
    final int lastDotIndex = fqn.lastIndexOf('.');
    final String lowercased = StringUtil.toLowerCase(fqn);
    return (lastDotIndex >= 0 && (lowercased.endsWith(".mxml") || lowercased.endsWith(".as"))) ? fqn.substring(0, lastDotIndex) : fqn;
  }

  private static void loadTargetPlayerVersion(final FlashBuilderProject project, final Element compilerElement) {
    final String version = compilerElement.getAttributeValue(TARGET_PLAYER_VERSION_ATTR);
    if (version != null && !version.startsWith("0")) {
      project.setTargetPlayerVersion(version);
    }
  }

  private static void loadAdditionalCompilerArguments(final FlashBuilderProject project, final Element compilerElement) {
    final String arguments = compilerElement.getAttributeValue(ADDITIONAL_COMPILER_ARGUMENTS_ATTR);
    if (!StringUtil.isEmptyOrSpaces(arguments)) {
      project.setAdditionalCompilerOptions(arguments.replace('\n', ' ').replace('\r', ' ').replace('\t', ' '));
    }
  }

  private static void loadDependenciesAndCheckIfSdkUsed(final FlashBuilderProject project,
                                                        final Element compilerElement,
                                                        final Map<String, String> pathReplacementMap) {
    for (final Element libraryPathElement : compilerElement.getChildren(LIBRARY_PATH_TAG)) {
      for (final Element libraryPathEntryElement : libraryPathElement.getChildren(LIBRARY_PATH_ENTRY_TAG)) {
        final String libraryKind = StringUtil.notNullize(libraryPathEntryElement.getAttributeValue(LIBRARY_KIND_ATTR), SWC_FILE_KIND);
        if (libraryKind.equals(USE_SDK_KIND)) {
          project.setSdkUsed(true);
        }
        else {
          final String libraryPath = libraryPathEntryElement.getAttributeValue(PATH_ATTR);
          if (!StringUtil.isEmptyOrSpaces(libraryPath)) {
            if (SWC_FILE_KIND.equals(libraryKind) || SWC_FOLDER_KIND.equals(libraryKind) || ANE_KIND.equals(libraryKind)) {
              // TODO: parse sources
              final Collection<String> librarySourcePaths = new ArrayList<>();

              final String replacedPath = pathReplacementMap.get(libraryPath);
              String path = replacedPath != null ? replacedPath : libraryPath;

              if (path.startsWith(FLEXUNIT_LIB_MACRO)) {
                final String flexUnitFrameworkPath = guessFlexUnitFrameworkPath();
                if (flexUnitFrameworkPath != null) {
                  path = flexUnitFrameworkPath + "/libs" + path.substring(FLEXUNIT_LIB_MACRO.length());
                }
              }
              else if (path.equals(FLEXUNIT_LOCALE_MACRO)) {
                final String flexUnitFrameworkPath = guessFlexUnitFrameworkPath();
                if (flexUnitFrameworkPath != null) {
                  path = flexUnitFrameworkPath + "/locale/version4locale/FlexUnitTestRunner_rb.swc";
                }
              }

              project.addLibraryPathAndSources(FileUtil.toSystemIndependentName(path), librarySourcePaths);
            }
          }
        }
      }
    }
  }

  private static String guessFlexUnitFrameworkPath() {
    final String fbPath = FlashBuilderSdkFinder.findFBInstallationPath();
    if (fbPath == null) return null;

    final File pluginsDir = new File(fbPath + "/eclipse/plugins");
    if (!pluginsDir.isDirectory()) return null;

    final File[] flexUnitDirs = pluginsDir.listFiles((dir, name) -> name.startsWith("com.adobe.flexbuilder.flexunit_"));

    for (File flexUnitDir : flexUnitDirs) {
      final String flexUnitLibPath = flexUnitDir.getPath() + "/flexunitframework";
      if (new File(flexUnitLibPath).isDirectory()) {
        return flexUnitLibPath;
      }
    }
    return null;
  }

  private static void loadSdkName(final FlashBuilderProject project, final Element compilerElement) {
    final String sdkName = compilerElement.getAttributeValue(FLEX_SDK_ATTR);
    if (!StringUtil.isEmptyOrSpaces(sdkName)) {
      project.setSdkName(sdkName);
    }
  }

  private static void loadApplications(final FlashBuilderProject project, final Element actionScriptPropertiesElement) {
    for (final Element applicationsElement : actionScriptPropertiesElement.getChildren(APPLICATIONS_ELEMENT)) {
      for (final Element applicationElement : applicationsElement.getChildren(APPLICATION_ELEMENT)) {
        final String path = applicationElement.getAttributeValue(PATH_ATTR);
        final String className = path == null ? null : getClassName(path);
        if (className != null && !"FlexUnitApplication".equals(className) && !"FlexUnitCompilerApplication".equals(className)) {
          project.addApplicationClassName(className);
        }
      }
    }
  }

  private static void loadModules(final FlashBuilderProject project, final Element actionScriptPropertiesElement) {
    for (final Element modulesElement : actionScriptPropertiesElement.getChildren(MODULES_ELEMENT)) {
      for (final Element moduleElement : modulesElement.getChildren(MODULE_ELEMENT)) {
        final String mainClassPath = moduleElement.getAttributeValue(SOURCE_PATH_ATTR);
        final String outputPath = moduleElement.getAttributeValue(DEST_PATH_ATTR);
        final String optimize = moduleElement.getAttributeValue(OPTIMIZE_ATTR);
        final String optimizeFor = moduleElement.getAttributeValue(APPLICATION_ATTR);
        if (!StringUtil.isEmpty(mainClassPath) && !StringUtil.isEmpty(DEST_PATH_ATTR) && !StringUtil.isEmpty(optimizeFor)) {
          project.addModule(new FlashBuilderProject.FBRLMInfo(mainClassPath, outputPath, "true".equalsIgnoreCase(optimize), optimizeFor));
        }
      }
    }
  }

  private static void loadCssFilesToCompile(final FlashBuilderProject project, final Element actionScriptPropertiesElement) {
    for (final Element buildSccFilesElement : actionScriptPropertiesElement.getChildren(BUILD_CSS_FILES_ELEMENT)) {
      for (final Element buildCssFileEntryElement : buildSccFilesElement.getChildren(BUILD_CSS_FILE_ENTRY_ELEMENT)) {
        final String sourcePath = buildCssFileEntryElement.getAttributeValue(SOURCE_PATH_ATTR);
        if (!StringUtil.isEmpty(sourcePath)) {
          project.addCssFileToCompile(FileUtil.toSystemIndependentName(sourcePath));
        }
      }
    }
  }

  private static void loadTheme(final FlashBuilderProject project, final Element actionScriptPropertiesElement) {
    final Element themeElement = actionScriptPropertiesElement.getChild(THEME_ELEMENT, actionScriptPropertiesElement.getNamespace());
    final String defaultThemeAttr = themeElement == null ? null : themeElement.getAttributeValue(DEFAULT_THEME_ATTR);
    if ("false".equals(defaultThemeAttr)) {
      final String themeDirPathRaw = themeElement.getAttributeValue(THEME_LOCATION_ATTR);
      if (themeDirPathRaw != null) {
        project.setThemeDirPathRaw(themeDirPathRaw);
      }
    }
  }
}