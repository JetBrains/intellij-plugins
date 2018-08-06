// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.intellij.plugins.markdown.ui.preview;

import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedStream;
import org.intellij.plugins.markdown.settings.MarkdownCssSettings;
import org.intellij.plugins.markdown.ui.preview.javafx.MarkdownJavaFxHtmlPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.ide.BuiltInServerManager;
import org.jetbrains.ide.HttpRequestHandler;
import org.jetbrains.io.FileResponses;
import org.jetbrains.io.FileResponsesKt;
import org.jetbrains.io.Responses;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class PreviewStaticServer extends HttpRequestHandler {
  private static final Logger LOG = Logger.getInstance(PreviewStaticServer.class);

  public static final String INLINE_CSS_FILENAME = "inline.css";

  private static final String PREFIX = "/api/markdown-preview/";

  @Nullable
  private String myInlineStyle = null;

  private long myInlineStyleTimestamp = 0;

  public static PreviewStaticServer getInstance() {
    return HttpRequestHandler.Companion.getEP_NAME().findExtension(PreviewStaticServer.class);
  }

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

  public void setInlineStyle(@Nullable String inlineStyle) {
    myInlineStyle = inlineStyle;
    myInlineStyleTimestamp = System.currentTimeMillis();
  }

  @Override
  public boolean isSupported(@NotNull FullHttpRequest request) {
    return super.isSupported(request) && request.uri().startsWith(PREFIX);
  }

  @Override
  public boolean process(@NotNull QueryStringDecoder urlDecoder,
                         @NotNull FullHttpRequest request,
                         @NotNull ChannelHandlerContext context) {
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
                   MarkdownJavaFxHtmlPanel.class,
                   fileName);
    }
    else if ("styles".equals(contentType) && MarkdownHtmlPanel.STYLES.contains(fileName)) {
      if (INLINE_CSS_FILENAME.equals(fileName)) {
        sendInlineStyle(request, context.channel());
      }
      else {
        sendResource(request,
                     context.channel(),
                     MarkdownCssSettings.class,
                     fileName);
      }
    }
    else {
      return false;
    }

    return true;
  }


  private void sendInlineStyle(@NotNull HttpRequest request, @NotNull Channel channel) {
    final HttpResponse response = FileResponses.INSTANCE.prepareSend(request, channel, myInlineStyleTimestamp, INLINE_CSS_FILENAME);
    if (response == null) {
      return;
    }

    boolean isKeepAlive = Responses.addKeepAliveIfNeed(response, request);

    if (myInlineStyle == null) {
      Responses.send(HttpResponseStatus.NOT_FOUND, channel, request);
      return;
    }

    channel.write(response);
    if (request.method() != HttpMethod.HEAD) {
      byte[] data = myInlineStyle.getBytes(StandardCharsets.UTF_8);
      HttpUtil.setContentLength(response, data.length);
      channel.write(new ChunkedStream(new ByteArrayInputStream(data)));
    }

    FileResponsesKt.flushChunkedResponse(channel, isKeepAlive);
  }

  private static void sendResource(@NotNull HttpRequest request,
                                   @NotNull Channel channel,
                                   @NotNull Class<?> clazz,
                                   @NotNull String resourceName) {
    final String fileName = resourceName.substring(resourceName.lastIndexOf('/') + 1);
    final HttpResponse response = FileResponses.INSTANCE.prepareSend(request, channel, ApplicationInfo.getInstance().getBuildDate().getTimeInMillis(), fileName);
    if (response == null) {
      return;
    }

    boolean isKeepAlive = Responses.addKeepAliveIfNeed(response, request);

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
    catch (IOException e) {
      LOG.warn(e);
    }

    FileResponsesKt.flushChunkedResponse(channel, isKeepAlive);
  }
}
