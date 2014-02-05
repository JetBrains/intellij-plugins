package com.jetbrains.lang.dart.sdk;

import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.impl.libraries.ApplicationLibraryTable;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

  @Nullable
  public static DartSdk getGlobalDartSdk() {
    for (final Library library : ApplicationLibraryTable.getApplicationTable().getLibraries()) {
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
