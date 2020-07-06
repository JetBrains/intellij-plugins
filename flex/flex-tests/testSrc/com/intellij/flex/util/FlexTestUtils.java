// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.util;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.flex.FlexCommonUtils;
import com.intellij.flex.model.bc.BuildConfigurationNature;
import com.intellij.flex.model.bc.LinkageType;
import com.intellij.flex.model.bc.OutputType;
import com.intellij.flex.model.bc.TargetPlatform;
import com.intellij.javascript.flex.css.FlexStylesIndexableSetContributor;
import com.intellij.javascript.flex.mxml.schema.FlexSchemaHandler;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitRunConfiguration;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitRunConfigurationType;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitRunnerParameters;
import com.intellij.lang.javascript.flex.library.FlexLibraryProperties;
import com.intellij.lang.javascript.flex.library.FlexLibraryType;
import com.intellij.lang.javascript.flex.projectStructure.FlexBCConfigurator;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.Factory;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexLibraryIdGenerator;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexProjectConfigurationEditor;
import com.intellij.lang.javascript.flex.projectStructure.options.FlexProjectRootsUtil;
import com.intellij.lang.javascript.flex.run.FlashRunConfiguration;
import com.intellij.lang.javascript.flex.run.FlashRunConfigurationType;
import com.intellij.lang.javascript.flex.run.FlashRunnerParameters;
import com.intellij.lang.javascript.flex.sdk.FlexSdkType2;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.*;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.util.Consumer;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.FactoryMap;
import junit.framework.Assert;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.intellij.openapi.vfs.VfsUtilCore.convertFromUrl;
import static com.intellij.openapi.vfs.VfsUtilCore.urlToPath;

public class FlexTestUtils {

  @NotNull
  public static String getTestDataPath(@NotNull final String relativePath) {
    final File dir = new File("testData");
    if (dir.isDirectory()) {
      final String testDataPath = FileUtil.toSystemIndependentName(dir.getAbsolutePath());
      if (testDataPath.endsWith("/flex/flex-tests/testData")) {
        // started from 'flex-plugin' project
        return testDataPath + "/" + relativePath;
      }
    }

    return FileUtil.toSystemIndependentName(PathManager.getHomePath() + "/contrib/flex/flex-tests/testData/" + relativePath);
  }

  @NotNull
  public static String getPathToCompleteFlexSdk(final String version) {
    final File dir = new File("../flex-tests/testData/flex-sdk");
    if (dir.isDirectory()) {
      // started from 'flex-plugin' project
      final String path = FileUtil.toCanonicalPath(dir.getAbsolutePath());
      return path + "/" + version;
    }


    return PathManager.getHomePath() + "/contrib/flex/flex-tests/testData/flex-sdk/" + version;
  }

  public static void setupFlexLib(final Project project, final Class<?> clazz, final String testName) {
    if (JSTestUtils.testMethodHasOption(clazz, testName, JSTestOption.WithFlexLib)) {
      Module[] modules = ModuleManager.getInstance(project).getModules();

      for (Module module : modules) {
        addFlexLibrary(false, module, "Flex Lib", true, getTestDataPath("flexlib"), "flexlib.swc", null, null);
      }
    }
  }

  public static String getPathToMockFlex(@NotNull Class<?> clazz, @NotNull String testName) {
    if (JSTestUtils.testMethodHasOption(JSTestUtils.getTestMethod(clazz, testName), JSTestOption.WithGumboSdk)) {
      return getTestDataPath("MockFlexSdk4");
    }
    return getTestDataPath("MockFlexSdk3");
  }

  public static String getPathToMockFlex(JSTestUtils.TestDescriptor testDescriptor) {
    return getPathToMockFlex(testDescriptor.first, testDescriptor.second);
  }

  public static void setupFlexSdk(@NotNull final Module module,
                                  @NotNull String testName,
                                  @NotNull Class<?> clazz,
                                  String pathToFlexSdk,
                                  boolean air, @NotNull Disposable parent) {
    boolean withFlexSdk = JSTestUtils
      .testMethodHasOption(JSTestUtils.getTestMethod(clazz, testName), JSTestOption.WithFlexSdk, JSTestOption.WithGumboSdk,
                           JSTestOption.WithFlexFacet);
    if (withFlexSdk) {
      doSetupFlexSdk(module, pathToFlexSdk, air, getSdkVersion(testName, clazz), parent);
    }
  }

