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
import com.intellij.webcore.util.JsonUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.List;
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
  private static final Pattern SERVER_PORT_LINE_PATTERN = Pattern.compile("Karma.+server started at http://[^:]+:(\\d+)/.*$");

  private static final String[][] FAILED_TO_START_BROWSER_PATTERNS = new String[][] {
    {"ERROR [launcher]: No binary for ", " browser on your platform.\n"},
    {"WARN [launcher]: Can not load \"", "\", it is not registered!\n"},
    {"ERROR [launcher]: Cannot start ", "\n"}
  };

  private final KarmaServer myServer;
  private final List<String> myOverriddenBrowsers;
  private final ConcurrentMap<String, CapturedBrowser> myCapturedBrowsers = Maps.newConcurrentMap();
  private final AtomicInteger myBoundServerPort = new AtomicInteger(-1);
  private final AtomicBoolean myBrowsersReady = new AtomicBoolean(false);
  private final List<String> myFailedToStartBrowsers = ContainerUtil.createLockFreeCopyOnWriteList();
  private volatile KarmaConfig myConfig;

  public KarmaServerState(@NotNull KarmaServer server, @NotNull File configurationFile) {
    myServer = server;
    myOverriddenBrowsers = parseBrowsers(server.getServerSettings().getRunSettings().getBrowsers());
    myServer.registerStreamEventHandler(new BrowserEventHandler(BROWSER_CONNECTED_EVENT_TYPE));
    myServer.registerStreamEventHandler(new BrowserEventHandler(BROWSER_DISCONNECTED_EVENT_TYPE));
    myServer.registerStreamEventHandler(new ConfigHandler(configurationFile));
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
      boolean captured = ObjectUtils.notNull(autoCaptured, true);
      CapturedBrowser browser = new CapturedBrowser(browserName, browserId, captured);
      myCapturedBrowsers.put(browserId, browser);
      if (autoCaptured == Boolean.FALSE || canSetBrowsersReady()) {
        setBrowsersReady();
      }
    }
    else {
      myCapturedBrowsers.remove(browserId);
    }
  }

  private boolean canSetBrowsersReady() {
    List<String> expectedBrowsers = myOverriddenBrowsers;
    if (expectedBrowsers == null) {
      KarmaConfig config = myConfig;
      if (config == null) {
        return true;
      }
      expectedBrowsers = config.getBrowsers();
    }
    Set<String> expectedBrowserSet = ContainerUtil.newHashSet(expectedBrowsers);
    expectedBrowserSet.removeAll(myFailedToStartBrowsers);
    int autoCapturedBrowserCount = getAutoCapturedBrowserCount();
    return autoCapturedBrowserCount > 0 && expectedBrowserSet.size() <= autoCapturedBrowserCount;
  }

  private int getAutoCapturedBrowserCount() {
    int res = 0;
    for (CapturedBrowser browser : myCapturedBrowsers.values()) {
      if (browser.isAutoCaptured()) {
        res++;
      }
    }
    return res;
  }

  private void setBrowsersReady() {
    if (myBrowsersReady.compareAndSet(false, true)) {
      myServer.fireOnBrowsersReady();
    }
  }

  public boolean areBrowsersReady() {
    return myBrowsersReady.get();
  }

  @NotNull
  public Collection<CapturedBrowser> getCapturedBrowsers() {
    return myCapturedBrowsers.values();
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
        LOG.info("Browser " + failedToStartBrowser + " failed to start: " + line);
        myFailedToStartBrowsers.add(failedToStartBrowser);
        if (canSetBrowsersReady()) {
          setBrowsersReady();
        }
      }
    }
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

  private class BrowserEventHandler implements StreamEventHandler {

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
          LOG.warn("Illegal browser event. Type: " + myEventType + ", body: " + eventBody.toString());
        }
      }
    }
  }

  private class ConfigHandler implements StreamEventHandler {

    private final File myConfigurationFileDir;

    public ConfigHandler(@NotNull File configurationFile) {
      myConfigurationFileDir = configurationFile.getParentFile();
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

}
