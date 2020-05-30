// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.karma;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.javascript.nodejs.interpreter.NodeInterpreterUtil;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreter;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.webcore.util.JsonUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KarmaConfig {

  private static final Logger LOG = Logger.getInstance(KarmaConfig.class);
  private static final String BASE_PATH = "basePath";
  private static final String BROWSERS = "browsers";
  private static final String PROTOCOL = "protocol";
  private static final String HOST_NAME = "hostname";
  private static final String URL_ROOT = "urlRoot";
  private static final String DEBUG_INFO = "debugInfo";
  private static final String REMOTE_DEBUGGING_PORT = "--remote-debugging-port";

  private final List<String> myBrowsers;
  private final String myBasePath;
  private final String myProtocol;
  private final String myHostname;
  private final String myUrlRoot;
  private final int myRemoteDebuggingPort;

  public KarmaConfig(@NotNull String basePath,
                     @NotNull List<String> browsers,
                     @NotNull String protocol,
                     @NotNull String hostname,
                     @NotNull String urlRoot,
                     int remoteDebuggingPort) {
    myBasePath = basePath;
    myBrowsers = ImmutableList.copyOf(browsers);
    myProtocol = protocol;
    myHostname = hostname;
    myUrlRoot = urlRoot;
    myRemoteDebuggingPort = remoteDebuggingPort;
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

  /**
   * @return remote debugging port, or -1 if no browser was launched with --remote-debugging-port flag
   */
  public int getRemoteDebuggingPort() {
    return myRemoteDebuggingPort;
  }

  @Nullable
  public static KarmaConfig parseFromJson(@NotNull JsonElement jsonElement,
                                          @NotNull File configurationFileDir,
                                          @NotNull NodeJsInterpreter interpreter) {
    if (jsonElement.isJsonObject()) {
      JsonObject rootObject = jsonElement.getAsJsonObject();

      List<String> browsers = parseBrowsers(rootObject);
      String basePath = parseBasePath(jsonElement, rootObject, configurationFileDir, interpreter);
      String protocol = ObjectUtils.notNull(JsonUtil.getChildAsString(rootObject, PROTOCOL), "http:");
      String hostname = parseHostname(jsonElement, rootObject);
      String urlRoot = parseUrlRoot(jsonElement, rootObject);
      JsonObject debugInfoObj = JsonUtil.getChildAsObject(rootObject, DEBUG_INFO);
      int remoteDebuggingPort = debugInfoObj != null ? JsonUtil.getChildAsInteger(debugInfoObj, REMOTE_DEBUGGING_PORT, -1) : -1;

      return new KarmaConfig(basePath, browsers, protocol, hostname, urlRoot, remoteDebuggingPort);
    }
    return null;
  }

  @NotNull
  private static String parseBasePath(@NotNull JsonElement all,
                                      @NotNull JsonObject obj,
                                      @NotNull File configurationFileDir,
                                      @NotNull NodeJsInterpreter interpreter) {
    String basePath = JsonUtil.getChildAsString(obj, BASE_PATH);
    if (basePath == null) {
      LOG.warn("Can not parse Karma config.basePath from " + all.toString());
      basePath = configurationFileDir.getAbsolutePath();
    }
    return NodeInterpreterUtil.convertRemotePathToLocal(basePath, interpreter);
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
    hostname = StringUtil.toLowerCase(hostname);
    return hostname;
  }

  @NotNull
  private static List<String> parseBrowsers(@NotNull JsonObject obj) {
    JsonElement browsersElement = obj.get(BROWSERS);
    if (browsersElement != null && browsersElement.isJsonArray()) {
      JsonArray browsersArray = browsersElement.getAsJsonArray();
      List<String> browsers = new ArrayList<>();
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
