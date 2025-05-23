// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.sdk;

import com.google.common.annotations.VisibleForTesting;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.impl.ModifiableModelCommitter;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.platform.workspace.jps.entities.*;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.PlatformUtils;
import com.intellij.util.SmartList;
import com.jetbrains.lang.dart.ide.index.DartLibraryIndex;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

public final class DartSdkLibUtil {
  private static final Logger LOG = Logger.getInstance(DartSdkLibUtil.class.getName());

  private static final String[] SDK_LIB_SUBFOLDERS_BLACKLIST = {
    "analysis_server",
    "analyzer",
    "dev_compiler",
    "front_end",
    "internal",
    "kernel",
    "profiler",
    "web_sql",
  };

  public static boolean isIdeWithMultipleModuleSupport() {
    return PlatformUtils.isIntelliJ() || "AndroidStudio".equals(PlatformUtils.getPlatformPrefix());
  }

  public static void ensureDartSdkConfigured(final @NotNull Project project, final @NotNull String sdkHomePath) {
    configureDartSdkAndReturnUndoingDisposable(project, sdkHomePath);
  }

  /**
   * @return Disposable which undoes configuration
   */
  @VisibleForTesting
  public static @NotNull Disposable configureDartSdkAndReturnUndoingDisposable(final @NotNull Project project, final @NotNull String sdkHomePath) {
    final LibraryTable libraryTable = LibraryTablesRegistrar.getInstance().getLibraryTable(project);
    final Library library = libraryTable.getLibraryByName(DartSdk.DART_SDK_LIB_NAME);
    if (library == null) {
      final LibraryTable.ModifiableModel model = libraryTable.getModifiableModel();
      Library lib = createDartSdkLib(project, model, sdkHomePath);
      model.commit();
      return () -> {
        if (((LibraryEx)lib).isDisposed()) return;

        WriteCommandAction.runWriteCommandAction(null, () -> {
          LibraryTable.ModifiableModel m = libraryTable.getModifiableModel();
          m.removeLibrary(lib);
          m.commit();
        });
      };
    }
    final DartSdk sdk = DartSdk.getSdkByLibrary(library);
    if (sdk == null || !sdkHomePath.equals(sdk.getHomePath())) {
      return setupDartSdkRoots(project, library, sdkHomePath);
    }
    return ()->{};
  }

  public static void ensureDartSdkConfigured(final @NotNull Project project,
                                             final @NotNull LibraryTable.ModifiableModel libraryTableModel,
                                             final @NotNull String sdkHomePath) {
    final Library library = libraryTableModel.getLibraryByName(DartSdk.DART_SDK_LIB_NAME);
    if (library == null) {
      createDartSdkLib(project, libraryTableModel, sdkHomePath);
    }
    else {
      final DartSdk sdk = DartSdk.getSdkByLibrary(library);
      if (sdk == null || !sdkHomePath.equals(sdk.getHomePath())) {
        setupDartSdkRoots(project, library, sdkHomePath);
      }
    }
  }

  private static @NotNull Library createDartSdkLib(final @NotNull Project project,
                                                   final @NotNull LibraryTable.ModifiableModel libraryTableModel,
                                                   final @NotNull String sdkHomePath) {
    final Library existingLib = libraryTableModel.getLibraryByName(DartSdk.DART_SDK_LIB_NAME);
    if (existingLib != null) {
      setupDartSdkRoots(project, existingLib, sdkHomePath);
      return existingLib;
    }
    else {
      final Library library = libraryTableModel.createLibrary(DartSdk.DART_SDK_LIB_NAME);
      setupDartSdkRoots(project, library, sdkHomePath);
      return library;
    }
  }

