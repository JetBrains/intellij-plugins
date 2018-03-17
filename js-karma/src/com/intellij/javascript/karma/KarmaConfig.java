package com.intellij.javascript.karma;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.ObjectUtils;
import com.intellij.webcore.util.JsonUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class KarmaConfig {

  private static final Logger LOG = Logger.getInstance(KarmaConfig.class);
  private static final String AUTO_WATCH = "autoWatch";
  private static final String BASE_PATH = "basePath";
  private static final String BROWSERS = "browsers";
  private static final String PROTOCOL = "protocol";
  private static final String HOST_NAME = "hostname";
  private static final String URL_ROOT = "urlRoot";
  private static final String WEBPACK = "webpack";
  private static final String DEBUG_INFO = "debugInfo";
  private static final String REMOTE_DEBUGGING_PORT = "--remote-debugging-port";

  private final boolean myAutoWatch;
  private final List<String> myBrowsers;
  private final String myBasePath;
  private final String myProtocol;
  private final String myHostname;
  private final String myUrlRoot;
  private final boolean myWebpack;
  private final int myRemoteDebuggingPort;

  public KarmaConfig(boolean autoWatch,
                     @NotNull String basePath,
                     @NotNull List<String> browsers,
                     @NotNull String protocol,
                     @NotNull String hostname,
                     @NotNull String urlRoot,
                     boolean webpack,
                     int remoteDebuggingPort) {
    myAutoWatch = autoWatch;
    myBasePath = basePath;
    myBrowsers = ImmutableList.copyOf(browsers);
    myProtocol = protocol;
    myHostname = hostname;
    myUrlRoot = urlRoot;
    myWebpack = webpack;
    myRemoteDebuggingPort = remoteDebuggingPort;
  }

  public boolean isAutoWatch() {
    return myAutoWatch;
  }

  @NotNull
  public String getBasePath() {
    return myBasePath;
  }

  @NotNull
  public List<String> getBrowsers() {
    return myBrowsers;
  }

  @NotNull
  public String getProtocol() {
    return myProtocol;
  }

  @NotNull
  public String getHostname() {
    return myHostname;
  }

  @NotNull
  public String getUrlRoot() {
    return myUrlRoot;
  }

  public boolean isWebpack() {
    return myWebpack;
  }

  /**
   * @return remote debugging port, or -1 if no browser was launched with --remote-debugging-port flag
   */
  public int getRemoteDebuggingPort() {
    return myRemoteDebuggingPort;
  }

  @Nullable
  public static KarmaConfig parseFromJson(@NotNull JsonElement jsonElement,
                                          @NotNull File configurationFileDir) {
    if (jsonElement.isJsonObject()) {
      JsonObject rootObject = jsonElement.getAsJsonObject();

      boolean autoWatch = JsonUtil.getChildAsBoolean(rootObject, AUTO_WATCH, false);
      List<String> browsers = parseBrowsers(rootObject);
      String basePath = parseBasePath(jsonElement, rootObject, configurationFileDir);
      String protocol = ObjectUtils.notNull(JsonUtil.getChildAsString(rootObject, PROTOCOL), "http:");
      String hostname = parseHostname(jsonElement, rootObject);
      String urlRoot = parseUrlRoot(jsonElement, rootObject);
      boolean webpack = JsonUtil.getChildAsBoolean(rootObject, WEBPACK, false);
      JsonObject debugInfoObj = JsonUtil.getChildAsObject(rootObject, DEBUG_INFO);
      int remoteDebuggingPort = debugInfoObj != null ? JsonUtil.getChildAsInteger(debugInfoObj, REMOTE_DEBUGGING_PORT, -1) : -1;

      return new KarmaConfig(autoWatch, basePath, browsers, protocol, hostname, urlRoot, webpack, remoteDebuggingPort);
    }
    return null;
  }

  @NotNull
  private static String parseBasePath(@NotNull JsonElement all,
                                      @NotNull JsonObject obj,
                                      @NotNull File configurationFileDir) {
    String basePath = JsonUtil.getChildAsString(obj, BASE_PATH);
    if (basePath == null) {
      LOG.warn("Can not parse Karma config.basePath from " + all.toString());
      basePath = configurationFileDir.getAbsolutePath();
    }
    return basePath;
  }

  private static String parseUrlRoot(@NotNull JsonElement all, @NotNull JsonObject obj) {
    String urlRoot = JsonUtil.getChildAsString(obj, URL_ROOT);
    if (urlRoot == null) {
      LOG.warn("Can not parse Karma config.urlRoot from " + all.toString());
      urlRoot = "/";
    }
    if (!urlRoot.startsWith("/")) {
      urlRoot = "/" + urlRoot;
    }
    if (urlRoot.length() > 1 && urlRoot.endsWith("/")) {
      urlRoot = urlRoot.substring(0, urlRoot.length() - 1);
    }
    return urlRoot;
  }

  private static String parseHostname(@NotNull JsonElement all, @NotNull JsonObject obj) {
    String hostname = JsonUtil.getChildAsString(obj, HOST_NAME);
    if (hostname == null) {
      LOG.warn("Can not parse Karma config.hostname from " + all.toString());
      hostname = "localhost";
    }
    hostname = hostname.toLowerCase(Locale.ENGLISH);
    return hostname;
  }

  @NotNull
  private static List<String> parseBrowsers(@NotNull JsonObject obj) {
    JsonElement browsersElement = obj.get(BROWSERS);
    if (browsersElement != null && browsersElement.isJsonArray()) {
      JsonArray browsersArray = browsersElement.getAsJsonArray();
      List<String> browsers = Lists.newArrayList();
      for (JsonElement browserElement : browsersArray) {
        String browser = JsonUtil.getString(browserElement);
        if (browser != null) {
          browsers.add(browser);
        }
      }
      return browsers;
    }
    return Collections.emptyList();
  }

}
