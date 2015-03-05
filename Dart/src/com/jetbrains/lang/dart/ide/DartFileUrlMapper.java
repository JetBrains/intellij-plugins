package com.jetbrains.lang.dart.ide;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Url;
import com.jetbrains.javascript.debugger.FileUrlMapper;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.util.DartUrlResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.jetbrains.lang.dart.util.PubspecYamlUtil.PUBSPEC_YAML;

final class DartFileUrlMapper extends FileUrlMapper {
  private static final String SCHEME = "dart";

  @Nullable
  @Override
  public VirtualFile getFile(@NotNull final Url url, @NotNull final Project project, @Nullable Url requestor) {
    if (DartUrlResolver.DART_SCHEME.equals(url.getScheme())) {
      return DartUrlResolver.findFileInDartSdkLibFolder(project, DartSdk.getDartSdk(project), DartUrlResolver.DART_PREFIX + url.getPath());
    }

    if (DartUrlResolver.PACKAGE_SCHEME.equals(url.getScheme())) {
      final String packageUrl = DartUrlResolver.PACKAGE_PREFIX + url.getPath();
      final VirtualFile contextFile = findContextFile(project, requestor);

      if (contextFile != null) {
        return ApplicationManager.getApplication().runReadAction(new Computable<VirtualFile>() {
          public VirtualFile compute() {
            return DartUrlResolver.getInstance(project, contextFile).findFileByDartUrl(packageUrl);
          }
        });
      }
      else {
        if (ApplicationManager.getApplication().isDispatchThread()) {
          return DumbService.getInstance(project).isDumb() ? null : findFileInAnyPackagesFolder(project, packageUrl);
        }

        return DumbService.getInstance(project).runReadActionInSmartMode(new Computable<VirtualFile>() {
          @Override
          public VirtualFile compute() {
            return findFileInAnyPackagesFolder(project, packageUrl);
          }
        });
      }
    }

    return null;
  }

  @Nullable
  private static VirtualFile findContextFile(final @NotNull Project project, final @Nullable Url url) {
    if (url == null) return null;

    for (FileUrlMapper urlMapper : FileUrlMapper.EP_NAME.getExtensions()) {
      if (urlMapper instanceof DartFileUrlMapper) continue;
      final VirtualFile file = urlMapper.getFile(url, project, url);
      if (file != null) return file;
    }

    return null;
  }

  @Nullable
  private static VirtualFile findFileInAnyPackagesFolder(final @NotNull Project project, final @NotNull String packageUrl) {
    for (final VirtualFile yamlFile : FilenameIndex.getVirtualFilesByName(project, PUBSPEC_YAML, GlobalSearchScope.projectScope(project))) {
      final VirtualFile file = DartUrlResolver.getInstance(project, yamlFile).findFileByDartUrl(packageUrl);
      if (file != null) return file;
    }

    return null;
  }

  @Nullable
  @Override
  public FileType getFileType(@NotNull Url url) {
    return SCHEME.equals(url.getScheme()) || DartUrlResolver.PACKAGE_SCHEME.equals(url.getScheme()) ? DartFileType.INSTANCE : null;
  }
}