  /**
   * @return Disposable which undoes configuration
   */
  private static @NotNull Disposable setupDartSdkRoots(final @NotNull Project project, final @NotNull Library library, final @NotNull String sdkHomePath) {
    LocalFileSystem.getInstance().refreshAndFindFileByPath(sdkHomePath + "/lib");

    final SortedSet<String> roots = getRootUrls(project, sdkHomePath);
    if (roots.isEmpty()) return ()->{}; // corrupted SDK

    if (Comparing.haveEqualElements(ArrayUtilRt.toStringArray(roots), library.getRootProvider().getUrls(OrderRootType.CLASSES))) {
      return ()->{}; // already ok
    }

    final LibraryEx.ModifiableModelEx libModifiableModel = (LibraryEx.ModifiableModelEx)library.getModifiableModel();
    try {
      // remove old
      for (String url : libModifiableModel.getUrls(OrderRootType.CLASSES)) {
        libModifiableModel.removeRoot(url, OrderRootType.CLASSES);
      }
      for (String url : libModifiableModel.getExcludedRootUrls()) {
        libModifiableModel.removeExcludedRoot(url);
      }

      // add new
      for (String root : roots) {
        libModifiableModel.addRoot(root, OrderRootType.CLASSES);
      }

      libModifiableModel.commit();
      return ()-> WriteCommandAction.runWriteCommandAction(null, ()->{
        Library.ModifiableModel m = library.getModifiableModel();
        for (String root : roots) {
          m.removeRoot(root, OrderRootType.CLASSES);
        }
        m.commit();
      });
    }
    catch (Exception e) {
      LOG.warn(e);
      Disposer.dispose(libModifiableModel);
    }
    return ()->{};
  }

  private static @NotNull SortedSet<String> getRootUrls(final @NotNull Project project, final @NotNull String sdkHomePath) {
    final SortedSet<String> result = getRootUrlsFromLibrariesFile(project, sdkHomePath);

    if (result.isEmpty() || !result.contains(VfsUtilCore.pathToUrl(sdkHomePath + "/lib/core"))) {
      LOG.info("Failed to get useful info from " + sdkHomePath + "/lib/_internal/libraries.dart");
      return getRootUrlsFailover(sdkHomePath);
    }

    return result;
  }

  @VisibleForTesting
  public static @NotNull SortedSet<String> getRootUrlsFromLibrariesFile(@NotNull Project project, @NotNull String sdkHomePath) {
    final Map<String, String> map = DartLibraryIndex.getSdkLibUriToRelativePathMap(project, sdkHomePath);
    final SortedSet<String> result = new TreeSet<>();

    for (Map.Entry<String, String> entry : map.entrySet()) {
      if (entry.getKey().startsWith("dart:_")) continue; // private libs

      final String relPath = entry.getValue();
      final int slashIndex = relPath.indexOf("/");
      if (slashIndex <= 0) {
        LOG.info("Skipping unexpected Dart library path: " + relPath);
        continue;
      }

      result.add(VfsUtilCore.pathToUrl(sdkHomePath + "/lib/" + relPath.substring(0, slashIndex)));
    }
    return result;
  }

  @VisibleForTesting
  public static @NotNull SortedSet<String> getRootUrlsFailover(final @NotNull String sdkHomePath) {
    final SortedSet<String> result = new TreeSet<>();

    final File lib = new File(sdkHomePath + "/lib");
    if (!lib.isDirectory()) return result;

    final File[] children = lib.listFiles();
    if (children == null) return result;

    for (File subDir : children) {
      final String subDirName = subDir.getName();
      if (!subDir.isDirectory()) continue;
      if (subDirName.startsWith("_")) continue;
      if (ArrayUtil.contains(subDirName, SDK_LIB_SUBFOLDERS_BLACKLIST)) continue;

      result.add(VfsUtilCore.pathToUrl(sdkHomePath + "/lib/" + subDirName));
    }

    return result;
  }

  public static boolean isDartSdkEnabled(final @NotNull Module module) {
    for (final OrderEntry orderEntry : ModuleRootManager.getInstance(module).getOrderEntries()) {
      if (isDartSdkOrderEntry(orderEntry)) {
        return true;
      }
    }

    return false;
  }

  public static boolean isDartSdkEnabled(@NotNull ModuleEntity moduleEntity) {
    for (ModuleDependencyItem dependencyItem : moduleEntity.getDependencies()) {
      if (isDartSdkOrderEntry(dependencyItem)) {
        return true;
      }
    }
    return false;
  }

  public static void enableDartSdk(final @NotNull Module module) {
    enableDartSdkAndReturnUndoingDisposable(module);
  }

