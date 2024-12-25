// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.sdk;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class DartSdk {
  public static final String DART_SDK_LIB_NAME = "Dart SDK";
  private static final String UNKNOWN_VERSION = "unknown";

  private final @NotNull String myHomePath;
  private final @NotNull String myVersion;

  private DartSdk(final @NotNull String homePath, final @NotNull String version) {
    myHomePath = homePath;
    myVersion = version;
  }

  public @NotNull String getHomePath() {
    return myHomePath;
  }

  /**
   * @return presentable version with revision, like "1.9.1_r44672" or "1.9.0-dev.10.9_r44532" or "1.10.0-edge.44829"
   */
  public @NotNull String getVersion() {
    return myVersion;
  }

  public static @Nullable DartSdk getDartSdk(final @NotNull Project project) {
    return CachedValuesManager.getManager(project).getCachedValue(project, () -> {
      final DartSdk sdk = findDartSdkAmongLibraries(LibraryTablesRegistrar.getInstance().getLibraryTable(project).getLibraries());
      if (sdk == null) {
        return new CachedValueProvider.Result<>(null, ProjectRootManager.getInstance(project));
      }

      List<Object> dependencies = new ArrayList<>(3);
      dependencies.add(ProjectRootManager.getInstance(project));
      ContainerUtil.addIfNotNull(dependencies, LocalFileSystem.getInstance().findFileByPath(sdk.getHomePath() + "/version"));
      ContainerUtil.addIfNotNull(dependencies, LocalFileSystem.getInstance().findFileByPath(sdk.getHomePath() + "/lib/core/core.dart"));

      return new CachedValueProvider.Result<>(sdk, ArrayUtil.toObjectArray(dependencies));
    });
  }

  private static @Nullable DartSdk findDartSdkAmongLibraries(final Library[] libs) {
    for (final Library library : libs) {
      if (DART_SDK_LIB_NAME.equals(library.getName())) {
        return getSdkByLibrary(library);
      }
    }

    return null;
  }

  public static @Nullable DartSdk getSdkByLibrary(final @NotNull Library library) {
    final VirtualFile[] roots = library.getFiles(OrderRootType.CLASSES);
    final VirtualFile dartCoreRoot = DartSdkLibraryPresentationProvider.findDartCoreRoot(Arrays.asList(roots));
    if (dartCoreRoot != null) {
      final String homePath = dartCoreRoot.getParent().getParent().getPath();
      final String version = StringUtil.notNullize(DartSdkUtil.getSdkVersion(homePath), UNKNOWN_VERSION);
      return new DartSdk(homePath, version);
    }

    return null;
  }
}