  public static Sdk getSdk(JSTestUtils.TestDescriptor testDescriptor, @NotNull Disposable parent) {
    return createSdk(getPathToMockFlex(testDescriptor), getSdkVersion(testDescriptor), parent);
  }

  private static String getSdkVersion(JSTestUtils.TestDescriptor testDescriptor) {
    return getSdkVersion(testDescriptor.second, testDescriptor.first);
  }

  private static String getSdkVersion(String testName, Class<?> clazz) {
    return JSTestUtils.testMethodHasOption(JSTestUtils.getTestMethod(clazz, testName), JSTestOption.WithGumboSdk) ? "4.0.0" : "3.4.0";
  }

  public static void setupFlexSdk(@NotNull final Module module, @NotNull String testName, @NotNull Class<?> clazz, @NotNull Disposable parent) {
    setupFlexSdk(module, testName, clazz, getPathToMockFlex(clazz, testName), false, parent);
  }

  public static void addASDocToSdk(final Module module, final Class<?> clazz, final String testName) {
    WriteAction.run(() -> {
      final Sdk flexSdk = FlexUtils.getSdkForActiveBC(module);
      final SdkModificator sdkModificator = flexSdk.getSdkModificator();
      VirtualFile docRoot = LocalFileSystem.getInstance().findFileByPath(getPathToMockFlex(clazz, testName) + "/asdoc");
      sdkModificator.addRoot(docRoot, JavadocOrderRootType.getInstance());
      sdkModificator.commitChanges();
    });
  }

  private static void doSetupFlexSdk(@NotNull Module module,
                                     final String flexSdkRootPath,
                                     final boolean air,
                                     final String sdkVersion, @NotNull Disposable parent) {
    WriteAction.run(() -> {
      final Sdk sdk = createSdk(flexSdkRootPath, sdkVersion, parent);

      if (ModuleType.get(module) == FlexModuleType.getInstance()) {
        modifyBuildConfiguration(module, bc -> {
          bc.setNature(new BuildConfigurationNature(air ? TargetPlatform.Desktop : TargetPlatform.Web, false, OutputType.Application));
          bc.getDependencies().setSdkEntry(Factory.createSdkEntry(sdk.getName()));
        });
      }

    });
  }

  public static Sdk createSdk(final String flexSdkRootPath, @Nullable String sdkVersion, @NotNull Disposable parent) {
    return createSdk(flexSdkRootPath, sdkVersion, true, parent);
  }

  public static Sdk createSdk(final String flexSdkRootPath,
                              @Nullable String sdkVersion,
                              final boolean removeExisting,
                              @NotNull Disposable parent) {
    Sdk sdk = WriteCommandAction.runWriteCommandAction(null, (Computable<Sdk>)() -> {
      final ProjectJdkTable projectJdkTable = ProjectJdkTable.getInstance();
      if (removeExisting) {
        final List<Sdk> existingFlexSdks = projectJdkTable.getSdksOfType(FlexSdkType2.getInstance());
        for (Sdk existingFlexSdk : existingFlexSdks) {
          projectJdkTable.removeJdk(existingFlexSdk);
        }
      }

      VfsRootAccess.allowRootAccess(parent, flexSdkRootPath);
      final FlexSdkType2 sdkType = FlexSdkType2.getInstance();
      final Sdk sdk1 = new ProjectJdkImpl(sdkType.suggestSdkName(null, flexSdkRootPath), sdkType, flexSdkRootPath, "");
      sdkType.setupSdkPaths(sdk1);
      projectJdkTable.addJdk(sdk1, parent);
      return sdk1;
    });

    final SdkModificator modificator = sdk.getSdkModificator();
    if (sdkVersion != null) {
      modificator.setVersionString(sdkVersion);
    }
    if (sdk.getHomeDirectory() == null) {
      throw new IllegalArgumentException("Could not find a Flex SDK at " + flexSdkRootPath);
    }
    modificator.addRoot(sdk.getHomeDirectory(), OrderRootType.CLASSES);
    modificator.commitChanges();
    return sdk;
  }

  public static void setSdk(final ModifiableFlexBuildConfiguration bc, final Sdk sdk) {
    bc.getDependencies().setSdkEntry(Factory.createSdkEntry(sdk.getName()));
    bc.getDependencies().setTargetPlayer(FlexCommonUtils.getMaximumTargetPlayer(sdk.getHomePath()));
  }

