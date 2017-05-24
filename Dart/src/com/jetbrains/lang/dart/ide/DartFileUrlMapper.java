package com.jetbrains.lang.dart.ide;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.SmartList;
import com.intellij.util.Url;
import com.intellij.util.Urls;
import com.jetbrains.javascript.debugger.FileUrlMapper;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.pubServer.PubServerManager;
import com.jetbrains.lang.dart.pubServer.PubServerPathHandlerKt;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.util.DartUrlResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.builtInWebServer.WebServerPathToFileManager;
import org.jetbrains.ide.BuiltInServerManagerImpl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.jetbrains.lang.dart.util.DartUrlResolver.PACKAGE_PREFIX;
import static com.jetbrains.lang.dart.util.PubspecYamlUtil.PUBSPEC_YAML;

final class DartFileUrlMapper extends FileUrlMapper {
  private static final String SDK_URL_MARKER = "/packages/$sdk/lib/";
  private static final String PACKAGE_URL_MARKER = "/" + DartUrlResolver.PACKAGES_FOLDER_NAME + "/";

  @NotNull
  @Override
  public List<Url> getUrls(@NotNull final VirtualFile file, @NotNull final Project project, @Nullable final String currentAuthority) {
    if (currentAuthority == null || file.getFileType() != DartFileType.INSTANCE) return Collections.emptyList();

    if (Registry.is("dart.redirect.to.pub.server", true) && ProjectFileIndex.getInstance(project).isInContent(file)) {
      final Pair<VirtualFile, String> servedDirAndPath = PubServerPathHandlerKt.getServedDirAndPathForPubServer(project, file);
      if (servedDirAndPath != null) {
        final VirtualFile servedDir = servedDirAndPath.first;
        final String path = servedDirAndPath.second;
        final String pubAuthority = BuiltInServerManagerImpl.isOnBuiltInWebServerByAuthority(currentAuthority)
                                    ? PubServerManager.getInstance(project).getPubServerAuthorityForServedDir(servedDir)
                                    : currentAuthority;
        if (pubAuthority != null) {
          return Collections.singletonList(Urls.newHttpUrl(pubAuthority, path));
        }
      }
    }

    final DartUrlResolver urlResolver = DartUrlResolver.getInstance(project, file);
    final String dartUri = urlResolver.getDartUrlForFile(file);
    if (!dartUri.startsWith(PACKAGE_PREFIX)) return Collections.emptyList();

    if (BuiltInServerManagerImpl.isOnBuiltInWebServerByAuthority(currentAuthority)) {
      final List<Url> result = new SmartList<>();
      final VirtualFile pubspec = urlResolver.getPubspecYamlFile();
      final VirtualFile dartRoot = pubspec != null ? pubspec.getParent() : null;

      if (Registry.is("dart.redirect.to.pub.server", true)) {
        // package:PackageName/subdir/foo.dart -> http://localhost:45455/packages/PackageName/subdir/foo.dart
        final Collection<String> authorities =
          dartRoot != null
          ? PubServerManager.getInstance(project).getAlivePubServerAuthoritiesForDartRoot(pubspec.getParent())
          : PubServerManager.getInstance(project).getAllAlivePubServerAuthorities();
        for (String pubAuthority : authorities) {
          final String pubUrlPath = "/packages/" + dartUri.substring(PACKAGE_PREFIX.length());
          result.add(Urls.newHttpUrl(pubAuthority, pubUrlPath));
        }
      }
      else if (dartRoot != null) {
        // for built-in server:
        // package:PackageName/subdir/foo.dart -> http://localhost:63342/ProjectName/MayBeRelPathToDartProject/web/packages/PackageName/subdir/foo.dart
        final String dartRootUrlPath = WebServerPathToFileManager.getInstance(project).getPath(dartRoot);
        if (dartRootUrlPath == null) return Collections.emptyList();

        //BuiltInWebBrowserUrlProviderKt.getBuiltInServerUrls(pubspec, project, currentAuthority);
        final Url dartRootUrl = Urls.newHttpUrl(currentAuthority, "/" + project.getName() + "/" + dartRootUrlPath);

        final String urlPath =
          StringUtil.trimEnd(dartRootUrl.getPath(), "/") + "/web/packages/" + dartUri.substring(PACKAGE_PREFIX.length());

        result.add(Urls.newHttpUrl(currentAuthority, urlPath));
      }

      return result;
    }
    else {
      // for any other server (e.g. localhost:8181):
      // package:PackageName/subdir/foo.dart -> http://localhost:8181/packages/PackageName/subdir/foo.dart
      final String urlPath = "/packages/" + dartUri.substring(PACKAGE_PREFIX.length());
      return Collections.singletonList(Urls.newHttpUrl(currentAuthority, urlPath));
    }
  }

  @Nullable
  @Override
  public VirtualFile getFile(@NotNull final Url url, @NotNull final Project project, @Nullable Url requestor) {
    final String scheme = url.getScheme();
    final String path = url.getPath();

    if (DartUrlResolver.DART_SCHEME.equals(scheme)) {
      return DartUrlResolver.findFileInDartSdkLibFolder(project, DartSdk.getDartSdk(project), DartUrlResolver.DART_PREFIX + path);
    }

    if (DartUrlResolver.PACKAGE_SCHEME.equals(scheme)) {
      final String packageUri = PACKAGE_PREFIX + path;
      final VirtualFile contextFile = findContextFile(project, requestor);

      if (contextFile != null) {
        return ReadAction.compute(() -> DartUrlResolver.getInstance(project, contextFile).findFileByDartUrl(packageUri));
      }
      else {
        if (ApplicationManager.getApplication().isDispatchThread()) {
          return DumbService.getInstance(project).isDumb() ? null : findFileInAnyPackagesFolder(project, packageUri);
        }

        return DumbService.getInstance(project).runReadActionInSmartMode(() -> findFileInAnyPackagesFolder(project, packageUri));
      }
    }

    if ("http".equalsIgnoreCase(scheme)) {
      final int sdkUrlMarkerIndex = path.indexOf(SDK_URL_MARKER);
      if (sdkUrlMarkerIndex >= 0) {
        // http://localhost:63343/dart-tagtree/example/packages/$sdk/lib/_internal/js_runtime/lib/js_helper.dart
        final String relPath = path.substring(sdkUrlMarkerIndex + SDK_URL_MARKER.length());
        return DartUrlResolver.findFileInDartSdkLibFolder(project, DartSdk.getDartSdk(project), DartUrlResolver.DART_PREFIX + relPath);
      }

      final int packageUrlMarkerIndex = path.lastIndexOf(PACKAGE_URL_MARKER);
      if (packageUrlMarkerIndex >= 0) {
        // http://localhost:63343/DartSample2/web/packages/browser/dart.js or http://localhost:63343/DartSample2/packages/DartSample2/src/myFile.dart
        // First make sure that this URL relates to Dart. 'packageUrlMarkerIndex >= 0' condition is not strict enough to guarantee that this is a Dart project
        final VirtualFile contextFile = findContextFile(project, requestor);
        if (contextFile != null) {
          final String packageUri = PACKAGE_PREFIX + path.substring(packageUrlMarkerIndex + PACKAGE_URL_MARKER.length());
          return ReadAction.compute(() -> DartUrlResolver.getInstance(project, contextFile).findFileByDartUrl(packageUri));
        }
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
    return DartUrlResolver.DART_SCHEME.equals(url.getScheme()) || DartUrlResolver.PACKAGE_SCHEME.equals(url.getScheme())
           ? DartFileType.INSTANCE
           : null;
  }
}