package com.jetbrains.lang.dart.pubServer;

import com.google.common.net.UrlEscapers;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.io.NettyKt;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.util.DartUrlResolver;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.builtInWebServer.*;

import java.util.regex.Matcher;

public class PubServerPathHandler extends WebServerPathHandlerAdapter {
  private static final Logger LOG = Logger.getInstance(PubServerPathHandler.class.getName());

  @Override
  protected boolean process(@NotNull final String path,
                            @NotNull final Project project,
                            @NotNull final FullHttpRequest request,
                            @NotNull ChannelHandlerContext context) {
    final DartSdk sdk = DartSdk.getDartSdk(project);
    if (sdk == null || StringUtil.compareVersionNumbers(sdk.getVersion(), "1.6") < 0) return false;

    final Pair<VirtualFile, String> servedDirAndPathForPubServer = getServedDirAndPathForPubServer(project, path);
    if (servedDirAndPathForPubServer == null) return false;

    boolean isSignedRequest = BuiltInWebServerKt.isSignedRequest(request);
    HttpHeaders validateResult = null;
    String userAgent = NettyKt.getUserAgent(request);
    if (!isSignedRequest &&
        userAgent != null &&
        NettyKt.isRegularBrowser(request) &&
        NettyKt.getOrigin(request) == null &&
        NettyKt.getReferrer(request) == null &&
        (request.uri().endsWith(".map") || request.uri().endsWith(".dart"))) {
      Matcher matcher = DefaultWebServerPathHandlerKt.getChromeVersionFromUserAgent().matcher(userAgent);
      if (matcher.find() && StringUtil.compareVersionNumbers(matcher.group(1), "51") >= 0) {
        validateResult = EmptyHttpHeaders.INSTANCE;
      }
    }

    if (validateResult == null) {
      validateResult = BuiltInWebServerKt.validateToken(request, context.channel(), isSignedRequest);
    }

    if (validateResult != null) {
      PubServerManager.getInstance(project).send(context.channel(), request, validateResult, servedDirAndPathForPubServer.first, servedDirAndPathForPubServer.second);
    }
    return true;
  }

  @Nullable
  private static Pair<VirtualFile, String> getServedDirAndPathForPubServer(@NotNull final Project project, @NotNull final String path) {
    // File with requested path may not exist, pub server will generate and serve it.
    // Here we find deepest (if nested) Dart project (aka Dart package) folder and its existing subfolder that can be served by pub server.

    // There may be 2 content roots with web/foo.html and web/bar.html files in them correspondingly. We need to catch the correct 'web' folder.
    // First see if full path can be resolved to a file
    final PathInfo fullPathInfo = WebServerPathToFileManager.getInstance(project).getPathInfo(path);
    final VirtualFile file = fullPathInfo == null
                             ? null
                             : LocalFileSystem.getInstance().findFileByPath(FileUtilRt.toSystemIndependentName(fullPathInfo.getFilePath()));
    if (file != null) {
      final VirtualFile pubspec = PubspecYamlUtil.findPubspecYamlFile(project, file);
      if (pubspec == null) return null;

      final VirtualFile dartRoot = pubspec.getParent();
      final String relativePath = FileUtil.getRelativePath(dartRoot.getPath(), file.getPath(), '/');
      // we only handle files 2 levels deeper than the Dart project root
      final int slashIndex = relativePath == null ? -1 : relativePath.indexOf('/');
      final String folderName = slashIndex == -1 ? null : relativePath.substring(0, slashIndex);

      if (folderName == null ||
          "build".equals(folderName) ||
          "lib".equals(folderName) ||
          DartUrlResolver.PACKAGES_FOLDER_NAME.equals(folderName)) {
        return null;
      }

      final VirtualFile servedDir = dartRoot.findChild(folderName);
      final String pubServePath = relativePath.substring(slashIndex);
      return Pair.create(servedDir, escapeUrl(pubServePath));
    }

    // If above failed then take the longest path part that corresponds to an existing folder
    VirtualFile servedDir = null;
    String pubServePath = null;

    int slashIndex = -1;
    while ((slashIndex = path.indexOf('/', slashIndex + 1)) != -1) {
      final String pathPart = path.substring(0, slashIndex);
      PathInfo dirInfo = WebServerPathToFileManager.getInstance(project).getPathInfo(pathPart);
      if (dirInfo == null || !dirInfo.isDirectory()) {
        continue;
      }

      VirtualFile dir = LocalFileSystem.getInstance().findFileByPath(FileUtilRt.toSystemIndependentName(dirInfo.getFilePath()));
      final VirtualFile parentDir = dir == null ? null : dir.getParent();
      if (parentDir != null && parentDir.findChild(PubspecYamlUtil.PUBSPEC_YAML) != null) {
        if ("build".equals(dirInfo.getName()) ||
            "lib".equals(dirInfo.getName()) ||
            DartUrlResolver.PACKAGES_FOLDER_NAME.equals(dir.getName())) {
          return null; // contents of "build" folder should be served by the IDE internal web server directly, i.e. without pub serve
        }

        servedDir = dir;
        pubServePath = path.substring(slashIndex);
        // continue looking for nested Dart project
      }
    }

    return servedDir != null ? Pair.create(servedDir, escapeUrl(pubServePath)) : null;
  }

  private static String escapeUrl(@NotNull final String path) {
    try {
      // need to restore slash separators after UrlEscapers.urlPathSegmentEscaper work
      return StringUtil.replace(UrlEscapers.urlPathSegmentEscaper().escape(path), "%2F", "/");
    }
    catch (Exception e) {
      LOG.warn(path, e);
      return path;
    }
  }
}