package com.intellij.javascript.karma.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.javascript.karma.KarmaConfig;
import com.intellij.javascript.karma.util.GsonUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.intellij.util.containers.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
* @author Sergey Simonchik
*/
public class KarmaServerState {

  private static final Logger LOG = Logger.getInstance(KarmaServerState.class);
  private static final String BROWSER_CONNECTED_EVENT_TYPE = "browserConnected";
  private static final String BROWSER_DISCONNECTED_EVENT_TYPE = "browserDisconnected";

  private final KarmaServer myServer;
  private final ConcurrentMap<String, String> myCapturedBrowsers = new ConcurrentHashMap<String, String>();
  private volatile int myServerPort = -1;
  private volatile KarmaConfig myConfig;

  public KarmaServerState(@NotNull KarmaServer server, @NotNull ProcessHandler serverProcessHandler) {
    myServer = server;
    myServer.registerStreamEventHandler(new BrowserEventHandler(BROWSER_CONNECTED_EVENT_TYPE));
    myServer.registerStreamEventHandler(new BrowserEventHandler(BROWSER_DISCONNECTED_EVENT_TYPE));
    myServer.registerStreamEventHandler(new ConfigHandler());
    new ServerProcessListener(this, serverProcessHandler);
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
  public Collection<String> getCapturedBrowsers() {
    return myCapturedBrowsers.values();
  }

  public int getServerPort() {
    return myServerPort;
  }

  private void onServerPortBound(int serverPort) {
    myServerPort = serverPort;
    myServer.fireOnReady(serverPort);
  }

  @Nullable
  public KarmaConfig getKarmaConfig() {
    return myConfig;
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
        String id = GsonUtil.getStringProperty(event, "id");
        String name = GsonUtil.getStringProperty(event, "name");
        if (id != null && name != null) {
          handleBrowsersChange(myEventType, id, name);
        }
        else {
          LOG.warn("Illegal browser event. Type: " + myEventType + ", body: " + eventBody.toString());
        }
      }
    }
  }

  private class ConfigHandler implements StreamEventHandler {

    @NotNull
    @Override
    public String getEventType() {
      return "configFile";
    }

    @Override
    public void handle(@NotNull JsonElement eventBody) {
      myConfig = KarmaConfig.parseFromJson(eventBody);
    }
  }

  private static class ServerProcessListener implements ProcessListener {
    private static final Pattern SERVER_PORT_LINE_PATTERN = Pattern.compile("^INFO \\[.*\\]: Karma.+server started at http://[^:]+:(\\d+)/.*$");

    private final StringBuilder myBuffer = new StringBuilder();
    private final KarmaServerState myState;
    private final ProcessHandler myHandler;

    public ServerProcessListener(@NotNull KarmaServerState state, @NotNull ProcessHandler handler) {
      myState = state;
      myHandler = handler;
      handler.addProcessListener(this);
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
      if (outputType != ProcessOutputTypes.SYSTEM && outputType != ProcessOutputTypes.STDERR) {
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
      int serverPort = myState.myServerPort;
      if (serverPort == -1) {
        serverPort = parseServerPort(text);
        if (serverPort != -1) {
          myHandler.removeProcessListener(this);
          myState.onServerPortBound(serverPort);
        }
      }
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

  }

}
