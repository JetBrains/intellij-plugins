package com.jetbrains.lang.dart.sdk;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.impl.libraries.ApplicationLibraryTable;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartProjectComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DartSdk {
  public static final String DART_SDK_GLOBAL_LIB_NAME = "Dart SDK";
  private static final String UNKNOWN_VERSION = "unknown";
  private static final Key<CachedValue<DartSdk>> CACHED_DART_SDK_KEY = Key.create("CACHED_DART_SDK_KEY");

  private final @NotNull String myHomePath;
  private final @NotNull String myVersion;

  private DartSdk(@NotNull final String homePath, @NotNull final String version) {
    myHomePath = homePath;
    myVersion = version;
  }

  @NotNull
  public String getHomePath() {
    return myHomePath;
  }

  /**
   * @return presentable version with revision, like "1.9.1_r44672" or "1.9.0-dev.10.9_r44532" or "1.10.0-edge.44829"
   */
  @NotNull
  public String getVersion() {
    return myVersion;
  }

  /**
   * Returns the same as {@link #getGlobalDartSdk()} but much faster
   */
  @Nullable
  public static DartSdk getDartSdk(@NotNull final Project project) {
    CachedValue<DartSdk> cachedValue = project.getUserData(CACHED_DART_SDK_KEY);

    if (cachedValue == null) {
      cachedValue = CachedValuesManager.getManager(project).createCachedValue(() -> {
        final DartSdk sdk = getGlobalDartSdk();
        if (sdk == null) {
          return new CachedValueProvider.Result<DartSdk>(null, DartProjectComponent.getProjectRootsModificationTracker(project));
        }

        List<Object> dependencies = new ArrayList<Object>(3);
        dependencies.add(DartProjectComponent.getProjectRootsModificationTracker(project));
        ContainerUtil.addIfNotNull(dependencies, LocalFileSystem.getInstance().findFileByPath(sdk.getHomePath() + "/version"));
        ContainerUtil.addIfNotNull(dependencies, LocalFileSystem.getInstance().findFileByPath(sdk.getHomePath() + "/lib/core/core.dart"));

        return new CachedValueProvider.Result<DartSdk>(sdk, ArrayUtil.toObjectArray(dependencies));
      }, false);

      project.putUserData(CACHED_DART_SDK_KEY, cachedValue);
    }

    return cachedValue.getValue();
  }

  @Nullable
  public static DartSdk getGlobalDartSdk() {
    return findDartSdkAmongGlobalLibs(ApplicationLibraryTable.getApplicationTable().getLibraries());
  }

  @Nullable
  public static DartSdk findDartSdkAmongGlobalLibs(final Library[] globalLibraries) {
    for (final Library library : globalLibraries) {
      if (DART_SDK_GLOBAL_LIB_NAME.equals(library.getName())) {
        return getSdkByLibrary(library);
      }
    }

    return null;
  }

  @Nullable
  static DartSdk getSdkByLibrary(@NotNull final Library library) {
    final VirtualFile[] roots = library.getFiles(OrderRootType.CLASSES);
    if (roots.length == 1 && DartSdkLibraryPresentationProvider.isDartSdkLibRoot(roots[0])) {
      final String homePath = roots[0].getParent().getPath();
      final String version = StringUtil.notNullize(DartSdkUtil.getSdkVersion(homePath), UNKNOWN_VERSION);
      return new DartSdk(homePath, version);
    }

    return null;
  }
}
