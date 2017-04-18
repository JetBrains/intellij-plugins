package org.intellij.plugins.markdown.ui.preview;

import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.CharsetToolkit;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedStream;
import org.intellij.plugins.markdown.settings.MarkdownCssSettings;
import org.intellij.plugins.markdown.ui.preview.javafx.JavaFxHtmlPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.ide.BuiltInServerManager;
import org.jetbrains.ide.HttpRequestHandler;
import org.jetbrains.io.FileResponses;
import org.jetbrains.io.Responses;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;

public class PreviewStaticServer extends HttpRequestHandler {

  private static final String PREFIX = "/api/markdown-preview/";

  @NotNull
  public static String createCSP(@NotNull List<String> scripts, @NotNull List<String> styles) {
    return "default-src 'none'; script-src " + StringUtil.join(scripts, " ") + "; "
           + "style-src https: " + StringUtil.join(styles, " ") + "; "
           + "img-src *; connect-src 'none'; font-src *; " +
           "object-src 'none'; media-src 'none'; child-src 'none';";
  }

  @NotNull
  private static String getStaticUrl(@NotNull String staticPath) {
    return "http://localhost:" + BuiltInServerManager.getInstance().getPort() + PREFIX + staticPath;
  }

  @NotNull
  public static String getScriptUrl(@NotNull String scriptFileName) {
    return getStaticUrl("scripts/" + scriptFileName);
  }

  @NotNull
  public static String getStyleUrl(@NotNull String scriptFileName) {
    return getStaticUrl("styles/" + scriptFileName);
  }

  @Nullable
  public static String getCSPHash(@NotNull String inlineCSS) {
    if (inlineCSS.isEmpty()) {
      return null;
    }

    try {
      String algorithm = "sha-512";
      MessageDigest instance = MessageDigest.getInstance(algorithm);
      byte[] digest = instance.digest(inlineCSS.getBytes(CharsetToolkit.UTF8_CHARSET));
      return '\'' + algorithm.replace("-", "") + '-' + new String(Base64.getEncoder().encode(digest), CharsetToolkit.UTF8_CHARSET) + '\'';
    }
    catch (NoSuchAlgorithmException e) {
      return null;
    }
  }

  @Override
  public boolean isSupported(@NotNull FullHttpRequest request) {
    return super.isSupported(request) && request.uri().startsWith(PREFIX);
  }

  @Override
  public boolean process(@NotNull QueryStringDecoder urlDecoder,
                         @NotNull FullHttpRequest request,
                         @NotNull ChannelHandlerContext context) throws IOException {
    final String path = urlDecoder.path();
    if (!path.startsWith(PREFIX)) {
      throw new IllegalStateException("prefix should have been checked by #isSupported");
    }

    final String payLoad = path.substring(PREFIX.length());
    final List<String> typeAndName = StringUtil.split(payLoad, "/");

    if (typeAndName.size() != 2) {
      return false;
    }
    final String contentType = typeAndName.get(0);
    final String fileName = typeAndName.get(1);

    if ("scripts".equals(contentType) && MarkdownHtmlPanel.SCRIPTS.contains(fileName)) {
      sendResource(request,
                   context.channel(),
                   JavaFxHtmlPanel.class,
                   fileName);
    }
    else if ("styles".equals(contentType) && MarkdownHtmlPanel.STYLES.contains(fileName)) {
      sendResource(request,
                   context.channel(),
                   MarkdownCssSettings.class,
                   fileName);
    }
    return false;
  }


  private static void sendResource(@NotNull HttpRequest request,
                                   @NotNull Channel channel,
                                   @NotNull Class<?> clazz,
                                   @NotNull String resourceName) {
    final String fileName = resourceName.substring(resourceName.lastIndexOf('/') + 1);
    final HttpResponse response =
      FileResponses.INSTANCE.prepareSend(request, channel, 0, fileName, EmptyHttpHeaders.INSTANCE);
    if (response == null) {
      return;
    }

    Responses.addKeepAliveIfNeed(response, request);

    try (final InputStream resource = clazz.getResourceAsStream(resourceName)) {
      if (resource == null) {
        Responses.send(HttpResponseStatus.NOT_FOUND, channel, request);
        return;
      }

      channel.write(response);
      if (request.method() != HttpMethod.HEAD) {
        channel.write(new ChunkedStream(resource));
      }
    }
    catch (IOException ignored) {
    }

    final ChannelFuture future = channel.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
    future.addListener(ChannelFutureListener.CLOSE);
  }
}
