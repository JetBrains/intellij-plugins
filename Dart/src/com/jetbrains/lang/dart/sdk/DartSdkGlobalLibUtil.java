package com.jetbrains.lang.dart.sdk;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.ex.ProjectRootManagerEx;
import com.intellij.openapi.roots.impl.libraries.ApplicationLibraryTable;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.util.EmptyRunnable;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import com.intellij.util.PlatformUtils;
import com.intellij.util.SmartList;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class DartSdkGlobalLibUtil {
  private static final Logger LOG = Logger.getInstance(DartSdkGlobalLibUtil.class.getName());

  public static boolean isIdeWithMultipleModuleSupport() {
    return PlatformUtils.isIntelliJ();
  }

  public static String createDartSdkGlobalLib(final @NotNull Project project, final @NotNull String sdkHomePath) {
    final LibraryTable.ModifiableModel model = ApplicationLibraryTable.getApplicationTable().getModifiableModel();
    final String darSdkGlobalLibName = createDartSdkGlobalLib(model, sdkHomePath);
    model.commit();
    ProjectRootManagerEx.getInstanceEx(project).makeRootsChange(EmptyRunnable.getInstance(), false, true);
    return darSdkGlobalLibName;
  }

  public static String createDartSdkGlobalLib(final @NotNull LibraryTable.ModifiableModel libraryTableModel,
                                              final @NotNull String sdkHomePath) {
    // similar to LibraryUtil.createLibrary()
    String name = DartSdk.DART_SDK_GLOBAL_LIB_NAME;
    int count = 2;
    while (libraryTableModel.getLibraryByName(name) != null) {
      name = DartSdk.DART_SDK_GLOBAL_LIB_NAME + " (" + count++ + ")";
    }

    final Library library = libraryTableModel.createLibrary(name);

    setupDartSdkRoots(library, sdkHomePath);
    return library.getName();
  }

  public static void updateDartSdkGlobalLib(final @NotNull Project project,
                                            final @NotNull String dartSdkGlobalLibName,
                                            final @NotNull String sdkHomePath) {
    final LibraryTable.ModifiableModel model = ApplicationLibraryTable.getApplicationTable().getModifiableModel();
    updateDartSdkGlobalLib(model, dartSdkGlobalLibName, sdkHomePath);
    model.commit();
    ProjectRootManagerEx.getInstanceEx(project).makeRootsChange(EmptyRunnable.getInstance(), false, true);
  }

  public static void updateDartSdkGlobalLib(final @NotNull LibraryTable.ModifiableModel libraryTableModifiableModel,
                                            final @NotNull String dartSdkGlobalLibName,
                                            final @NotNull String sdkHomePath) {
    final Library library = libraryTableModifiableModel.getLibraryByName(dartSdkGlobalLibName);
    LOG.assertTrue(library != null, dartSdkGlobalLibName);
    setupDartSdkRoots(library, sdkHomePath);
  }

  private static void setupDartSdkRoots(final @NotNull Library library, final @NotNull String sdkHomePath) {
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
        libModifiableModel.dispose();
      }
    }
  }

  static void updateDependencyOnDartSdkGlobalLib(final @NotNull Project project,
                                                 final @NotNull Module[] modulesWithDart,
                                                 final @NotNull String dartSdkGlobalLibName) {
    for (final Module module : ModuleManager.getInstance(project).getModules()) {
      final boolean mustHaveDart = ArrayUtil.contains(module, modulesWithDart);
      boolean hasDart = false;

      final ModifiableRootModel modifiableModel = ModuleRootManager.getInstance(module).getModifiableModel();
      try {
        for (final OrderEntry orderEntry : modifiableModel.getOrderEntries()) {
          if (isOrderEntryPointingToThisGlobalLib(orderEntry, dartSdkGlobalLibName)) {
            hasDart = true;

            if (!mustHaveDart) {
              modifiableModel.removeOrderEntry(orderEntry);
            }
          }
        }

        if (mustHaveDart && !hasDart) {
          modifiableModel.addInvalidLibrary(dartSdkGlobalLibName, LibraryTablesRegistrar.APPLICATION_LEVEL);
        }

        if (modifiableModel.isChanged()) {
          modifiableModel.commit();
        }
        else {
          modifiableModel.dispose();
        }
      }
      catch (Exception e) {
        LOG.warn(e);
        if (!modifiableModel.isDisposed()) modifiableModel.dispose();
      }
    }
  }

  static void detachDartSdkGlobalLib(final @NotNull Collection<Module> modules,
                                     final @NotNull String dartSdkGlobalLibName) {
    for (final Module module : modules) {
      final ModifiableRootModel modifiableModel = ModuleRootManager.getInstance(module).getModifiableModel();
      try {
        for (final OrderEntry orderEntry : modifiableModel.getOrderEntries()) {
          if (isOrderEntryPointingToThisGlobalLib(orderEntry, dartSdkGlobalLibName)) {
            modifiableModel.removeOrderEntry(orderEntry);
          }
        }
        modifiableModel.commit();
      }
      catch (Exception e) {
        LOG.warn(e);
        if (!modifiableModel.isDisposed()) modifiableModel.dispose();
      }
    }
  }

  public static Collection<Module> getModulesWithDartSdkGlobalLibAttached(final @NotNull Project project,
                                                                          final @NotNull String dartSdkGlobalLibName) {
    final Collection<Module> result = new SmartList<Module>();

    for (final Module module : ModuleManager.getInstance(project).getModules()) {
      for (final OrderEntry orderEntry : ModuleRootManager.getInstance(module).getOrderEntries()) {
        if (isOrderEntryPointingToThisGlobalLib(orderEntry, dartSdkGlobalLibName)) {
          result.add(module);
          break;
        }
      }
    }

    return result;
  }

  public static boolean isDartSdkGlobalLibAttached(final @NotNull Module module, final @NotNull String dartSdkGlobalLibName) {
    for (final OrderEntry orderEntry : ModuleRootManager.getInstance(module).getOrderEntries()) {
      if (isOrderEntryPointingToThisGlobalLib(orderEntry, dartSdkGlobalLibName)) {
        return true;
      }
    }

    return false;
  }

  private static boolean isOrderEntryPointingToThisGlobalLib(final @NotNull OrderEntry orderEntry, final @NotNull String globalLibName) {
    return orderEntry instanceof LibraryOrderEntry &&
           ((LibraryOrderEntry)orderEntry).getLibraryLevel() == LibraryTablesRegistrar.APPLICATION_LEVEL &&
           globalLibName.equals(((LibraryOrderEntry)orderEntry).getLibraryName());
  }

  public static void configureDependencyOnGlobalLib(final @NotNull Module module, final @NotNull String globalLibName) {
    if (isDartSdkGlobalLibAttached(module, globalLibName)) return;

    final ModifiableRootModel modifiableModel = ModuleRootManager.getInstance(module).getModifiableModel();
    try {
      modifiableModel.addInvalidLibrary(globalLibName, LibraryTablesRegistrar.APPLICATION_LEVEL);
      modifiableModel.commit();
    }
    catch (Exception e) {
      LOG.warn(e);
      if (!modifiableModel.isDisposed()) modifiableModel.dispose();
    }
  }
}
