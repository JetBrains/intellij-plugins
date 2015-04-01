package com.jetbrains.lang.dart.pubServer;

import com.google.common.net.UrlEscapers;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.builtInWebServer.WebServerPathHandlerAdapter;
import org.jetbrains.builtInWebServer.WebServerPathToFileManager;

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

    PubServerManager.getInstance(project).send(context, request, servedDirAndPathForPubServer.first, servedDirAndPathForPubServer.second);
    return true;
  }

  @Nullable
  private static Pair<VirtualFile, String> getServedDirAndPathForPubServer(@NotNull final Project project, @NotNull final String path) {
    // File with requested path may not exist, pub server will generate and serve it.
    // Here we find deepest (if nested) Dart project (aka Dart package) folder and its existing subfolder that can be served by pub server.

    VirtualFile servedDir = null;
    String pubServePath = null;

    int slashIndex = -1;
    while ((slashIndex = path.indexOf('/', slashIndex + 1)) != -1) {
      final String pathPart = path.substring(0, slashIndex);
      final VirtualFile dir = WebServerPathToFileManager.getInstance(project).get(pathPart);
      if (dir == null || !dir.isDirectory()) {
        continue;
      }

      final VirtualFile parentDir = dir.getParent();
      if (parentDir != null && parentDir.findChild(PubspecYamlUtil.PUBSPEC_YAML) != null) {
        if ("build".equals(dir.getName())) {
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
