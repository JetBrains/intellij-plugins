package com.jetbrains.lang.dart.ide;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Url;
import com.jetbrains.javascript.debugger.FileUrlMapper;
import com.jetbrains.lang.dart.ide.settings.DartSettingsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class DartFileUrlMapper extends FileUrlMapper {
  private static final String SCHEME = "dart";

  @Nullable
  @Override
  public Url getUrl(@NotNull VirtualFile file, @NotNull Project project, @Nullable String currentAuthority) {
    return null;
  }

  @Nullable
  @Override
  public VirtualFile getFile(@NotNull Url url, @NotNull Project project) {
    if (SCHEME.equals(url.getScheme())) {
      String path = url.getPath();
      int i = path.indexOf('/');
      String libraryName;
      String libraryFilePath;
      if (i > 0) {
        libraryName = path.substring(0, i);
        libraryFilePath = path.substring(i + 1);
      }
      else {
        libraryName = path;
        libraryFilePath = null;
      }

      VirtualFile file = DartSettingsUtil.getSettings().findSdkLibrary(libraryName, project);
      if (file == null) {
        return null;
      }
      return libraryFilePath == null ? file : file.getParent().findFileByRelativePath(libraryFilePath);
    }
    return null;
  }
}