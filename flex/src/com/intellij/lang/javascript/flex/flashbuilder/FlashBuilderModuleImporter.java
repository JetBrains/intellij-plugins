// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.flashbuilder;

import com.intellij.flex.FlexCommonUtils;
import com.intellij.flex.model.bc.*;
import com.intellij.lang.javascript.flex.FlexModuleBuilder;
import com.intellij.lang.javascript.flex.library.FlexLibraryProperties;
import com.intellij.lang.javascript.flex.library.FlexLibraryType;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableBuildConfigurationEntry;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableFlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableModuleLibraryEntry;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.Factory;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexProjectConfigurationEditor;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.lang.javascript.flex.projectStructure.ui.CreateHtmlWrapperTemplateDialog;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathMacros;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.impl.ProjectMacrosUtil;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootModel;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.*;
import com.intellij.util.SystemProperties;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FlashBuilderModuleImporter {

  private static final String CORE_RESOURCES_PREFS_REL_PATH =
    "/.metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.core.resources.prefs";
  private static final String PATHVARIABLE_DOT = "pathvariable.";

  private static final String SDK_THEMES_DIR_MACRO = "${SDK_THEMES_DIR}";
  private static final String EXTERNAL_THEME_DIR_MACRO = "${EXTERNAL_THEME_DIR}";
  private static final String EXTERNAL_THEME_DIR_REL_PATH = SystemInfo.isWindows
                                                            ? "/AppData/Roaming/Adobe/Flash Builder/Themes"
                                                            : "/Library/Application Support/Adobe/Flash Builder/Themes";

  private final Project myIdeaProject;
  private final FlexProjectConfigurationEditor myFlexConfigEditor;
  private final Collection<? extends FlashBuilderProject> myAllFBProjects;
  private final FlashBuilderSdkFinder mySdkFinder;
  private final Set<String> myPathVariables;
  private boolean myPathVariablesInitialized = false;

  public FlashBuilderModuleImporter(final Project ideaProject,
                                    final FlexProjectConfigurationEditor flexConfigEditor,
                                    final Collection<? extends FlashBuilderProject> allFBProjects,
                                    final FlashBuilderSdkFinder sdkFinder) {
    myIdeaProject = ideaProject;
    myFlexConfigEditor = flexConfigEditor;
    myAllFBProjects = allFBProjects;
    mySdkFinder = sdkFinder;

    myPathVariables = new HashSet<>();
    for (final FlashBuilderProject flashBuilderProject : allFBProjects) {
      myPathVariables.addAll(flashBuilderProject.getUsedPathVariables());
    }
  }

  public void setupModule(final ModifiableRootModel rootModel, final FlashBuilderProject flashBuilderProject) {
    setupRoots(rootModel, flashBuilderProject);
    setupBuildConfigs(rootModel, flashBuilderProject);
  }

  private void setupBuildConfigs(final ModuleRootModel rootModel, final FlashBuilderProject fbProject) {
    final Sdk sdk = fbProject.isSdkUsed() ? mySdkFinder.findSdk(fbProject) : null;

    final ModifiableFlexBuildConfiguration[] configurations = myFlexConfigEditor.getConfigurations(rootModel.getModule());
    assert configurations.length == 1;
    final ModifiableFlexBuildConfiguration mainBC = configurations[0];

    final String bcName = suggestMainBCName(fbProject);
    mainBC.setName(bcName);

    final TargetPlatform targetPlatform = fbProject.getTargetPlatform();
    mainBC.setTargetPlatform(targetPlatform);
    mainBC.setPureAs(fbProject.isPureActionScript());
    mainBC.setOutputType(fbProject.getOutputType());

    final Map<String, String> compilerOptions = new HashMap<>();

    if (fbProject.getOutputType() == OutputType.Application) {
      mainBC.setMainClass(fbProject.getMainAppClassName());

      final String shortClassName = StringUtil.getShortName(fbProject.getMainAppClassName());
      mainBC.setOutputFileName(shortClassName + ".swf");

      // todo dependencies.setComponentSet();

      if (!mainBC.isPureAs() && fbProject.getThemeDirPathRaw() != null && sdk != null) {
        setupTheme(fbProject.getThemeDirPathRaw(), mainBC.getNature(), sdk, mainBC.getDependencies().getComponentSet(), compilerOptions);
      }

      switch (targetPlatform) {
        case Web:
          if (fbProject.isUseHtmlWrapper()) {
            mainBC.setUseHtmlWrapper(true);
            mainBC.setWrapperTemplatePath(fbProject.getProjectRootPath() + "/" + CreateHtmlWrapperTemplateDialog.HTML_TEMPLATE_FOLDER_NAME);
          }
          break;
        case Desktop:
          setupAirDescriptor(mainBC, rootModel);
          mainBC.getAirDesktopPackagingOptions().setPackageFileName(shortClassName);
          if (!StringUtil.isEmpty(fbProject.getDesktopCertPath())) {
            mainBC.getAirDesktopPackagingOptions().getSigningOptions().setUseTempCertificate(false);
            mainBC.getAirDesktopPackagingOptions().getSigningOptions().setKeystorePath(fbProject.getDesktopCertPath());
          }
          FilesToPackageUtil
            .setupFilesToPackage(mainBC.getAirDesktopPackagingOptions(), fbProject.getPathsExcludedFromDesktopPackaging(), rootModel);
          break;
        case Mobile:
          setupAirDescriptor(mainBC, rootModel);

          mainBC.getAndroidPackagingOptions().setEnabled(fbProject.isAndroidSupported());
          mainBC.getAndroidPackagingOptions().setPackageFileName(shortClassName);
          if (!StringUtil.isEmpty(fbProject.getAndroidCertPath())) {
            mainBC.getAndroidPackagingOptions().getSigningOptions().setUseTempCertificate(false);
            mainBC.getAndroidPackagingOptions().getSigningOptions().setKeystorePath(fbProject.getAndroidCertPath());
          }
          FilesToPackageUtil
            .setupFilesToPackage(mainBC.getAndroidPackagingOptions(), fbProject.getPathsExcludedFromAndroidPackaging(), rootModel);

          mainBC.getIosPackagingOptions().setEnabled(fbProject.isIosSupported());
          mainBC.getIosPackagingOptions().setPackageFileName(shortClassName);
          mainBC.getIosPackagingOptions().getSigningOptions()
            .setProvisioningProfilePath(StringUtil.notNullize(fbProject.getIOSProvisioningPath()));
          mainBC.getIosPackagingOptions().getSigningOptions().setKeystorePath(StringUtil.notNullize(fbProject.getIOSCertPath()));
          FilesToPackageUtil.setupFilesToPackage(mainBC.getIosPackagingOptions(), fbProject.getPathsExcludedFromIOSPackaging(), rootModel);
          break;
      }
    }
    else {
      mainBC.setOutputFileName(fbProject.getName() + ".swc");
    }

    mainBC.setOutputFolder(getAbsolutePathWithLinksHandled(fbProject, fbProject.getOutputFolderPath()));

    setupRLMsAndCSSFilesToCompile(mainBC, fbProject);

    if (sdk != null) {
      mainBC.getDependencies().setSdkEntry(Factory.createSdkEntry(sdk.getName()));

      final String sdkHome = sdk.getHomePath();
      if (targetPlatform == TargetPlatform.Web && sdkHome != null) {
        mainBC.getDependencies().setTargetPlayer(FlexSdkUtils.getTargetPlayer(fbProject.getTargetPlayerVersion(), sdkHome));
      }
    }

    // todo dependencies.setFrameworkLinkage();

    setupDependencies(mainBC, fbProject);

    final String fbOptions = fbProject.getAdditionalCompilerOptions();

    setupLocales(fbOptions, compilerOptions);

    setupNamespacesAndManifests(rootModel, fbProject, compilerOptions);

    setupFilesToIncludeInSwc(rootModel, mainBC, fbProject.getFilesIncludedInSwc());

    mainBC.getCompilerOptions().setAllOptions(compilerOptions);

    // todo parse options, replace "-a b" to "-a=b", move some to dedicated fields
    final String ideaOptions = FlexCommonUtils.removeOptions(fbOptions, "locale", "compiler.locale", "source-path", "compiler.source-path");
    mainBC.getCompilerOptions().setAdditionalOptions(ideaOptions);

    if (mainBC.getOutputType() == OutputType.Application) {
      FlexModuleBuilder.createRunConfiguration(rootModel.getModule(), mainBC);
    }

    setupOtherAppsAndModules(rootModel, mainBC, fbProject);
  }

  private void setupRLMsAndCSSFilesToCompile(final ModifiableFlexBuildConfiguration mainBC, final FlashBuilderProject fbProject) {
    if (BCUtils.canHaveRLMsAndRuntimeStylesheets(mainBC) && !fbProject.getCssFilesToCompile().isEmpty()) {
      final Collection<String> cssPaths = new ArrayList<>();
      for (final String path : fbProject.getCssFilesToCompile()) {
        final String cssPath = getAbsolutePathWithLinksHandled(fbProject, path);
        final VirtualFile cssFile = LocalFileSystem.getInstance().findFileByPath(cssPath);
        if (cssFile != null) {
          cssPaths.add(cssFile.getPath());
        }
        else if (ApplicationManager.getApplication().isUnitTestMode()) {
          cssPaths.add(cssPath);
        }
      }
      mainBC.setCssFilesToCompile(cssPaths);
    }
  }

  private static void setupLocales(final String fbOptions, final Map<String, String> compilerOptions) {
    final List<String> locales = FlexCommonUtils.getOptionValues(fbOptions, "locale", "compiler.locale");

    final StringBuilder localesBuf = new StringBuilder();
    for (String locale : locales) {
      if (localesBuf.length() > 0) {
        localesBuf.append(CompilerOptionInfo.LIST_ENTRIES_SEPARATOR);
      }
      localesBuf.append(locale);
    }
    compilerOptions.put("compiler.locale", localesBuf.toString());
  }

  private void setupNamespacesAndManifests(final ModuleRootModel rootModel,
                                           final FlashBuilderProject fbProject,
                                           final Map<String, String> compilerOptions) {
    if (!fbProject.getNamespacesAndManifestPaths().isEmpty()) {
      final StringBuilder nsBuf = new StringBuilder();
      for (Pair<String, String> nsAndManifestPath : fbProject.getNamespacesAndManifestPaths()) {
        final String manifestPath = nsAndManifestPath.second;
        VirtualFile manifestFile = null;
        for (final VirtualFile sourceRoot : rootModel.getSourceRoots()) {
          if ((manifestFile = sourceRoot.findFileByRelativePath(manifestPath)) != null) {
            break;
          }
        }
        final String resolvedManifestPath = manifestFile != null ? manifestFile.getPath()
                                                                 : getAbsolutePathWithLinksHandled(fbProject, manifestPath);


        if (nsBuf.length() > 0) {
          nsBuf.append(CompilerOptionInfo.LIST_ENTRIES_SEPARATOR);
        }
        nsBuf.append(nsAndManifestPath.first).append(CompilerOptionInfo.LIST_ENTRY_PARTS_SEPARATOR);
        nsBuf.append(resolvedManifestPath);
      }

      compilerOptions.put("compiler.namespaces.namespace", nsBuf.toString());
    }
  }

  private static void setupFilesToIncludeInSwc(final ModuleRootModel rootModel,
                                               final ModifiableFlexBuildConfiguration bc,
                                               final Collection<String> paths) {
    if (bc.getOutputType() == OutputType.Library) {
      bc.getCompilerOptions().setFilesToIncludeInSWC(ContainerUtil.mapNotNull(paths, path -> {
        for (VirtualFile srcRoot : rootModel.getSourceRoots()) {
          final VirtualFile assetFile = LocalFileSystem.getInstance().findFileByPath(srcRoot.getPath() + "/" + path);
          if (assetFile != null) {
            return assetFile.getPath();
          }
        }
        return null;
      }));
    }
  }

  private static void setupTheme(final String themeDirPathRaw,
                                 final BuildConfigurationNature nature,
                                 final Sdk sdk,
                                 final ComponentSet componentSet,
                                 final Map<String, String> compilerOptions) {
    String themeDirPath = null;

    if (themeDirPathRaw.startsWith(EXTERNAL_THEME_DIR_MACRO)) {
      themeDirPath =
        SystemProperties.getUserHome() + EXTERNAL_THEME_DIR_REL_PATH + themeDirPathRaw.substring(EXTERNAL_THEME_DIR_MACRO.length());
    }
    else if (themeDirPathRaw.startsWith(SDK_THEMES_DIR_MACRO)) {
      themeDirPath = sdk.getHomePath() + themeDirPathRaw.substring(SDK_THEMES_DIR_MACRO.length());
    }

    if (themeDirPath != null) {
      final File themeDir = new File(themeDirPath);
      if (themeDir.isDirectory()) {
        final String themeFilePath = findThemeFilePath(themeDir);
        if (themeFilePath != null) {
          final String themePathWithMacro = themeFilePath.replace(sdk.getHomePath(), CompilerOptionInfo.FLEX_SDK_MACRO);

          if ("${SDK_THEMES_DIR}/frameworks/themes/Halo".equals(themeDirPathRaw)) {
            compilerOptions.put("compiler.theme", themePathWithMacro);
          }
          else if ("${SDK_THEMES_DIR}/frameworks/themes/AeonGraphical".equals(themeDirPathRaw)) {
            final String haloTheme = CompilerOptionInfo.FLEX_SDK_MACRO + "/frameworks/themes/Halo/halo.swc";
            compilerOptions.put("compiler.theme", haloTheme + CompilerOptionInfo.LIST_ENTRIES_SEPARATOR + themePathWithMacro);
          }
          else {
            final CompilerOptionInfo themeInfo = CompilerOptionInfo.getOptionInfo("compiler.theme");
            final String defaultTheme = themeInfo.getDefaultValue(sdk.getVersionString(), nature, componentSet);

            if (defaultTheme.isEmpty()) {
              compilerOptions.put("compiler.theme", themePathWithMacro);
            }
            else {
              compilerOptions.put("compiler.theme", defaultTheme + CompilerOptionInfo.LIST_ENTRIES_SEPARATOR + themePathWithMacro);
            }
          }
        }
      }
    }
  }

  @Nullable
  private static String findThemeFilePath(final File themeDir) {
    final String fileName = ContainerUtil.find(themeDir.list(),
                                               path -> FileUtilRt.extensionEquals(path, "css") || FileUtilRt.extensionEquals(path, "swc"));
    return fileName == null ? null : FileUtil.toSystemIndependentName(themeDir + "/" + fileName);
  }

  private static void setupAirDescriptor(final ModifiableFlexBuildConfiguration bc, final ModuleRootModel rootModel) {
    if (bc.getTargetPlatform() == TargetPlatform.Desktop) {
      bc.getAirDesktopPackagingOptions().setUseGeneratedDescriptor(true);
    }
    else {
      bc.getAndroidPackagingOptions().setUseGeneratedDescriptor(true);
      bc.getIosPackagingOptions().setUseGeneratedDescriptor(true);
    }

    final String descriptorRelPath = bc.getMainClass().replace('.', '/') + "-app.xml";

    for (VirtualFile srcRoot : rootModel.getSourceRoots()) {
      if (srcRoot.findFileByRelativePath(descriptorRelPath) != null) {
        if (bc.getTargetPlatform() == TargetPlatform.Desktop) {
          bc.getAirDesktopPackagingOptions().setUseGeneratedDescriptor(false);
          bc.getAirDesktopPackagingOptions().setCustomDescriptorPath(srcRoot.getPath() + "/" + descriptorRelPath);
        }
        else {
          bc.getAndroidPackagingOptions().setUseGeneratedDescriptor(false);
          bc.getAndroidPackagingOptions().setCustomDescriptorPath(srcRoot.getPath() + "/" + descriptorRelPath);
          bc.getIosPackagingOptions().setUseGeneratedDescriptor(false);
          bc.getIosPackagingOptions().setCustomDescriptorPath(srcRoot.getPath() + "/" + descriptorRelPath);
        }
        break;
      }
    }
  }

  private void setupOtherAppsAndModules(final ModuleRootModel rootModel,
                                        final ModifiableFlexBuildConfiguration mainBC,
                                        final FlashBuilderProject fbProject) {
    final Collection<ModifiableFlexBuildConfiguration> allApps = new ArrayList<>();
    allApps.add(mainBC);

    for (String mainClass : fbProject.getApplicationClassNames()) {
      final ModifiableFlexBuildConfiguration bc = myFlexConfigEditor.copyConfiguration(mainBC, mainBC.getNature());
      final String shortClassName = StringUtil.getShortName(mainClass);
      bc.setName(shortClassName);
      bc.setMainClass(mainClass);
      bc.setOutputFileName(shortClassName + ".swf");

      if (bc.getTargetPlatform() == TargetPlatform.Desktop) {
        setupAirDescriptor(bc, rootModel);
        bc.getAirDesktopPackagingOptions().setPackageFileName(shortClassName);
      }

      if (bc.getTargetPlatform() == TargetPlatform.Mobile) {
        setupAirDescriptor(bc, rootModel);

        bc.getAndroidPackagingOptions().setPackageFileName(shortClassName);
        bc.getIosPackagingOptions().setPackageFileName(shortClassName);
      }

      FlexModuleBuilder.createRunConfiguration(rootModel.getModule(), bc);

      allApps.add(bc);
    }

    if (BCUtils.canHaveRLMsAndRuntimeStylesheets(mainBC)) {
      setupModules(rootModel, allApps, fbProject);
    }
  }

  private void setupModules(final ModuleRootModel rootModel,
                            final Collection<ModifiableFlexBuildConfiguration> apps,
                            final FlashBuilderProject fbProject) {
    for (final FlashBuilderProject.FBRLMInfo rlm : fbProject.getModules()) {
      ModifiableFlexBuildConfiguration hostApp = apps.iterator().next();
      if (rlm.OPTIMIZE) {
        final String hostAppMainClass = getMainClassFqn(fbProject, rlm.OPTIMIZE_FOR, rootModel.getSourceRootUrls());
        for (ModifiableFlexBuildConfiguration appBC : apps) {
          if (hostAppMainClass.equals(appBC.getMainClass())) {
            hostApp = appBC;
            break;
          }
        }
      }

      final Collection<FlexBuildConfiguration.RLMInfo> rlms = new ArrayList<>(hostApp.getRLMs());
      final String rlmMainClass = getMainClassFqn(fbProject, rlm.MAIN_CLASS_PATH, rootModel.getSourceRootUrls());
      rlms.add(new FlexBuildConfiguration.RLMInfo(rlmMainClass, rlm.OUTPUT_PATH, rlm.OPTIMIZE));
      hostApp.setRLMs(rlms);
    }
  }

  private static String suggestMainBCName(final FlashBuilderProject fbProject) {
    return fbProject.getOutputType() == OutputType.Application && !fbProject.getMainAppClassName().isEmpty()
           ? StringUtil.getShortName(fbProject.getMainAppClassName())
           : fbProject.getName();
  }

  private String getMainClassFqn(final FlashBuilderProject flashBuilderProject,
                                 final String mainClassPath,
                                 final String[] sourceRootUrls) {
    final String mainClassPathUrl = VfsUtilCore.pathToUrl(getAbsolutePathWithLinksHandled(flashBuilderProject, mainClassPath));
    for (final String sourceRootUrl : sourceRootUrls) {
      if (mainClassPathUrl.startsWith(sourceRootUrl + "/")) {
        return FlashBuilderProjectLoadUtil.getClassName(mainClassPathUrl.substring(sourceRootUrl.length() + 1));
      }
    }

    return FlashBuilderProjectLoadUtil.getClassName(mainClassPathUrl.substring(mainClassPathUrl.lastIndexOf('/') + 1));
  }

  private void setupRoots(final ModifiableRootModel rootModel, final FlashBuilderProject fbProject) {
    final String mainContentEntryUrl = VfsUtilCore.pathToUrl(fbProject.getProjectRootPath());
    final ContentEntry mainContentEntry = rootModel.addContentEntry(mainContentEntryUrl);
    final Collection<ContentEntry> otherContentEntries = new ArrayList<>();

    final Collection<String> sourcePaths = fbProject.getSourcePaths();
    if (sourcePaths.isEmpty()) {
      final VirtualFile contentRoot = mainContentEntry.getFile();
      final String mainClass = fbProject.getMainAppClassName();
      if (contentRoot != null &&
          !StringUtil.isEmpty(mainClass) &&
          (contentRoot.findChild(mainClass + ".mxml") != null || contentRoot.findChild(mainClass + ".as") != null)) {
        mainContentEntry.addSourceFolder(mainContentEntry.getUrl(), false);
      }
    }
    else {
      final List<String> locales = FlexCommonUtils.getOptionValues(fbProject.getAdditionalCompilerOptions(), "locale", "compiler.locale");
      final List<String> moreSourcePaths =
        FlexCommonUtils.getOptionValues(fbProject.getAdditionalCompilerOptions(), "source-path", "compiler.source-path");

      for (final String rawSourcePath : sourcePaths) {
        if (rawSourcePath.contains(FlexCommonUtils.LOCALE_TOKEN)) {
          for (String locale : locales) {
            handleRawSourcePath(rootModel, fbProject, mainContentEntryUrl, mainContentEntry, otherContentEntries,
                                rawSourcePath.replace(FlexCommonUtils.LOCALE_TOKEN, locale));
          }
        }
        else {
          handleRawSourcePath(rootModel, fbProject, mainContentEntryUrl, mainContentEntry, otherContentEntries, rawSourcePath);
        }
      }

      for (String sourcePath : moreSourcePaths) {
        if (sourcePath.contains(FlexCommonUtils.LOCALE_TOKEN)) {
          for (String locale : locales) {
            final String path = getPathToSourceRootSetInAdditionalOptions(sourcePath.replace(FlexCommonUtils.LOCALE_TOKEN, locale),
                                                                          mainContentEntryUrl, mainContentEntry);

            if (path != null) {
              handleRawSourcePath(rootModel, fbProject, mainContentEntryUrl, mainContentEntry, otherContentEntries, path);
            }
          }
        }
        else {
          final String path = getPathToSourceRootSetInAdditionalOptions(sourcePath, mainContentEntryUrl, mainContentEntry);
          if (path != null) {
            handleRawSourcePath(rootModel, fbProject, mainContentEntryUrl, mainContentEntry, otherContentEntries, path);
          }
        }
      }
    }
  }

  @Nullable
  private static String getPathToSourceRootSetInAdditionalOptions(final String rawPath,
                                                                  final String mainContentEntryUrl,
                                                                  final ContentEntry mainContentEntry) {
    // sourcePath can be absolute or relative to project root or relative to main source root
    String path = rawPath;
    if (new File(path).isDirectory()) return path;

    path = VfsUtilCore.urlToPath(mainContentEntryUrl) + "/" + rawPath;
    if (new File(path).isDirectory()) return path;

    if (mainContentEntry.getSourceFolders().length > 0) {
      path = VfsUtilCore.urlToPath(mainContentEntry.getSourceFolders()[0].getUrl()) + "/" + rawPath;
      if (new File(path).isDirectory()) return path;
    }

    return null;
  }

  private void handleRawSourcePath(final ModifiableRootModel rootModel,
                                   final FlashBuilderProject fbProject,
                                   final String mainContentEntryUrl,
                                   final ContentEntry mainContentEntry,
                                   final Collection<ContentEntry> otherContentEntries,
                                   final String rawSourcePath) {
    final String sourcePath = getAbsolutePathWithLinksHandled(fbProject, rawSourcePath);
    final String sourceUrl = VfsUtilCore.pathToUrl(sourcePath);
    if (FileUtil.isAncestor(new File(VfsUtilCore.urlToPath(mainContentEntryUrl)), new File(VfsUtilCore.urlToPath(sourceUrl)), false)) {
      addSourceRoot(mainContentEntry, sourceUrl);
    }
    else {
      for (final ContentEntry otherContentEntry : otherContentEntries) {
        if (FileUtil.isAncestor(new File(VfsUtilCore.urlToPath(mainContentEntryUrl)), new File(VfsUtilCore.urlToPath(sourceUrl)), false)) {
          addSourceRoot(otherContentEntry, sourceUrl);
          return;
        }
      }

      final ContentEntry newContentEntry = rootModel.addContentEntry(sourceUrl);
      addSourceRoot(newContentEntry, sourceUrl);
      otherContentEntries.add(newContentEntry);
    }
  }

  private static void addSourceRoot(final ContentEntry contentEntry, final String sourceUrl) {
    final VirtualFile srcDir = LocalFileSystem.getInstance().findFileByPath(VfsUtilCore.urlToPath(sourceUrl));

    final Ref<Boolean> testClassesFound = Ref.create(false);
    final Ref<Boolean> nonTestClassesFound = Ref.create(false);

    if (srcDir != null && !"src".equals(srcDir.getName())) {
      VfsUtilCore.visitChildrenRecursively(srcDir, new VirtualFileVisitor<Void>() {
        @Override
        @NotNull
        public Result visitFileEx(@NotNull final VirtualFile file) {
          if (nonTestClassesFound.get()) {
            return SKIP_CHILDREN;
          }

          if (file.isDirectory()) {
            if ("flexUnitTests".equals(file.getName())) {
              testClassesFound.set(true);
              return SKIP_CHILDREN;
            }

            return CONTINUE;
          }
          else {
            final String ext = StringUtil.toLowerCase(StringUtil.notNullize(file.getExtension()));

            if ("mxml".equals(ext) || "fxg".equals(ext)) {
              nonTestClassesFound.set(true);
            }
            else if ("as".equals(ext)) {
              if (file.getNameWithoutExtension().endsWith("Test") || file.getName().contains("Test") && file.getName().contains("Suite")) {
                testClassesFound.set(true);
              }
              else {
                nonTestClassesFound.set(true);
              }
            }

            return CONTINUE;
          }
        }
      });
    }

    final boolean isTest = testClassesFound.get() && !nonTestClassesFound.get();
    contentEntry.addSourceFolder(sourceUrl, isTest);
  }

  private void setupDependencies(final ModifiableFlexBuildConfiguration bc, final FlashBuilderProject fbProject) {
    OUTER:
    for (final String libraryPathOrig : fbProject.getLibraryPaths()) {
      for (FlashBuilderProject otherProject : myAllFBProjects) {
        if (otherProject != fbProject && libraryPathOrig.startsWith("/" + otherProject.getName() + "/")) {
          final ModifiableBuildConfigurationEntry bcEntry =
            myFlexConfigEditor.createBcEntry(bc.getDependencies(), otherProject.getName(), suggestMainBCName(otherProject));
          bc.getDependencies().getModifiableEntries().add(0, bcEntry);
          continue OUTER;
        }
      }

      final LibraryTable.ModifiableModel libraryModel = myFlexConfigEditor.getLibraryModel(bc.getDependencies());

      final Library library = libraryModel.createLibrary(null, FlexLibraryType.FLEX_LIBRARY);

      final LibraryEx.ModifiableModelEx libraryModifiableModel = ((LibraryEx.ModifiableModelEx)library.getModifiableModel());
      final String libraryId = UUID.randomUUID().toString();
      libraryModifiableModel.setProperties(new FlexLibraryProperties(libraryId));

      final String libraryPath = getAbsolutePathWithLinksHandled(fbProject, libraryPathOrig);

      if (StringUtil.toLowerCase(libraryPath).endsWith(".swc") || StringUtil.toLowerCase(libraryPath).endsWith(".ane")) {
        libraryModifiableModel.addRoot(VirtualFileManager.constructUrl(JarFileSystem.PROTOCOL, libraryPath) + JarFileSystem.JAR_SEPARATOR,
                                       OrderRootType.CLASSES);
      }
      else {
        libraryModifiableModel.addJarDirectory(VfsUtilCore.pathToUrl(libraryPath), false);
      }

      for (final String librarySourcePath : fbProject.getLibrarySourcePaths(libraryPathOrig)) {
        libraryModifiableModel.addRoot(VfsUtilCore.pathToUrl(getAbsolutePathWithLinksHandled(fbProject, librarySourcePath)),
                                       OrderRootType.SOURCES);
      }

      ApplicationManager.getApplication().runWriteAction(() -> libraryModifiableModel.commit());

      final ModifiableModuleLibraryEntry libraryEntry = myFlexConfigEditor.createModuleLibraryEntry(bc.getDependencies(), libraryId);
      libraryEntry.getDependencyType().setLinkageType(LinkageType.Merged); // todo set correct linkage!
      bc.getDependencies().getModifiableEntries().add(libraryEntry);
    }
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

    final boolean absolute = FileUtil.isAbsolute(path) && (SystemInfo.isWindows || new File(path).exists());
    return absolute ? path : project.getProjectRootPath() + (slashIndex == 0 ? "" : "/") + path;
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
    final Map<String, String> eclipsePathVariables = new HashMap<>();
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
