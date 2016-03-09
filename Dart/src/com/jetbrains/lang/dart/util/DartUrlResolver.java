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

  @Nullable
  public abstract VirtualFile getPackageRoot();

  /**
   * Process 'Path Packages' (https://www.dartlang.org/tools/pub/dependencies.html#path-packages) and this package itself (symlink to local 'lib' folder)
   */
  public abstract void processLivePackages(final @NotNull PairConsumer<String, VirtualFile> packageNameAndDirConsumer);

  public abstract Collection<String> getLivePackageNames();

  @Nullable
  public abstract VirtualFile getPackageDirIfNotInOldStylePackagesFolder(@NotNull final String packageName,
                                                                         @Nullable final String pathAfterPackageName);

  /**
   * Dart url has <code>dart:</code>, <code>package:</code> or <code>file:</code> scheme
   */
  @Nullable
  public abstract VirtualFile findFileByDartUrl(@NotNull String url);

  @Nullable
  public static VirtualFile findFileInDartSdkLibFolder(final @NotNull Project project,
                                                       final @Nullable DartSdk dartSdk,
                                                       final @Nullable String dartUri) {
    if (dartSdk == null || dartUri == null || !dartUri.startsWith(DART_PREFIX)) return null;

    final int firstSlashIndex = dartUri.indexOf('/');

    if (firstSlashIndex < 0) {
      // This is a main library file from SDK. For example "dart:html" URI maps to SDK/lib/html/dartium/html_dartium.dart file (according to info from SDK/lib/_internal/libraries.dart file)
      return DartLibraryIndex.getSdkLibByUri(project, dartUri);
    }

    // URI contains slash that means that this is a 'part'-file, for example "dart:_internal/symbol.dart".
    // First search for main library file ("dart:_internal" maps to SDK/lib/internal/internal.dart) and then look for its part near it.
    final String mainLibUri = dartUri.substring(0, firstSlashIndex);
    final VirtualFile mainLibFile = DartLibraryIndex.getSdkLibByUri(project, mainLibUri);
    if (mainLibFile != null) {
      final String partRelPath = dartUri.substring(firstSlashIndex + 1);
      final VirtualFile partFile = mainLibFile.getParent().findFileByRelativePath(partRelPath);
      if (partFile != null) {
        return partFile;
      }
    }

    // Finally look for file in SDK by its relative path
    final String sdkLibRelPath = dartUri.substring(DART_PREFIX.length());
    final String path = dartSdk.getHomePath() + "/lib/" + sdkLibRelPath;
    return LocalFileSystem.getInstance().findFileByPath(path);
  }

  @NotNull
  public abstract String getDartUrlForFile(final @NotNull VirtualFile file);

  public boolean mayNeedDynamicUpdate() {
    return true;
  }
}
