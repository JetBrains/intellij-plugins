package com.jetbrains.lang.dart.pubServer;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.builtInWebServer.WebServerPathHandlerAdapter;

public class PubServerPathHandler extends WebServerPathHandlerAdapter {
  @Override
  protected boolean process(@NotNull final String path,
                            @NotNull final Project project,
                            @NotNull final FullHttpRequest request,
                            @NotNull ChannelHandlerContext context) {
    if (!isDartPath(project, path)) return false;

    final PubServerService pubServer = PubServerService.getInstance(project);
    final String pathForPubServer = path.startsWith("web") ? path.substring("web".length()) : path;
    pubServer.sendToPubServe(context, request, pathForPubServer);

    return true;
  }

  private static boolean isDartPath(@NotNull final Project project, @NotNull final String path) {
    // todo respect module name in path, nested Dart projects, etc.
    final VirtualFile projectBaseDir = project.getBaseDir();
    return projectBaseDir != null && projectBaseDir.findChild(PubspecYamlUtil.PUBSPEC_YAML) != null;
  }
}
