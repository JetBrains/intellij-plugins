package com.intellij.lang.javascript.flex.flashbuilder;

import com.intellij.lang.javascript.flex.FlexModuleBuilder;
import com.intellij.lang.javascript.flex.TargetPlayerUtils;
import com.intellij.lang.javascript.flex.library.FlexLibraryProperties;
import com.intellij.lang.javascript.flex.library.FlexLibraryType;
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
import com.intellij.util.PathUtil;
import gnu.trove.THashMap;
import gnu.trove.THashSet;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FlashBuilderModuleImporter {

  private static final String CORE_RESOURCES_PREFS_REL_PATH =
    "/.metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.core.resources.prefs";
  private static final String PATHVARIABLE_DOT = "pathvariable.";

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
      }

      if (targetPlatform == TargetPlatform.Mobile) {
        setupAirDescriptor(mainBC, rootModel);

        mainBC.getAndroidPackagingOptions().setEnabled(fbProject.isAndroidSupported());
        mainBC.getAndroidPackagingOptions().setPackageFileName(shortClassName);

        mainBC.getIosPackagingOptions().setEnabled(fbProject.isIosSupported());
        mainBC.getIosPackagingOptions().setPackageFileName(shortClassName);
      }
    }
    else {
      mainBC.setOutputFileName(fbProject.getName() + ".swc");
    }

    mainBC.setOutputFolder(getAbsolutePathWithLinksHandled(fbProject, fbProject.getOutputFolderPath()));

    if (BCUtils.canHaveRuntimeStylesheets(mainBC) && !fbProject.getCssFilesToCompile().isEmpty()) {
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
    // todo parse options, replace "-a b" to "-a=b", move some to dedicated fields
    mainBC.getCompilerOptions().setAdditionalOptions(fbProject.getAdditionalCompilerOptions());

    if (mainBC.getOutputType() == OutputType.Application) {
      FlexModuleBuilder.createRunConfiguration(rootModel.getModule(), mainBC.getName());
    }

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
    }

    for (final Pair<String, String> sourcePathAndDestPath : fbProject.getModules()) {
      final ModifiableFlexIdeBuildConfiguration bc = myFlexConfigEditor.copyConfiguration(mainBC, mainBC.getNature());
      bc.setCssFilesToCompile(Collections.<String>emptyList());
      final String mainClass = getModuleClassName(fbProject, sourcePathAndDestPath.first, rootModel.getSourceRootUrls());
      final String shortName = StringUtil.getShortName(mainClass);
      bc.setName(shortName);
      bc.setOutputType(OutputType.RuntimeLoadedModule);
      //bc.setOptimizeFor(); todo
      bc.setMainClass(mainClass);
      bc.setOutputFileName(PathUtil.getFileName(sourcePathAndDestPath.second));
      final String parentPath = PathUtil.getParentPath(sourcePathAndDestPath.second);
      bc.setOutputFolder(bc.getOutputFolder() + (parentPath.isEmpty() ? "" : ("/" + parentPath)));
    }
  }

  private static String suggestMainBCName(final FlashBuilderProject fbProject) {
    return fbProject.getOutputType() == OutputType.Application && !fbProject.getMainAppClassName().isEmpty()
           ? StringUtil.getShortName(fbProject.getMainAppClassName())
           : fbProject.getName();
  }

  private String getModuleClassName(final FlashBuilderProject flashBuilderProject,
                                    final String moduleSourcePath,
                                    final String[] sourceRootUrls) {
    final String mainClassPathUrl = VfsUtil.pathToUrl(getAbsolutePathWithLinksHandled(flashBuilderProject, moduleSourcePath));
    for (final String sourceRootUrl : sourceRootUrls) {
      if (mainClassPathUrl.startsWith(sourceRootUrl + "/")) {
        return FlashBuilderProjectLoadUtil.getClassName(mainClassPathUrl.substring(sourceRootUrl.length() + 1));
      }
    }

    return FlashBuilderProjectLoadUtil.getClassName(mainClassPathUrl.substring(mainClassPathUrl.lastIndexOf('/') + 1));
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

  private void setupDependencies(final ModifiableFlexIdeBuildConfiguration bc, final FlashBuilderProject fbProject) {
    OUTER:
    for (final String libraryPathOrig : fbProject.getLibraryPaths()) {
      for (FlashBuilderProject otherProject : myAllFBProjects) {
        if (otherProject != fbProject && libraryPathOrig.startsWith("/" + otherProject.getName() + "/")) {
          final ModifiableBuildConfigurationEntry bcEntry =
            myFlexConfigEditor.createBcEntry(bc.getDependencies(), otherProject.getName(), suggestMainBCName(otherProject));
          bc.getDependencies().getModifiableEntries().add(bcEntry);
          continue OUTER;
        }
      }

      final LibraryTableBase.ModifiableModelEx libraryModel = myFlexConfigEditor.getLibraryModel(bc.getDependencies());

      final Library library = libraryModel.createLibrary(null, FlexLibraryType.getInstance());

      final LibraryEx.ModifiableModelEx libraryModifiableModel = ((LibraryEx.ModifiableModelEx)library.getModifiableModel());
      final String libraryId = UUID.randomUUID().toString();
      libraryModifiableModel.setProperties(new FlexLibraryProperties(libraryId));

      final String libraryPath = getAbsolutePathWithLinksHandled(fbProject, libraryPathOrig);

      if (libraryPath.toLowerCase().endsWith(".swc")) {
        libraryModifiableModel.addRoot(VirtualFileManager.constructUrl(JarFileSystem.PROTOCOL, libraryPath) + JarFileSystem.JAR_SEPARATOR,
                                       OrderRootType.CLASSES);
      }
      else {
        libraryModifiableModel.addJarDirectory(VfsUtil.pathToUrl(libraryPath), false);
      }

      for (final String librarySourcePath : fbProject.getLibrarySourcePaths(libraryPathOrig)) {
        libraryModifiableModel.addRoot(VfsUtil.pathToUrl(getAbsolutePathWithLinksHandled(fbProject, librarySourcePath)),
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
