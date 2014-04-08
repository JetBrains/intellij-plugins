package com.jetbrains.lang.dart.ide;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.ProjectScope;
import com.intellij.util.Url;
import com.jetbrains.javascript.debugger.FileUrlMapper;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.ide.index.DartLibraryIndex;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import static com.jetbrains.lang.dart.util.DartResolveUtil.PACKAGE_PREFIX;
import static com.jetbrains.lang.dart.util.DartResolveUtil.PACKAGE_SCHEME;
import static com.jetbrains.lang.dart.util.PubspecYamlUtil.PUBSPEC_YAML;

final class DartFileUrlMapper extends FileUrlMapper {
  private static final String SCHEME = "dart";

  @NotNull
  @Override
  public List<Url> getUrls(@NotNull VirtualFile file, @NotNull Project project, @Nullable String currentAuthority) {
    return Collections.emptyList();
  }

  @Nullable
  @Override
  public VirtualFile getFile(@NotNull final Url url, @NotNull final Project project, @Nullable Url requestor) {
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

      VirtualFile file = DartLibraryIndex.getStandardLibraryFromSdk(project, libraryName);
      if (file == null) {
        return null;
      }
      return libraryFilePath == null ? file : file.getParent().findFileByRelativePath(libraryFilePath);
    }
    else if (PACKAGE_SCHEME.equals(url.getScheme())) {
      final String packageUrl = PACKAGE_PREFIX + url.getPath();

      if (ApplicationManager.getApplication().isDispatchThread()) {
        return DumbService.getInstance(project).isDumb() ? null : findFileInPackagesFolder(project, packageUrl);
      }

      return DumbService.getInstance(project).runReadActionInSmartMode(new Computable<VirtualFile>() {
        @Override
        public VirtualFile compute() {
          return findFileInPackagesFolder(project, packageUrl);
        }
      });
    }
    return null;
  }

  @Nullable
  private static VirtualFile findFileInPackagesFolder(final @NotNull Project project, final @NotNull String packageUrl) {
    for (final VirtualFile file : FilenameIndex.getVirtualFilesByName(project, PUBSPEC_YAML, ProjectScope.getContentScope(project))) {
      final VirtualFile result = DartResolveUtil.getPackagePrefixImportedFile(project, file, packageUrl);
      if (result != null) {
        return result;
      }
    }

    return null;
  }

  @Nullable
  @Override
  public FileType getFileType(@NotNull Url url) {
    return SCHEME.equals(url.getScheme()) || PACKAGE_SCHEME.equals(url.getScheme()) ? DartFileType.INSTANCE : null;
  }
}