  /**
   * @return Disposable which undoes configuration
   */
  @VisibleForTesting
  public static @NotNull Disposable enableDartSdkAndReturnUndoingDisposable(final @NotNull Module module) {
    if (isDartSdkEnabled(module)) return Disposer.newDisposable();

    final ModifiableRootModel modifiableModel = ModuleRootManager.getInstance(module).getModifiableModel();
    try {
      modifiableModel.addInvalidLibrary(DartSdk.DART_SDK_LIB_NAME, LibraryTablesRegistrar.PROJECT_LEVEL);
      modifiableModel.commit();
      return () -> {
        if (module.isDisposed()) return;

        WriteCommandAction.runWriteCommandAction(null, () -> {
          ModifiableRootModel m = ModuleRootManager.getInstance(module).getModifiableModel();
          for (OrderEntry orderEntry : m.getOrderEntries()) {
            if (isDartSdkOrderEntry(orderEntry)) {
              m.removeOrderEntry(orderEntry);
            }
          }
          m.commit();
        });
      };
    }
    catch (Exception e) {
      LOG.error(e);
      if (!modifiableModel.isDisposed()) modifiableModel.dispose();
    }
    return Disposer.newDisposable();
  }

  public static void disableDartSdk(final @NotNull Collection<? extends Module> modules) {
    if (modules.isEmpty()) return;

    final List<ModifiableRootModel> models = new SmartList<>();

    for (final Module module : modules) {
      final ModifiableRootModel modifiableModel = ModuleRootManager.getInstance(module).getModifiableModel();
      for (final OrderEntry orderEntry : modifiableModel.getOrderEntries()) {
        if (isDartSdkOrderEntry(orderEntry)) {
          modifiableModel.removeOrderEntry(orderEntry);
        }
      }
      models.add(modifiableModel);
    }

    commitModifiableModels(modules.iterator().next().getProject(), models);
  }

  public static Collection<Module> getModulesWithDartSdkEnabled(final @NotNull Project project) {
    final Collection<Module> result = new SmartList<>();

    for (final Module module : ModuleManager.getInstance(project).getModules()) {
      if (isDartSdkEnabled(module)) {
        result.add(module);
      }
    }

    return result;
  }

  public static void enableDartSdkForSpecifiedModulesAndDisableForOthers(final @NotNull Project project,
                                                                         final Module @NotNull [] modulesWithDart) {
    final List<ModifiableRootModel> modelsToCommit = new SmartList<>();

    for (final Module module : ModuleManager.getInstance(project).getModules()) {
      final boolean mustHaveDart = ArrayUtil.contains(module, modulesWithDart);
      boolean hasDart = false;

      final ModifiableRootModel modifiableModel = ModuleRootManager.getInstance(module).getModifiableModel();
      for (final OrderEntry orderEntry : modifiableModel.getOrderEntries()) {
        if (isDartSdkOrderEntry(orderEntry)) {
          hasDart = true;

          if (!mustHaveDart) {
            modifiableModel.removeOrderEntry(orderEntry);
          }
        }
      }

      if (mustHaveDart && !hasDart) {
        modifiableModel.addInvalidLibrary(DartSdk.DART_SDK_LIB_NAME, LibraryTablesRegistrar.PROJECT_LEVEL);
      }

      if (modifiableModel.isChanged()) {
        modelsToCommit.add(modifiableModel);
      }
      else {
        modifiableModel.dispose();
      }
    }

    commitModifiableModels(project, modelsToCommit);
  }

  public static boolean isDartSdkOrderEntry(final @NotNull OrderEntry orderEntry) {
    return orderEntry instanceof LibraryOrderEntry &&
           LibraryTablesRegistrar.PROJECT_LEVEL.equals(((LibraryOrderEntry)orderEntry).getLibraryLevel()) &&
           DartSdk.DART_SDK_LIB_NAME.equals(((LibraryOrderEntry)orderEntry).getLibraryName());
  }

  public static boolean isDartSdkOrderEntry(@NotNull ModuleDependencyItem dependencyItem) {
    if (dependencyItem instanceof LibraryDependency) {
      LibraryId libraryId = ((LibraryDependency)dependencyItem).getLibrary();
      return libraryId.getTableId() instanceof LibraryTableId.ProjectLibraryTableId &&
             libraryId.getName().equals(DartSdk.DART_SDK_LIB_NAME);
    }
    return false;
  }

  private static void commitModifiableModels(final @NotNull Project project,
                                             final @NotNull Collection<ModifiableRootModel> modelsToCommit) {
    if (!modelsToCommit.isEmpty()) {
      try {
        ModifiableModelCommitter.multiCommit(modelsToCommit, ModuleManager.getInstance(project).getModifiableModel());
      }
      finally {
        for (ModifiableRootModel model : modelsToCommit) {
          if (!model.isDisposed()) {
            model.dispose();
          }
        }
      }
    }
  }
}