  public static Module createModule(@NotNull Project project, String moduleName, VirtualFile moduleContent) throws IOException {
    return WriteAction.compute(() -> {
      ModifiableModuleModel m1 = ModuleManager.getInstance(project).getModifiableModel();
      VirtualFile moduleDir = PlatformTestUtil.getOrCreateProjectBaseDir(project).createChildDirectory(JSTestUtils.class, moduleName);
      Module result = m1.newModule(moduleDir.toNioPath().resolve(moduleName + ".iml"), FlexModuleType.getInstance().getId());
      m1.commit();

      if (moduleContent != null) {
        VfsUtil.copyDirectory(JSTestUtils.class, moduleContent, moduleDir, null);
        PsiTestUtil.addSourceRoot(result, moduleDir);
      }
      return result;
    });
  }

  public static void modifyBuildConfiguration(final Module module, final Consumer<? super ModifiableFlexBuildConfiguration> modifier) {
    modifyConfigs(module.getProject(), editor -> modifier.consume(editor.getConfigurations(module)[0]));
  }

  public static void modifyConfigs(Project project, final Consumer<? super FlexProjectConfigurationEditor> modifier) {
    Module[] modules = ModuleManager.getInstance(project).getModules();
    final FlexProjectConfigurationEditor editor = createConfigEditor(modules);
    try {
      modifier.consume(editor);
      editor.commit();
    }
    catch (ConfigurationException ex) {
      throw new RuntimeException(ex);
    }
    finally {
      Disposer.dispose(editor);
    }
  }

  public static FlexProjectConfigurationEditor createConfigEditor(final Module... modules) {
    final Map<Module, ModifiableRootModel> models =
      FactoryMap.create(module -> {
        final ModifiableRootModel result1 = ModuleRootManager.getInstance(module).getModifiableModel();
        Disposer.register(module, new Disposable() {
          @Override
          public void dispose() {
            if (!result1.isDisposed()) {
              result1.dispose();
            }
          }
        });
        return result1;
      });

    return new FlexProjectConfigurationEditor(modules[0].getProject(), new FlexProjectConfigurationEditor.ProjectModifiableModelProvider() {
      @Override
      public Module[] getModules() {
        return modules;
      }

      @Override
      public ModifiableRootModel getModuleModifiableModel(Module module) {
        return models.get(module);
      }

      @Override
      public void addListener(FlexBCConfigurator.Listener listener, Disposable parentDisposable) {
        // ignore
      }

      @Override
      public void commitModifiableModels() {
        ApplicationManager.getApplication().runWriteAction(() -> {
          for (ModifiableRootModel model : models.values()) {
            if (model.isChanged()) {
              model.commit();
            }
          }
        });
      }

      @Override
      public Library findSourceLibraryForLiveName(final String name, @NotNull final String level) {
        return findSourceLibrary(name, level);
      }

      @Override
      public Library findSourceLibrary(final String name, @NotNull final String level) {
        return getLibrariesTable(level).getLibraryByName(name);
      }

      private LibraryTable getLibrariesTable(final String level) {
        if (LibraryTablesRegistrar.APPLICATION_LEVEL.equals(level)) {
          return LibraryTablesRegistrar.getInstance().getLibraryTable();
        }
        else {
          assert LibraryTablesRegistrar.PROJECT_LEVEL.equals(level);
          return LibraryTablesRegistrar.getInstance().getLibraryTable(modules[0].getProject());
        }
      }
    });
  }

  public static void addFlexLibrary(final boolean isProjectLibrary,
                                    final Module module,
                                    final String libraryName,
                                    final boolean overwrite,
                                    String libraryRoot,
                                    @Nullable String classesPath,
                                    @Nullable String sourcesPath,
                                    @Nullable String asdocPath,
                                    final LinkageType linkageType,
                                    @Nullable VirtualFile copyTo) {
    if (copyTo != null) {
      if (classesPath != null) {
        classesPath = copyTo(copyTo, libraryRoot + classesPath).getName();
      }
      if (sourcesPath != null) {
        sourcesPath = copyTo(copyTo, libraryRoot + sourcesPath).getName();
      }
      if (asdocPath != null) {
        asdocPath = copyTo(copyTo, libraryRoot + asdocPath).getName();
      }
      libraryRoot = copyTo.getPath();
    }


    doAddFlexLibrary(isProjectLibrary, module, libraryName, overwrite, libraryRoot, classesPath, sourcesPath, asdocPath, linkageType);
  }

