package com.intellij.lang.javascript.flex.flashbuilder;

import com.intellij.lang.javascript.flex.FlexModuleBuilder;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.TargetPlayerUtils;
import com.intellij.lang.javascript.flex.library.FlexLibraryProperties;
import com.intellij.lang.javascript.flex.library.FlexLibraryType;
import com.intellij.lang.javascript.flex.projectStructure.CompilerOptionInfo;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.Factory;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexProjectConfigurationEditor;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.lang.javascript.flex.projectStructure.ui.CreateHtmlWrapperTemplateDialog;
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
import com.intellij.openapi.roots.impl.libraries.LibraryTableBase;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.*;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FlashBuilderModuleImporter {

  private static final String CORE_RESOURCES_PREFS_REL_PATH =
    "/.metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.core.resources.prefs";
  private static final String PATHVARIABLE_DOT = "pathvariable.";
  private static final String LOCALE_TOKEN = "{locale}";

  private final Project myIdeaProject;
  private FlexProjectConfigurationEditor myFlexConfigEditor;
  private final Collection<FlashBuilderProject> myAllFBProjects;
  private final FlashBuilderSdkFinder mySdkFinder;
  private final Set<String> myPathVariables;
  private boolean myPathVariablesInitialized = false;

  public FlashBuilderModuleImporter(final Project ideaProject,
                                    final FlexProjectConfigurationEditor flexConfigEditor,
                                    final Collection<FlashBuilderProject> allFBProjects,
                                    final FlashBuilderSdkFinder sdkFinder) {
    myIdeaProject = ideaProject;
    myFlexConfigEditor = flexConfigEditor;
    myAllFBProjects = allFBProjects;
    mySdkFinder = sdkFinder;

    myPathVariables = new THashSet<String>();
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

    final ModifiableFlexIdeBuildConfiguration[] configurations = myFlexConfigEditor.getConfigurations(rootModel.getModule());
    assert configurations.length == 1;
    final ModifiableFlexIdeBuildConfiguration mainBC = configurations[0];

    final String bcName = suggestMainBCName(fbProject);
    mainBC.setName(bcName);

    final TargetPlatform targetPlatform = fbProject.getTargetPlatform();
    mainBC.setTargetPlatform(targetPlatform);
    mainBC.setPureAs(fbProject.isPureActionScript());
    mainBC.setOutputType(fbProject.getOutputType());

    if (fbProject.getOutputType() == OutputType.Application) {
      mainBC.setMainClass(fbProject.getMainAppClassName());

      final String shortClassName = StringUtil.getShortName(fbProject.getMainAppClassName());
      mainBC.setOutputFileName(shortClassName + ".swf");

      if (targetPlatform == TargetPlatform.Web && fbProject.isUseHtmlWrapper()) {
        mainBC.setUseHtmlWrapper(true);
        mainBC.setWrapperTemplatePath(fbProject.getProjectRootPath() + "/" + CreateHtmlWrapperTemplateDialog.HTML_TEMPLATE_FOLDER_NAME);
      }

      if (targetPlatform == TargetPlatform.Desktop) {
        setupAirDescriptor(mainBC, rootModel);
        mainBC.getAirDesktopPackagingOptions().setPackageFileName(shortClassName);
        if (!StringUtil.isEmpty(fbProject.getDesktopCertPath())) {
          mainBC.getAirDesktopPackagingOptions().getSigningOptions().setUseTempCertificate(false);
          mainBC.getAirDesktopPackagingOptions().getSigningOptions().setKeystorePath(fbProject.getDesktopCertPath());
        }
      }

      if (targetPlatform == TargetPlatform.Mobile) {
        setupAirDescriptor(mainBC, rootModel);

        mainBC.getAndroidPackagingOptions().setEnabled(fbProject.isAndroidSupported());
        mainBC.getAndroidPackagingOptions().setPackageFileName(shortClassName);
        if (!StringUtil.isEmpty(fbProject.getAndroidCertPath())) {
          mainBC.getAndroidPackagingOptions().getSigningOptions().setUseTempCertificate(false);
          mainBC.getAndroidPackagingOptions().getSigningOptions().setKeystorePath(fbProject.getAndroidCertPath());
        }

        mainBC.getIosPackagingOptions().setEnabled(fbProject.isIosSupported());
        mainBC.getIosPackagingOptions().setPackageFileName(shortClassName);
        mainBC.getIosPackagingOptions().getSigningOptions()
          .setProvisioningProfilePath(StringUtil.notNullize(fbProject.getIOSProvisioningPath()));
        mainBC.getIosPackagingOptions().getSigningOptions().setKeystorePath(StringUtil.notNullize(fbProject.getIOSCertPath()));
      }
    }
    else {
      mainBC.setOutputFileName(fbProject.getName() + ".swc");
    }

    mainBC.setOutputFolder(getAbsolutePathWithLinksHandled(fbProject, fbProject.getOutputFolderPath()));

    if (BCUtils.canHaveRLMsAndRuntimeStylesheets(mainBC) && !fbProject.getCssFilesToCompile().isEmpty()) {
      final Collection<String> cssPaths = new ArrayList<String>();
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

    if (sdk != null) {
      mainBC.getDependencies().setSdkEntry(Factory.createSdkEntry(sdk.getName()));

      final String sdkHome = sdk.getHomePath();
      if (targetPlatform == TargetPlatform.Web && sdkHome != null) {
        mainBC.getDependencies().setTargetPlayer(TargetPlayerUtils.getTargetPlayer(fbProject.getTargetPlayerVersion(), sdkHome));
      }
    }

    // todo dependencies.setComponentSet();
    // todo dependencies.setFrameworkLinkage();

    setupDependencies(mainBC, fbProject);

    final Map<String, String> compilerOptions = new THashMap<String, String>();

    // todo parse options, replace "-a b" to "-a=b", move some to dedicated fields
    final String fbOptions = fbProject.getAdditionalCompilerOptions();
    final List<String> locales = FlexUtils.getOptionValues(fbOptions, "locale", "compiler.locale");
    final String ideaOptions = FlexUtils.removeOptions(fbOptions, "locale", "compiler.locale", "source-path", "compiler.source-path");
    mainBC.getCompilerOptions().setAdditionalOptions(ideaOptions);

    final StringBuilder localesBuf = new StringBuilder();
    for (String locale : locales) {
      if (localesBuf.length() > 0) {
        localesBuf.append(CompilerOptionInfo.LIST_ENTRIES_SEPARATOR);
      }
      localesBuf.append(locale);
    }
    compilerOptions.put("compiler.locale", localesBuf.toString());

    if (mainBC.getOutputType() == OutputType.Application) {
      FlexModuleBuilder.createRunConfiguration(rootModel.getModule(), mainBC.getName());
    }

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

    mainBC.getCompilerOptions().setAllOptions(compilerOptions);

    setupOtherAppsAndModules(rootModel, mainBC, fbProject);
  }

  private static void setupAirDescriptor(final ModifiableFlexIdeBuildConfiguration bc, final ModuleRootModel rootModel) {
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
                                        final ModifiableFlexIdeBuildConfiguration mainBC,
                                        final FlashBuilderProject fbProject) {
    final Collection<ModifiableFlexIdeBuildConfiguration> allApps = new ArrayList<ModifiableFlexIdeBuildConfiguration>();
    allApps.add(mainBC);

    for (String mainClass : fbProject.getApplicationClassNames()) {
      final ModifiableFlexIdeBuildConfiguration bc = myFlexConfigEditor.copyConfiguration(mainBC, mainBC.getNature());
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

      FlexModuleBuilder.createRunConfiguration(rootModel.getModule(), bc.getName());

      allApps.add(bc);
    }

    if (BCUtils.canHaveRLMsAndRuntimeStylesheets(mainBC)) {
      setupModules(rootModel, allApps, fbProject);
    }
  }

  private void setupModules(final ModuleRootModel rootModel,
                            final Collection<ModifiableFlexIdeBuildConfiguration> apps,
                            final FlashBuilderProject fbProject) {
    for (final FlashBuilderProject.FBRLMInfo rlm : fbProject.getModules()) {
      ModifiableFlexIdeBuildConfiguration hostApp = apps.iterator().next();
      if (rlm.OPTIMIZE) {
        final String hostAppMainClass = getMainClassFqn(fbProject, rlm.OPTIMIZE_FOR, rootModel.getSourceRootUrls());
        for (ModifiableFlexIdeBuildConfiguration appBC : apps) {
          if (hostAppMainClass.equals(appBC.getMainClass())) {
            hostApp = appBC;
            break;
          }
        }
      }

      final Collection<FlexIdeBuildConfiguration.RLMInfo> rlms = new ArrayList<FlexIdeBuildConfiguration.RLMInfo>(hostApp.getRLMs());
      final String rlmMainClass = getMainClassFqn(fbProject, rlm.MAIN_CLASS_PATH, rootModel.getSourceRootUrls());
      rlms.add(new FlexIdeBuildConfiguration.RLMInfo(rlmMainClass, rlm.OUTPUT_PATH, rlm.OPTIMIZE));
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
    final Collection<ContentEntry> otherContentEntries = new ArrayList<ContentEntry>();

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
      final List<String> locales = FlexUtils.getOptionValues(fbProject.getAdditionalCompilerOptions(), "locale", "compiler.locale");
      final List<String> moreSourcePaths =
        FlexUtils.getOptionValues(fbProject.getAdditionalCompilerOptions(), "source-path", "compiler.source-path");

      for (final String rawSourcePath : sourcePaths) {
        if (rawSourcePath.contains(LOCALE_TOKEN)) {
          for (String locale : locales) {
            handleRawSourcePath(rootModel, fbProject, mainContentEntryUrl, mainContentEntry, otherContentEntries,
                                rawSourcePath.replace(LOCALE_TOKEN, locale));
          }
        }
        else {
          handleRawSourcePath(rootModel, fbProject, mainContentEntryUrl, mainContentEntry, otherContentEntries, rawSourcePath);
        }
      }

      for (String sourcePath : moreSourcePaths) {
        if (sourcePath.contains(LOCALE_TOKEN)) {
          for (String locale : locales) {
            final String path = getPathToSourceRootSetInAdditionalOptions(sourcePath.replace(LOCALE_TOKEN, locale),
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
      mainContentEntry.addSourceFolder(sourceUrl, false);
    }
    else {
      for (final ContentEntry otherContentEntry : otherContentEntries) {
        if (FileUtil.isAncestor(new File(VfsUtilCore.urlToPath(mainContentEntryUrl)), new File(VfsUtilCore.urlToPath(sourceUrl)), false)) {
          otherContentEntry.addSourceFolder(sourceUrl, false);
          return;
        }
      }

      final ContentEntry newContentEntry = rootModel.addContentEntry(sourceUrl);
      newContentEntry.addSourceFolder(sourceUrl, false);
      otherContentEntries.add(newContentEntry);
    }
  }

  private void setupDependencies(final ModifiableFlexIdeBuildConfiguration bc, final FlashBuilderProject fbProject) {
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

      final LibraryTableBase.ModifiableModelEx libraryModel = myFlexConfigEditor.getLibraryModel(bc.getDependencies());

      final Library library = libraryModel.createLibrary(null, FlexLibraryType.FLEX_LIBRARY);

      final LibraryEx.ModifiableModelEx libraryModifiableModel = ((LibraryEx.ModifiableModelEx)library.getModifiableModel());
      final String libraryId = UUID.randomUUID().toString();
      libraryModifiableModel.setProperties(new FlexLibraryProperties(libraryId));

      final String libraryPath = getAbsolutePathWithLinksHandled(fbProject, libraryPathOrig);

      if (libraryPath.toLowerCase().endsWith(".swc") || libraryPath.toLowerCase().endsWith(".ane")) {
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

      ApplicationManager.getApplication().runWriteAction(new Runnable() {
        public void run() {
          libraryModifiableModel.commit();
        }
      });

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
