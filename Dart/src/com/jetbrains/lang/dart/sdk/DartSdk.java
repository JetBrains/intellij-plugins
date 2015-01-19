package com.jetbrains.lang.dart.sdk;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.impl.libraries.ApplicationLibraryTable;
import com.intellij.openapi.roots.libraries.Library;
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
import java.util.List;

public class DartSdk {
  static final String DART_SDK_GLOBAL_LIB_NAME = "Dart SDK";
  private static final String UNKNOWN_VERSION = "unknown";

  private final @NotNull String myHomePath;
  private final @NotNull String myVersion;
  private final @NotNull String myGlobalLibName;

  private DartSdk(@NotNull final String homePath, @NotNull final String version, final @NotNull String globalLibName) {
    myHomePath = homePath;
    myVersion = version;
    myGlobalLibName = globalLibName;
  }

  @NotNull
  public String getHomePath() {
    return myHomePath;
  }

  @NotNull
  public String getVersion() {
    return myVersion;
  }

  @NotNull
  public String getGlobalLibName() {
    return myGlobalLibName;
  }

  /**
   * Returns the same as {@link #getGlobalDartSdk()} but much faster
   */
  @Nullable
  public static DartSdk getDartSdk(@NotNull final Project project) {
    return CachedValuesManager.getManager(project).getCachedValue(project, new CachedValueProvider<DartSdk>() {
      @Nullable
      @Override
      public Result<DartSdk> compute() {
        final DartSdk sdk = getGlobalDartSdk();
        if (sdk == null) {
          return null;
        }

        List<Object> dependencies = new ArrayList<Object>(3);
        dependencies.add(ProjectRootManager.getInstance(project));
        ContainerUtil.addIfNotNull(dependencies, LocalFileSystem.getInstance().findFileByPath(sdk.getHomePath() + "/version"));
        ContainerUtil.addIfNotNull(dependencies, LocalFileSystem.getInstance().findFileByPath(sdk.getHomePath() + "/lib/core/core.dart"));

        return new Result<DartSdk>(sdk, ArrayUtil.toObjectArray(dependencies));
      }
    });
  }

  @Nullable
  public static DartSdk getGlobalDartSdk() {
    return findDartSdkAmongGlobalLibs(ApplicationLibraryTable.getApplicationTable().getLibraries());
  }

  public static DartSdk findDartSdkAmongGlobalLibs(final Library[] globalLibraries) {
    for (final Library library : globalLibraries) {
      final String libraryName = library.getName();
      if (libraryName != null && libraryName.startsWith(DART_SDK_GLOBAL_LIB_NAME)) {
        for (final VirtualFile root : library.getFiles(OrderRootType.CLASSES)) {
          if (DartSdkLibraryPresentationProvider.isDartSdkLibRoot(root)) {
            final String homePath = root.getParent().getPath();
            final String version = StringUtil.notNullize(DartSdkUtil.getSdkVersion(homePath), UNKNOWN_VERSION);
            return new DartSdk(homePath, version, libraryName);
          }
        }
      }
    }

    return null;
  }
}
