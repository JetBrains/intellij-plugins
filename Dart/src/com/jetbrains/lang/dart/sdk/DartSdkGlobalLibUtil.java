package com.jetbrains.lang.dart.sdk;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.impl.libraries.ApplicationLibraryTable;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.roots.libraries.LibraryUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

class DartSdkGlobalLibUtil {
  private static final Logger LOG = Logger.getInstance(DartSdkGlobalLibUtil.class.getName());

  static String createDartSdkGlobalLib(final String sdkHomePath) {
    final Library library = LibraryUtil.createLibrary(ApplicationLibraryTable.getApplicationTable(), DartSdk.DART_SDK_GLOBAL_LIB_NAME);
    setupDartSdkRoots(library, sdkHomePath);
    return library.getName();
  }

  static void updateDartSdkGlobalLib(final String dartSdkGlobalLibName, final String sdkHomePath) {
    final Library oldLibrary = ApplicationLibraryTable.getApplicationTable().getLibraryByName(dartSdkGlobalLibName);
    LOG.assertTrue(oldLibrary != null, dartSdkGlobalLibName);

    ApplicationLibraryTable.getApplicationTable().removeLibrary(oldLibrary);

    final Library newLibrary = ApplicationLibraryTable.getApplicationTable().createLibrary(dartSdkGlobalLibName);
    setupDartSdkRoots(newLibrary, sdkHomePath);
  }

  private static void setupDartSdkRoots(final Library library, final String sdkHomePath) {
    final VirtualFile libRoot = LocalFileSystem.getInstance().refreshAndFindFileByPath(sdkHomePath + "/lib");
    if (libRoot != null && libRoot.isDirectory()) {
      final LibraryEx.ModifiableModelEx libModifiableModel = (LibraryEx.ModifiableModelEx)library.getModifiableModel();
      try {
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
          if (isOrderEntryPointingToThisSdk(orderEntry, dartSdkGlobalLibName)) {
            hasDart = true;

            if (!mustHaveDart) {
              modifiableModel.removeOrderEntry(orderEntry);
            }
            break;
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

  static void detachDartSdkLib(final @NotNull DartSdk sdk, final @NotNull Collection<Module> modules) {
    for (final Module module : modules) {
      final ModifiableRootModel modifiableModel = ModuleRootManager.getInstance(module).getModifiableModel();
      try {
        for (final OrderEntry orderEntry : modifiableModel.getOrderEntries()) {
          if (isOrderEntryPointingToThisSdk(orderEntry, sdk.getGlobalLibName())) {
            modifiableModel.removeOrderEntry(orderEntry);
            break;
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

  static Collection<Module> getModulesWithDartSdkLibAttached(final @NotNull Project project, final @NotNull String dartSdkGlobalLibName) {
    final Collection<Module> result = new ArrayList<Module>();

    for (final Module module : ModuleManager.getInstance(project).getModules()) {
      for (final OrderEntry orderEntry : ModuleRootManager.getInstance(module).getOrderEntries()) {
        if (isOrderEntryPointingToThisSdk(orderEntry, dartSdkGlobalLibName)) {
          result.add(module);
          break;
        }
      }
    }

    return result;
  }

  private static boolean isOrderEntryPointingToThisSdk(final @NotNull OrderEntry orderEntry, final @NotNull String dartSdkGlobalLibName) {
    return orderEntry instanceof LibraryOrderEntry &&
           ((LibraryOrderEntry)orderEntry).getLibraryLevel() == LibraryTablesRegistrar.APPLICATION_LEVEL &&
           dartSdkGlobalLibName.equals(((LibraryOrderEntry)orderEntry).getLibraryName());
  }
}
