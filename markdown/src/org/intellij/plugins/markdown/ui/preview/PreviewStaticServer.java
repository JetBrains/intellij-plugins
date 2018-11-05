// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.intellij.plugins.markdown.ui.preview;

import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Url;
import com.intellij.util.Urls;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.intellij.plugins.markdown.settings.MarkdownCssSettings;
import org.intellij.plugins.markdown.ui.preview.javafx.MarkdownJavaFxHtmlPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.ide.BuiltInServerManager;
import org.jetbrains.ide.HttpRequestHandler;
import org.jetbrains.io.FileResponses;
import org.jetbrains.io.Responses;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class PreviewStaticServer extends HttpRequestHandler {
  public static final String INLINE_CSS_FILENAME = "inline.css";
  private static final Logger LOG = Logger.getInstance(PreviewStaticServer.class);
  private static final String PREFIX = "/api/markdown-preview/";
  private static final String GENERATED_IMAGES_PREFIX = "generatedImages";
  private static final String ABSOLUTE_PATH_IMAGES_PREFIX = "images";

  @Nullable
  private byte[] myInlineStyleBytes = null;
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
    Url url = Urls.parseEncoded("http://localhost:" + BuiltInServerManager.getInstance().getPort() + PREFIX + staticPath);
    return BuiltInServerManager.getInstance().addAuthToken(Objects.requireNonNull(url)).toExternalForm();
  }

  @NotNull
  public static String getGeneratedImageUrl(@NotNull String pluginPrefix, @NotNull String fileName) {
    return getStaticUrl(GENERATED_IMAGES_PREFIX + "/" + pluginPrefix + "/" + fileName);
  }

  @NotNull
  public static String getAbsolutePathImageUrl(@NotNull String filePath) {
    return getStaticUrl(ABSOLUTE_PATH_IMAGES_PREFIX + "?" + filePath);
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
    myInlineStyleBytes = inlineStyle == null ? null : inlineStyle.getBytes(StandardCharsets.UTF_8);
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
    if (payLoad.startsWith(GENERATED_IMAGES_PREFIX)) {
      String imagePluginPrefix = payLoad.substring(GENERATED_IMAGES_PREFIX.length() + File.separator.length());
      String pluginPrefix = StringUtil.substringBefore(imagePluginPrefix, "/");
      String imageRelativePath = StringUtil.substringAfter(imagePluginPrefix, "/");

      if (pluginPrefix != null && imageRelativePath != null) {
        sendPluginImage(request, context.channel(), pluginPrefix, imageRelativePath);
        return true;
      }
    }
    else if (payLoad.startsWith(ABSOLUTE_PATH_IMAGES_PREFIX)) {
      String imageAbsolutePath = urlDecoder.rawQuery();
      if (StringUtil.isNotEmpty(imageAbsolutePath)) {
        sendAbsolutePathImage(request, context.channel(), imageAbsolutePath);
        return true;
      }
    }

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
    if (FileResponses.INSTANCE.checkCache(request, channel, myInlineStyleTimestamp)) {
      return;
    }

    if (myInlineStyleBytes == null) {
      Responses.send(HttpResponseStatus.NOT_FOUND, channel, request);
      return;
    }

    ByteBuf inlineStyleBuf = Unpooled.wrappedBuffer(Arrays.copyOf(myInlineStyleBytes, myInlineStyleBytes.length));

    FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, inlineStyleBuf);
    response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/css");
    response.headers().set(HttpHeaderNames.CACHE_CONTROL, "private, must-revalidate");
    response.headers().set(HttpHeaderNames.LAST_MODIFIED, new Date(myInlineStyleTimestamp));
    Responses.send(response, channel, request);
  }

  private static void sendAbsolutePathImage(@NotNull FullHttpRequest request, @NotNull Channel channel, @NotNull String imageAbsolutePath) {
    long lastModified = ApplicationInfo.getInstance().getBuildDate().getTimeInMillis();
    if (FileResponses.INSTANCE.checkCache(request, channel, lastModified)) {
      return;
    }

    VirtualFile imageFile = LocalFileSystem.getInstance().findFileByPath(imageAbsolutePath);
    if (imageFile == null) {
      Responses.send(HttpResponseStatus.NOT_FOUND, channel, request);
      return;
    }

    byte[] data;
    try {
      data = imageFile.contentsToByteArray();
    }
    catch (IOException e) {
      LOG.warn(e);
      Responses.send(HttpResponseStatus.INTERNAL_SERVER_ERROR, channel, request);
      return;
    }

    sendResource(request, channel, lastModified, data, imageFile.getName());
  }

  private static void sendPluginImage(@NotNull FullHttpRequest request,
                                      @NotNull Channel channel,
                                      @NotNull String pluginPrefix,
                                      @NotNull String imageRelativePath) {
    long lastModified = ApplicationInfo.getInstance().getBuildDate().getTimeInMillis();
    if (FileResponses.INSTANCE.checkCache(request, channel, lastModified)) {
      return;
    }

    for (File pluginSystemPath : MarkdownCodeFencePluginCache.getPluginSystemPaths()) {
      if (!pluginSystemPath.getPath().endsWith(pluginPrefix)) {
        continue;
      }

      Path imageFile = Paths.get(pluginSystemPath.getPath(), imageRelativePath);
      if (imageFile.toFile().exists()) {
        byte[] data;
        try {
          data = FileUtilRt.loadBytes(new FileInputStream(imageFile.toFile()));
        }
        catch (IOException e) {
          LOG.warn(e);
          Responses.send(HttpResponseStatus.INTERNAL_SERVER_ERROR, channel, request);
          return;
        }

        sendResource(request, channel, lastModified, data, imageFile.getFileName().toString());

        return;
      }
    }

    Responses.send(HttpResponseStatus.NOT_FOUND, channel, request);
  }

  private static void sendResource(@NotNull HttpRequest request,
                                   @NotNull Channel channel,
                                   @NotNull Class<?> clazz,
                                   @NotNull String resourceName) {
    long lastModified = ApplicationInfo.getInstance().getBuildDate().getTimeInMillis();
    if (FileResponses.INSTANCE.checkCache(request, channel, lastModified)) {
      return;
    }

    byte[] data;
    try (final InputStream inputStream = clazz.getResourceAsStream(resourceName)) {
      if (inputStream == null) {
        Responses.send(HttpResponseStatus.NOT_FOUND, channel, request);
        return;
      }

      data = FileUtilRt.loadBytes(inputStream);
    }
    catch (IOException e) {
      LOG.warn(e);
      Responses.send(HttpResponseStatus.INTERNAL_SERVER_ERROR, channel, request);
      return;
    }
    sendResource(request, channel, lastModified, data, resourceName);
  }

  private static void sendResource(@NotNull HttpRequest request,
                                   @NotNull Channel channel,
                                   long lastModified,
                                   @NotNull byte[] data,
                                   @NotNull String path) {
    FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(data));
    response.headers().set(HttpHeaderNames.CONTENT_TYPE, FileResponses.INSTANCE.getContentType(path));
    response.headers().set(HttpHeaderNames.CACHE_CONTROL, "private, must-revalidate");
    response.headers().set(HttpHeaderNames.LAST_MODIFIED, new Date(lastModified));
    Responses.send(response, channel, request);
  }
}
