package com.intellij.javascript.karma.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.intellij.util.containers.ConcurrentHashMap;
import com.intellij.util.text.SemVer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
* @author Sergey Simonchik
*/
public class KarmaServerState implements ProcessListener {

  private static final Pattern WEB_SERVER_LINE_PATTERN = Pattern.compile("^INFO \\[.*\\]: Karma v(.+) server started at http://[^:]+:(\\d+)/.*$");
  private static final Logger LOG = Logger.getInstance(KarmaServerState.class);
  private static final String BROWSER_CONNECTED_EVENT_TYPE = "browserConnected";
  private static final String BROWSER_DISCONNECTED_EVENT_TYPE = "browserDisconnected";

  private final KarmaServer myServer;
  private final ConcurrentMap<String, String> myCapturedBrowsers = new ConcurrentHashMap<String, String>();
  private final StringBuilder myBuffer = new StringBuilder();
  private volatile int myWebServerPort = -1;
  private volatile int myRunnerPort = -1;

  public KarmaServerState(@NotNull KarmaServer server) {
    myServer = server;
    myServer.registerStreamEventHandler(new BrowserEventHandler(BROWSER_CONNECTED_EVENT_TYPE));
    myServer.registerStreamEventHandler(new BrowserEventHandler(BROWSER_DISCONNECTED_EVENT_TYPE));
  }

  @Override
  public void startNotified(ProcessEvent event) {
  }

  @Override
  public void processTerminated(ProcessEvent event) {
  }

  @Override
  public void processWillTerminate(ProcessEvent event, boolean willBeDestroyed) {
  }

  @Override
  public void onTextAvailable(ProcessEvent event, Key outputType) {
    if (outputType != ProcessOutputTypes.SYSTEM && outputType != ProcessOutputTypes.STDOUT) {
      myBuffer.append(event.getText());
      int startInd = 0;
      for (int i = 0; i < myBuffer.length(); i++) {
        if (myBuffer.charAt(i) == '\n') {
          String line = myBuffer.substring(startInd, i);
          handleStdout(line);
          startInd = i + 1;
        }
      }
      myBuffer.delete(0, startInd);
    }
  }

  private void handleStdout(@NotNull String text) {
    if (myWebServerPort == -1) {
      myWebServerPort = parseWebServerPort(text);
    }
    if (myRunnerPort == -1) {
      myRunnerPort = parseRunnerPort(text);
    }
    if (myWebServerPort != -1 && myRunnerPort != -1) {
      myServer.fireOnReady(myWebServerPort, myRunnerPort);
    }
  }

  private static int parseWebServerPort(@NotNull String text) {
    Matcher m = WEB_SERVER_LINE_PATTERN.matcher(text);
    if (m.find()) {
      String karmaVersionStr = m.group(1);
      SemVer semVer = SemVer.parseFromText(karmaVersionStr);
      if (semVer == null) {
        LOG.warn("Can't parse sem ver from '" + karmaVersionStr + "'");
        return -1;
      }
      String portStr = m.group(2);
      try {
        return Integer.parseInt(portStr);
      }
      catch (NumberFormatException e) {
        LOG.warn("Can't parse web server port from '" + text + "'");
      }
    }
    return -1;
  }

  private static int parseRunnerPort(@NotNull String text) {
    String prefix = "INFO [karma]: To run via this server, use \"karma run --runner-port ";
    String suffix = "\"";
    if (text.startsWith(prefix) && text.endsWith(suffix)) {
      String str = text.substring(prefix.length(), text.length() - suffix.length());
      try {
        return Integer.parseInt(str);
      }
      catch (NumberFormatException e) {
        LOG.warn("Can't parse runner port from '" + text + "'");
      }
    }
    return -1;
  }

  private void handleBrowsersChange(@NotNull String eventType,
                                    @NotNull String browserId,
                                    @NotNull String browserName) {
    if (BROWSER_CONNECTED_EVENT_TYPE.equals(eventType)) {
      myCapturedBrowsers.put(browserId, browserName);
      myServer.onBrowserCaptured();
    }
    else {
      myCapturedBrowsers.remove(browserId);
    }
  }

  public boolean hasCapturedBrowser() {
    return !myCapturedBrowsers.isEmpty();
  }

  @NotNull
  public Set<String> getCapturedBrowsers() {
    return myCapturedBrowsers.keySet();
  }

  public int getWebServerPort() {
    return myWebServerPort;
  }

  public int getRunnerPort() {
    return myRunnerPort;
  }

  @Nullable
  private static String getStringProperty(@NotNull JsonObject jsonObject, @NotNull String propertyName) {
    JsonElement jsonElement = jsonObject.get(propertyName);
    if (jsonElement != null) {
      if (jsonElement.isJsonPrimitive()) {
        JsonPrimitive primitive = jsonElement.getAsJsonPrimitive();
        if (primitive.isString()) {
          return primitive.getAsString();
        }
      }
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
        String id = getStringProperty(event, "id");
        String name = getStringProperty(event, "name");
        if (id != null && name != null) {
          handleBrowsersChange(myEventType, id, name);
        }
        else {
          LOG.warn("Illegal browser event. Type: " + myEventType + ", body: " + eventBody.toString());
        }
      }
    }
  }
}