  private static void doAddFlexLibrary(boolean isProjectLibrary,
                                       Module module,
                                       String libraryName,
                                       boolean overwrite,
                                       String libraryRoot,
                                       @Nullable String classesPath,
                                       @Nullable String sourcesPath,
                                       @Nullable String asdocPath,
                                       LinkageType linkageType) {
    ModifiableRootModel moduleModifiableModel = ModuleRootManager.getInstance(module).getModifiableModel();
    WriteAction.run(() -> {
      try {
        // first let's create Flex library
        final LibraryTable libraryTable;
        if (isProjectLibrary) {
          libraryTable = com.intellij.openapi.roots.libraries.LibraryTablesRegistrar.getInstance().getLibraryTable(module.getProject());
        }
        else {
          libraryTable = moduleModifiableModel.getModuleLibraryTable();
        }

        Library library = libraryTable.getLibraryByName(libraryName);
        if (library != null && overwrite) {
          libraryTable.removeLibrary(library);
          library = null;
        }

        if (library == null) {
          LibraryTable.ModifiableModel libraryTableModifiableModel = libraryTable.getModifiableModel();
          library = libraryTableModifiableModel.createLibrary(libraryName, FlexLibraryType.FLEX_LIBRARY);

          LibraryEx.ModifiableModelEx libraryModel = (LibraryEx.ModifiableModelEx)library.getModifiableModel();
          libraryModel.setProperties(new FlexLibraryProperties(FlexLibraryIdGenerator.generateId()));
          addRootIfNotNull(libraryRoot, classesPath, libraryModel, OrderRootType.CLASSES, ".swc", ".zip");
          addRootIfNotNull(libraryRoot, sourcesPath, libraryModel, OrderRootType.SOURCES, ".zip");
          addRootIfNotNull(libraryRoot, asdocPath, libraryModel, JavadocOrderRootType.getInstance(), ".zip");
          libraryModel.commit();
          libraryTableModifiableModel.commit();
        }

        moduleModifiableModel.commit();

        // then add Flex library to build configuration dependency

        final String committedLibraryId;
        if (isProjectLibrary) {
          committedLibraryId =
            FlexProjectRootsUtil.getLibraryId(com.intellij.openapi.roots.libraries.LibraryTablesRegistrar.getInstance().getLibraryTable(module.getProject()).getLibraryByName(libraryName));
        }
        else {
          final OrderEntry
            entry = ContainerUtil.find(ModuleRootManager.getInstance(module).getOrderEntries(),
                                       orderEntry -> orderEntry instanceof LibraryOrderEntry &&
                                                     ((LibraryOrderEntry)orderEntry).getLibraryName().equals(libraryName));
          committedLibraryId = FlexProjectRootsUtil.getLibraryId(((LibraryOrderEntry)entry).getLibrary());
        }

        if (ModuleType.get(module) == FlexModuleType.getInstance()) {
          modifyConfigs(module.getProject(), e -> {
            final ModifiableFlexBuildConfiguration[] bcs = e.getConfigurations(module);
            final ModifiableDependencyEntry dependencyEntry;
            if (isProjectLibrary) {
              dependencyEntry = e.createSharedLibraryEntry(bcs[0].getDependencies(), libraryName, LibraryTablesRegistrar.PROJECT_LEVEL);
            }
            else {
              dependencyEntry = e.createModuleLibraryEntry(bcs[0].getDependencies(), committedLibraryId);
            }
            dependencyEntry.getDependencyType().setLinkageType(linkageType);
            bcs[0].getDependencies().getModifiableEntries().add(dependencyEntry);
          });
        }
      }
      finally {
        if (!moduleModifiableModel.isDisposed()) {
          moduleModifiableModel.dispose();
        }
      }
    });
  }

