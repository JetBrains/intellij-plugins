package com.jetbrains.lang.dart.sdk;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.impl.libraries.ApplicationLibraryTable;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import com.intellij.util.PlatformUtils;
import com.intellij.util.SmartList;
import com.jetbrains.lang.dart.DartProjectComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class DartSdkGlobalLibUtil {
  private static final Logger LOG = Logger.getInstance(DartSdkGlobalLibUtil.class.getName());

  public static boolean isIdeWithMultipleModuleSupport() {
    return PlatformUtils.isIntelliJ();
  }

  public static void ensureDartSdkConfigured(@NotNull final String sdkHomePath) {
    final Library library = ApplicationLibraryTable.getApplicationTable().getLibraryByName(DartSdk.DART_SDK_GLOBAL_LIB_NAME);
    if (library == null) {
      final LibraryTable.ModifiableModel model = ApplicationLibraryTable.getApplicationTable().getModifiableModel();
      createDartSdkGlobalLib(model, sdkHomePath);
      model.commit();
    }
    else {
      final DartSdk sdk = DartSdk.getSdkByLibrary(library);
      if (sdk == null || !sdkHomePath.equals(sdk.getHomePath())) {
        setupDartSdkRoots(library, sdkHomePath);
      }
    }
  }

  public static void ensureDartSdkConfigured(@NotNull final LibraryTable.ModifiableModel libraryTableModel,
                                             @NotNull final String sdkHomePath) {
    final Library library = libraryTableModel.getLibraryByName(DartSdk.DART_SDK_GLOBAL_LIB_NAME);
    if (library == null) {
      createDartSdkGlobalLib(libraryTableModel, sdkHomePath);
    }
    else {
      final DartSdk sdk = DartSdk.getSdkByLibrary(library);
      if (sdk == null || !sdkHomePath.equals(sdk.getHomePath())) {
        setupDartSdkRoots(library, sdkHomePath);
      }
    }
  }

  private static void createDartSdkGlobalLib(@NotNull final LibraryTable.ModifiableModel libraryTableModel,
                                             @NotNull final String sdkHomePath) {
    final Library existingLib = libraryTableModel.getLibraryByName(DartSdk.DART_SDK_GLOBAL_LIB_NAME);
    if (existingLib != null) {
      setupDartSdkRoots(existingLib, sdkHomePath);
    }
    else {
      final Library library = libraryTableModel.createLibrary(DartSdk.DART_SDK_GLOBAL_LIB_NAME);
      setupDartSdkRoots(library, sdkHomePath);
    }
  }

  private static void setupDartSdkRoots(@NotNull final Library library, @NotNull final String sdkHomePath) {
    final VirtualFile libRoot = LocalFileSystem.getInstance().refreshAndFindFileByPath(sdkHomePath + "/lib");
    if (libRoot != null && libRoot.isDirectory()) {
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
        libModifiableModel.addRoot(libRoot, OrderRootType.CLASSES);

        libRoot.refresh(false, true);
        for (final VirtualFile subFolder : libRoot.getChildren()) {
          if (subFolder.getName().startsWith("_")) {
            libModifiableModel.addExcludedRoot(subFolder.getUrl());
          }
        }

        libModifiableModel.commit();
      }
      catch (Exception e) {
        LOG.warn(e);
        Disposer.dispose(libModifiableModel);
      }
    }
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
      modifiableModel.addInvalidLibrary(DartSdk.DART_SDK_GLOBAL_LIB_NAME, LibraryTablesRegistrar.APPLICATION_LEVEL);
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
        modifiableModel.addInvalidLibrary(DartSdk.DART_SDK_GLOBAL_LIB_NAME, LibraryTablesRegistrar.APPLICATION_LEVEL);
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

  private static boolean isDartSdkOrderEntry(@NotNull final OrderEntry orderEntry) {
    return orderEntry instanceof LibraryOrderEntry &&
           LibraryTablesRegistrar.APPLICATION_LEVEL.equals(((LibraryOrderEntry)orderEntry).getLibraryLevel()) &&
           DartSdk.DART_SDK_GLOBAL_LIB_NAME.equals(((LibraryOrderEntry)orderEntry).getLibraryName());
  }
}
