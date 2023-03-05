// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.karma.server;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.javascript.karma.KarmaConfig;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.execution.ParametersListUtil;
import com.intellij.webcore.util.JsonUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KarmaServerState {

  private static final Logger LOG = Logger.getInstance(KarmaServerState.class);
  private static final String BROWSER_CONNECTED_EVENT_TYPE = "browserConnected";
  private static final String BROWSER_DISCONNECTED_EVENT_TYPE = "browserDisconnected";
  private static final Pattern SERVER_PORT_LINE_PATTERN = Pattern.compile("Karma.+server started at http[s]?://[^:]+:(\\d+)/.*$");

  private static final String[][] FAILED_TO_START_BROWSER_PATTERNS = new String[][] {
    {"ERROR [launcher]: No binary for ", " browser on your platform.\n"},
    {"ERROR [launcher]: Cannot start ", "\n"}
  };

  private final KarmaServer myServer;
  private final List<String> myOverriddenBrowsers;
  private final ConcurrentMap<String, CapturedBrowser> myCapturedBrowsers = Maps.newConcurrentMap();
  private final AtomicInteger myBoundServerPort = new AtomicInteger(-1);
  private final AtomicBoolean myBrowsersReady = new AtomicBoolean(false);
  private final List<String> myFailedToStartBrowsers = ContainerUtil.createLockFreeCopyOnWriteList();
  private volatile KarmaConfig myConfig;

  public KarmaServerState(@NotNull KarmaServer server, @NotNull String configurationFilePath, @NotNull String workingDirectory) {
    myServer = server;
    myOverriddenBrowsers = parseBrowsers(findBrowsers(server.getServerSettings().getKarmaOptions()));
    myServer.registerStreamEventHandler(new BrowserEventHandler(BROWSER_CONNECTED_EVENT_TYPE));
    myServer.registerStreamEventHandler(new BrowserEventHandler(BROWSER_DISCONNECTED_EVENT_TYPE));
    myServer.registerStreamEventHandler(new BrowserCapturingFailedEventHandler());
    myServer.registerStreamEventHandler(new ConfigHandler(configurationFilePath, workingDirectory));
  }

  @NotNull
  private static String findBrowsers(@NotNull String karmaOptions) {
    String singleOptionPrefix = "--browsers=";
    List<String> options = ParametersListUtil.parse(karmaOptions);
    Optional<String> singleOption = options.stream().filter(s -> s.startsWith(singleOptionPrefix)).findFirst();
    if (singleOption.isPresent()) {
      return singleOption.get().substring(singleOptionPrefix.length());
    }
    int ind = options.indexOf("--browsers");
    if (ind >= 0 && ind < options.size() - 1) {
      return options.get(ind + 1);
    }
    return "";
  }

  @Nullable
  private static List<String> parseBrowsers(@NotNull String browsersStr) {
    if (StringUtil.isEmptyOrSpaces(browsersStr)) {
      return null;
    }
    Splitter splitter = Splitter.on(',').trimResults().omitEmptyStrings();
    return splitter.splitToList(browsersStr);
  }

  private void handleBrowsersChange(@NotNull String eventType,
                                    @NotNull String browserId,
                                    @NotNull String browserName,
                                    @Nullable Boolean autoCaptured) {
    if (BROWSER_CONNECTED_EVENT_TYPE.equals(eventType)) {
      CapturedBrowser browser = new CapturedBrowser(browserName, browserId, ObjectUtils.notNull(autoCaptured, true));
      myCapturedBrowsers.put(browserId, browser);
    }
    else {
      myCapturedBrowsers.remove(browserId);
    }
    updateBrowsersReadyStatus();
  }

  private void updateBrowsersReadyStatus() {
    boolean ready = isCapturedBrowsersQuorum();
    if (myBrowsersReady.compareAndSet(!ready, ready)) {
      myServer.fireOnBrowsersReady(ready);
    }
  }

  private boolean isCapturedBrowsersQuorum() {
    List<String> expectedBrowsers = myOverriddenBrowsers;
    if (expectedBrowsers == null) {
      KarmaConfig config = myConfig;
      if (config == null) {
        return true;
      }
      expectedBrowsers = config.getBrowsers();
    }
    Set<String> expectedBrowserSet = new HashSet<>(expectedBrowsers);
    myFailedToStartBrowsers.forEach(expectedBrowserSet::remove);
    if (ContainerUtil.exists(myCapturedBrowsers.values(), o -> !o.isAutoCaptured())) {
      return true;
    }
    long autoCapturedCount = myCapturedBrowsers.values().stream().filter(o -> o.isAutoCaptured()).count();
    return autoCapturedCount > 0 && expectedBrowserSet.size() <= autoCapturedCount;
  }

  public boolean areBrowsersReady() {
    return myBrowsersReady.get();
  }

  public int getServerPort() {
    return myBoundServerPort.get();
  }

  @Nullable
  public KarmaConfig getKarmaConfig() {
    return myConfig;
  }

  public void onStandardOutputLineAvailable(@NotNull String line) {
    int serverPort = myBoundServerPort.get();
    if (serverPort == -1) {
      serverPort = parseServerPort(line);
      if (serverPort != -1 && myBoundServerPort.compareAndSet(-1, serverPort)) {
        myServer.fireOnPortBound();
      }
    }
    if (!myBrowsersReady.get()) {
      String failedToStartBrowser = parseFailedToStartBrowser(line);
      if (failedToStartBrowser != null) {
        onBrowserCapturingFailed(failedToStartBrowser);
      }
    }
  }

  private void onBrowserCapturingFailed(@NotNull String notCapturedBrowser) {
    LOG.info("Browser " + notCapturedBrowser + " failed to be captured");
    myFailedToStartBrowsers.add(notCapturedBrowser);
    updateBrowsersReadyStatus();
  }

  @Nullable
  private static String parseFailedToStartBrowser(@NotNull String line) {
    for (String[] pattern : FAILED_TO_START_BROWSER_PATTERNS) {
      String failedToStartBrowser = getInnerSubstring(line, pattern[0], pattern[1]);
      if (failedToStartBrowser != null) {
        return failedToStartBrowser;
      }
    }
    return null;
  }

  private static int parseServerPort(@NotNull String text) {
    Matcher m = SERVER_PORT_LINE_PATTERN.matcher(text);
    if (m.find()) {
      String portStr = m.group(1);
      try {
        return Integer.parseInt(portStr);
      }
      catch (NumberFormatException e) {
        LOG.warn("Can't parse web server port from '" + text + "'");
      }
    }
    return -1;
  }

  @Nullable
  private static String getInnerSubstring(@NotNull String str, @NotNull String prefix, @NotNull String suffix) {
    if (str.startsWith(prefix) && str.endsWith(suffix) && prefix.length() + suffix.length() <= str.length()) {
      return str.substring(prefix.length(), str.length() - suffix.length());
    }
    return null;
  }

  private final class BrowserEventHandler implements StreamEventHandler {

    private final String myEventType;

    private BrowserEventHandler(@NotNull String eventType) {
      myEventType = eventType;
    }

    @NotNull
    @Override
    public String getEventType() {
      return myEventType;
    }

    @Override
    public void handle(@NotNull JsonElement eventBody) {
      if (eventBody.isJsonObject()) {
        JsonObject event = eventBody.getAsJsonObject();
        String id = JsonUtil.getChildAsString(event, "id");
        String name = JsonUtil.getChildAsString(event, "name");
        Boolean autoCaptured = JsonUtil.getChildAsBooleanObj(event, "isAutoCaptured");
        if (id != null && name != null) {
          handleBrowsersChange(myEventType, id, name, autoCaptured);
        }
        else {
          LOG.warn("Illegal browser event. Type: " + myEventType + ", body: " + eventBody);
        }
      }
    }
  }

  private class ConfigHandler implements StreamEventHandler {

    private final File myConfigurationFileDir;

    public ConfigHandler(@NotNull String configurationFilePath, @NotNull String workingDirectory) {
      File configFile = new File(configurationFilePath);
      if (configFile.isFile()) {
        myConfigurationFileDir = configFile.getParentFile();
      }
      else {
        myConfigurationFileDir = new File(workingDirectory);
      }
    }

    @NotNull
    @Override
    public String getEventType() {
      return "configFile";
    }

    @Override
    public void handle(@NotNull JsonElement eventBody) {
      myConfig = KarmaConfig.parseFromJson(eventBody, myConfigurationFileDir);
    }
  }

  private class BrowserCapturingFailedEventHandler implements StreamEventHandler {
    @NotNull
    @Override
    public String getEventType() {
      return "browserCapturingFailed";
    }

    @Override
    public void handle(@NotNull JsonElement eventBody) {
      if (!eventBody.isJsonObject()) {
        LOG.warn("Not an object");
        return;
      }
      String name = JsonUtil.getChildAsString(eventBody.getAsJsonObject(), "browserLauncherName");
      if (name != null) {
        onBrowserCapturingFailed(name);
      }
      else {
        LOG.warn("Unspecified name");
      }
    }
  }
}