  private static VirtualFile copyTo(VirtualFile to, final String path) {
    return WriteAction.compute(() -> {
      try {
        VirtualFile f = LocalFileSystem.getInstance().findFileByPath(path);
        if (f.isDirectory()) {
          VirtualFile result = to.createChildDirectory(JSTestUtils.class, f.getName());
          VfsUtil.copyDirectory(JSTestUtils.class, f, result, null);
          return result;
        }
        else {
          return VfsUtilCore.copyFile(JSTestUtils.class, f, to);
        }
      }
      catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }

  private static void addRootIfNotNull(@NotNull final String rootPath,
                                       @Nullable String relativePath,
                                       final Library.ModifiableModel libraryModel,
                                       final OrderRootType orderRootType, final String... archiveSuffices) {
    if (relativePath == null) {
      return;
    }

    if (!rootPath.endsWith("/") && !relativePath.startsWith("/")) {
      relativePath = "/" + relativePath;
    }

    VirtualFile root = LocalFileSystem.getInstance().findFileByPath(rootPath + relativePath);
    assert root != null : "path '" + rootPath + relativePath + "' not found";

    boolean archive = false;
    for (String suffix : archiveSuffices) {
      if (relativePath.endsWith(suffix)) {
        archive = true;
        break;
      }
    }
    if (archive) {
      root = JarFileSystem.getInstance().getJarRootForLocalFile(root);
      assert root != null;
    }
    libraryModel.addRoot(root, orderRootType);
  }

  public static void addFlexLibrary(final boolean isProjectLibrary,
                                    final Module module,
                                    final String libraryName,
                                    final boolean overwrite,
                                    final String libraryRoot,
                                    @Nullable final String classesPath,
                                    @Nullable final String sourcesPath,
                                    @Nullable final String asdocPath) {
    addFlexLibrary(isProjectLibrary, module, libraryName, overwrite, libraryRoot, classesPath, sourcesPath, asdocPath,
                   DependencyType.DEFAULT_LINKAGE, null);
  }

  public static void addFlexLibrary(final boolean isProjectLibrary,
                                    final Module module,
                                    final String libraryName,
                                    final boolean overwrite,
                                    String libraryRoot,
                                    @Nullable String classesPath,
                                    @Nullable String sourcesPath,
                                    @Nullable String asdocPath,
                                    final LinkageType linkageType) {
    addFlexLibrary(isProjectLibrary, module, libraryName, overwrite, libraryRoot, classesPath, sourcesPath, asdocPath, linkageType, null);
  }

  public static SdkModificator getFlexSdkModificator(final Module module) {
    return FlexUtils.getSdkForActiveBC(module).getSdkModificator();
  }

  public static void addFlexModuleDependency(final Module dependent, final Module dependency) {
    WriteCommandAction.writeCommandAction(null).run(() -> modifyConfigs(dependency.getProject(), editor -> {
      final ModifiableFlexBuildConfiguration dependentBc = editor.getConfigurations(dependent)[0];
      final ModifiableFlexBuildConfiguration dependencyBc = editor.getConfigurations(dependency)[0];
      dependencyBc.setOutputType(OutputType.Library);
      final ModifiableBuildConfigurationEntry dependencyEntry =
        editor.createBcEntry(dependentBc.getDependencies(), dependencyBc, null);
      dependentBc.getDependencies().getModifiableEntries().add(dependencyEntry);
    }));
  }

  public static void checkFlashRunConfig(final RunManager runManager,
                                         final Module module,
                                         final String configName,
                                         final String className) {
    final List<RunnerAndConfigurationSettings> settings = runManager.getConfigurationSettingsList(FlashRunConfigurationType.getInstance());
    RunnerAndConfigurationSettings settingsToCheck = null;
    for (RunnerAndConfigurationSettings setting : settings) {
      if (configName.equals(setting.getName())) {
        settingsToCheck = setting;
        break;
      }
    }

    Assert.assertNotNull("Run configuration not found: " + configName, settingsToCheck);
    final FlashRunnerParameters params = ((FlashRunConfiguration)settingsToCheck.getConfiguration()).getRunnerParameters();
    Assert.assertEquals(className, params.getOverriddenMainClass());
    Assert.assertEquals(FlexBuildConfigurationManager.getInstance(module).getActiveConfiguration().getName(), params.getBCName());
  }

  public static void checkFlexUnitRunConfig(final RunManager runManager,
                                            final Module module, final String configName,
                                            final String packageName,
                                            final String className,
                                            final String methodName) {
    final List<RunnerAndConfigurationSettings> settings = runManager.getConfigurationSettingsList(FlexUnitRunConfigurationType.class);
    RunnerAndConfigurationSettings settingsToCheck = null;
    for (RunnerAndConfigurationSettings setting : settings) {
      if (configName.equals(setting.getName())) {
        settingsToCheck = setting;
        break;
      }
    }

    Assert.assertNotNull("Run configuration not found: " + configName, settingsToCheck);
    final FlexUnitRunnerParameters params = ((FlexUnitRunConfiguration)settingsToCheck.getConfiguration()).getRunnerParameters();
    Assert.assertEquals(packageName, params.getPackageName());
    Assert.assertEquals(className, params.getClassName());
    Assert.assertEquals(methodName, params.getMethodName());
    Assert.assertEquals(FlexBuildConfigurationManager.getInstance(module).getActiveConfiguration().getName(), params.getBCName());
  }

  public static void createFlashRunConfig(final RunManager runManager,
                                          final Module module, final String configName, final String className, boolean generatedName) {
    final RunnerAndConfigurationSettings settings = runManager.createConfiguration(configName, FlashRunConfigurationType.class);
    runManager.addConfiguration(settings);

    final FlashRunnerParameters params = ((FlashRunConfiguration)settings.getConfiguration()).getRunnerParameters();
    params.setModuleName(module.getName());
    params.setBCName(FlexBuildConfigurationManager.getInstance(module).getActiveConfiguration().getName());
    params.setOverrideMainClass(true);
    params.setOverriddenMainClass(className);

    if (generatedName) {
      ((FlashRunConfiguration)settings.getConfiguration()).setGeneratedName();
    }
  }

  public static void createFlexUnitRunConfig(final RunManager runManager,
                                             final String configName,
                                             final Module module,
                                             final FlexUnitRunnerParameters.Scope scope,
                                             final String packageName,
                                             final String className,
                                             final String methodName,
                                             boolean generatedName) {
    final RunnerAndConfigurationSettings settings = runManager.createConfiguration(configName, FlexUnitRunConfigurationType.class);
    runManager.addConfiguration(settings);

    final FlexUnitRunnerParameters params = ((FlexUnitRunConfiguration)settings.getConfiguration()).getRunnerParameters();
    params.setModuleName(module.getName());
    params.setBCName(FlexBuildConfigurationManager.getInstance(module).getActiveConfiguration().getName());
    params.setScope(scope);
    params.setPackageName(packageName);
    params.setClassName(className);
    params.setMethodName(methodName);

    if (generatedName) {
      ((FlexUnitRunConfiguration)settings.getConfiguration()).setGeneratedName();
    }
  }

  public static void setupCustomSdk(final Module module,
                                    final VirtualFile swc,
                                    @Nullable final VirtualFile srcRoot,
                                    @Nullable final VirtualFile asdocRoot) {
    WriteAction.run(() -> {
      final SdkModificator modificator = getFlexSdkModificator(module);
      modificator.removeAllRoots();
      modificator.addRoot(swc, OrderRootType.CLASSES);
      if (srcRoot != null) {
        modificator.addRoot(srcRoot, OrderRootType.SOURCES);
      }
      if (asdocRoot != null) {
        modificator.addRoot(asdocRoot, JavadocOrderRootType.getInstance());
      }
      modificator.commitChanges();
    });
  }

  public static void addFlexUnitLib(Class<?> clazz, String method, Module module,
                                    String libRootPath, String flexUnit1Swc, String flexUnit4Swc) {
    if (JSTestUtils.testMethodHasOption(clazz, method, JSTestOption.WithFlexUnit1)) {
      addLibrary(module, "FlexUnit1", libRootPath, flexUnit1Swc, null, null);
    }
    if (JSTestUtils.testMethodHasOption(clazz, method, JSTestOption.WithFlexUnit4)) {
      addLibrary(module, "FlexUnit4", libRootPath, flexUnit4Swc, null, null);
    }
  }

  public static void addLibrary(final Module module,
                                @NotNull final String libraryName,
                                final String path,
                                String swcFileName,
                                @Nullable final String sourcesZipFileName,
                                @Nullable final String asdocRoot) {
    addFlexLibrary(false, module, libraryName, true, path, swcFileName, sourcesZipFileName, asdocRoot);
  }

  public static void allowFlexVfsRootsFor(@NotNull Disposable disposable, @NotNull String relativeTestPath) {
    VfsRootAccess.allowRootAccess(disposable, getTestDataPath(relativeTestPath),
                                  urlToPath(convertFromUrl(FlexSchemaHandler.class.getResource("z.xsd"))),
                                  urlToPath(convertFromUrl(FlexStylesIndexableSetContributor.class.getResource("FlexStyles.as"))));
  }

  public static void removeLibrary(Module module, String libraryName) {
    ModifiableRootModel moduleModifiableModel = ModuleRootManager.getInstance(module).getModifiableModel();
    WriteAction.run(() -> {
      LibraryTable table = moduleModifiableModel.getModuleLibraryTable();
      table.removeLibrary(table.getLibraryByName(libraryName));
      moduleModifiableModel.commit();
    });
  }
}
