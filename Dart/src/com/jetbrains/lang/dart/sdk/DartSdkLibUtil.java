package com.jetbrains.lang.dart.sdk;

import com.google.common.annotations.VisibleForTesting;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.impl.libraries.ProjectLibraryTable;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.util.ArrayUtil;
import com.intellij.util.PlatformUtils;
import com.intellij.util.SmartList;
import com.jetbrains.lang.dart.DartProjectComponent;
import com.jetbrains.lang.dart.ide.index.DartLibraryIndex;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

public class DartSdkLibUtil {
  private static final Logger LOG = Logger.getInstance(DartSdkLibUtil.class.getName());

  private static final String[] SDK_LIB_SUBFOLDERS_BLACKLIST = {
    "analysis_server",
    "analyzer",
    "dev_compiler",
    "front_end",
    "internal",
    "profiler",
  };

  public static boolean isIdeWithMultipleModuleSupport() {
    return PlatformUtils.isIntelliJ();
  }

  public static void ensureDartSdkConfigured(@NotNull final Project project, @NotNull final String sdkHomePath) {
    final LibraryTable libraryTable = ProjectLibraryTable.getInstance(project);
    final Library library = libraryTable.getLibraryByName(DartSdk.DART_SDK_LIB_NAME);
    if (library == null) {
      final LibraryTable.ModifiableModel model = libraryTable.getModifiableModel();
      createDartSdkLib(project, model, sdkHomePath);
      model.commit();
    }
    else {
      final DartSdk sdk = DartSdk.getSdkByLibrary(library);
      if (sdk == null || !sdkHomePath.equals(sdk.getHomePath())) {
        setupDartSdkRoots(project, library, sdkHomePath);
      }
    }
  }

  public static void ensureDartSdkConfigured(@NotNull final Project project,
                                             @NotNull final LibraryTable.ModifiableModel libraryTableModel,
                                             @NotNull final String sdkHomePath) {
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

  private static void createDartSdkLib(@NotNull final Project project,
                                       @NotNull final LibraryTable.ModifiableModel libraryTableModel,
                                       @NotNull final String sdkHomePath) {
    final Library existingLib = libraryTableModel.getLibraryByName(DartSdk.DART_SDK_LIB_NAME);
    if (existingLib != null) {
      setupDartSdkRoots(project, existingLib, sdkHomePath);
    }
    else {
      final Library library = libraryTableModel.createLibrary(DartSdk.DART_SDK_LIB_NAME);
      setupDartSdkRoots(project, library, sdkHomePath);
    }
  }

  private static void setupDartSdkRoots(@NotNull final Project project, @NotNull final Library library, @NotNull final String sdkHomePath) {
    LocalFileSystem.getInstance().refreshAndFindFileByPath(sdkHomePath + "/lib");

    final SortedSet<String> roots = getRootUrls(project, sdkHomePath);
    if (roots.isEmpty()) return; // corrupted SDK

    if (Comparing.haveEqualElements(ArrayUtil.toStringArray(roots), library.getRootProvider().getUrls(OrderRootType.CLASSES))) {
      return; // already ok
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
    }
    catch (Exception e) {
      LOG.warn(e);
      Disposer.dispose(libModifiableModel);
    }
  }

  @NotNull
  private static SortedSet<String> getRootUrls(@NotNull final Project project, @NotNull final String sdkHomePath) {
    final SortedSet<String> result = getRootUrlsFromLibrariesFile(project, sdkHomePath);

    if (result.isEmpty() || !result.contains(VfsUtilCore.pathToUrl(sdkHomePath + "/lib/core"))) {
      LOG.info("Failed to get useful info from " + sdkHomePath + "/lib/_internal/libraries.dart");
      return getRootUrlsFailover(sdkHomePath);
    }

    return result;
  }

  @NotNull
  @VisibleForTesting
  public static SortedSet<String> getRootUrlsFromLibrariesFile(@NotNull Project project, @NotNull String sdkHomePath) {
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

  @NotNull
  @VisibleForTesting
  public static SortedSet<String> getRootUrlsFailover(@NotNull final String sdkHomePath) {
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

  public static boolean isDartSdkEnabled(@NotNull final Module module) {
    for (final OrderEntry orderEntry : ModuleRootManager.getInstance(module).getOrderEntries()) {
      if (isDartSdkOrderEntry(orderEntry)) {
        return true;
      }
    }

    return false;
  }

  public static void enableDartSdk(@NotNull final Module module) {
    if (isDartSdkEnabled(module)) return;

    final ModifiableRootModel modifiableModel = ModuleRootManager.getInstance(module).getModifiableModel();
    try {
      modifiableModel.addInvalidLibrary(DartSdk.DART_SDK_LIB_NAME, LibraryTablesRegistrar.PROJECT_LEVEL);
      modifiableModel.commit();
    }
    catch (Exception e) {
      LOG.warn(e);
      if (!modifiableModel.isDisposed()) modifiableModel.dispose();
    }
  }

  public static void disableDartSdk(@NotNull final Collection<Module> modules) {
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

    DartProjectComponent.commitModifiableModels(modules.iterator().next().getProject(), models);
  }

  public static Collection<Module> getModulesWithDartSdkEnabled(@NotNull final Project project) {
    final Collection<Module> result = new SmartList<>();

    for (final Module module : ModuleManager.getInstance(project).getModules()) {
      if (isDartSdkEnabled(module)) {
        result.add(module);
      }
    }

    return result;
  }

  public static void enableDartSdkForSpecifiedModulesAndDisableForOthers(@NotNull final Project project,
                                                                         @NotNull final Module[] modulesWithDart) {
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

    DartProjectComponent.commitModifiableModels(project, modelsToCommit);
  }

  public static boolean isDartSdkOrderEntry(@NotNull final OrderEntry orderEntry) {
    return orderEntry instanceof LibraryOrderEntry &&
           LibraryTablesRegistrar.PROJECT_LEVEL.equals(((LibraryOrderEntry)orderEntry).getLibraryLevel()) &&
           DartSdk.DART_SDK_LIB_NAME.equals(((LibraryOrderEntry)orderEntry).getLibraryName());
  }
}
