package com.jetbrains.lang.dart.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PairConsumer;
import com.jetbrains.lang.dart.ide.index.DartLibraryIndex;
import com.jetbrains.lang.dart.sdk.DartSdk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public abstract class DartUrlResolver {

  public static final String DART_SCHEME = "dart";
  public static final String DART_PREFIX = "dart:";
  public static final String PACKAGE_SCHEME = "package";
  public static final String PACKAGE_PREFIX = "package:";
  public static final String FILE_SCHEME = "file";
  public static final String FILE_PREFIX = "file:";
  public static final String TEMP_SCHEME = "temp";                 // TempFileSystem in tests only
  public static final String TEMP_PREFIX = "temp:";                // TempFileSystem in tests only
  public static final String PACKAGES_FOLDER_NAME = "packages";
  public static final String DART_CORE_URI = "dart:core";

  /**
   * Returned instance becomes obsolete if/when pubspec.yaml file is added or deleted or if module-specific custom package roots are changed,
   * so do not keep returned instance too long.
   *
   * @param project
   * @param contextFile may be pubspec.yaml file, its parent folder or any file/folder within this parent folder; in case of import statements resolve this must be an analyzed file
   * @return
   */
  @NotNull
  public static DartUrlResolver getInstance(final @NotNull Project project, final @NotNull VirtualFile contextFile) {
    return new DartUrlResolverImpl(project, contextFile);
  }

  @Nullable
  public abstract VirtualFile getPubspecYamlFile();

  @NotNull
  public abstract VirtualFile[] getPackageRoots();

  /**
   * Process 'Path Packages' (https://www.dartlang.org/tools/pub/dependencies.html#path-packages) and this package itself (symlink to local 'lib' folder)
   */
  public abstract void processLivePackages(final @NotNull PairConsumer<String, VirtualFile> packageNameAndDirConsumer);

  public abstract Collection<String> getLivePackageNames();

  @Nullable
  public abstract VirtualFile getPackageDirIfLivePackageOrFromPubListPackageDirs(final @NotNull String packageName);

  @Nullable
  public abstract VirtualFile getPackageDirIfLivePackageOrFromPubListPackageDirs(final @NotNull String packageName, final @Nullable String pathAfterPackageName);

  /**
   * Dart url has <code>dart:</code>, <code>package:</code> or <code>file:</code> scheme
   */
  @Nullable
  public abstract VirtualFile findFileByDartUrl(@NotNull String url);

  @Nullable
  public static VirtualFile findFileInDartSdkLibFolder(final @NotNull Project project,
                                                       final @Nullable DartSdk dartSdk,
                                                       final @Nullable String dartUrl) {
    if (dartSdk == null || dartUrl == null || !dartUrl.startsWith(DART_PREFIX)) return null;

    final VirtualFile sdkLibByUri = DartLibraryIndex.getSdkLibByUri(project, dartUrl);

    if (sdkLibByUri != null) {
      return sdkLibByUri;
    }

    final String sdkLibRelPath = dartUrl.substring(DART_PREFIX.length());
    final String path = dartSdk.getHomePath() + "/lib/" + sdkLibRelPath;
    return LocalFileSystem.getInstance().findFileByPath(path);
  }

  @NotNull
  public abstract String getDartUrlForFile(final @NotNull VirtualFile file);
}
