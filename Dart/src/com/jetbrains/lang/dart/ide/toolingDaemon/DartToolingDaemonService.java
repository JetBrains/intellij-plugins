// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.toolingDaemon;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.ColoredProcessHandler;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.lang.dart.sdk.DartSdk;
import de.roderick.weberknecht.WebSocketEventHandler;
import de.roderick.weberknecht.WebSocketException;
import de.roderick.weberknecht.WebSocketMessage;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.URISyntaxException;
import de.roderick.weberknecht.WebSocket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class DartToolingDaemonService implements Disposable {
  public static final String MIN_SDK_VERSION = "3.4";
  private static final String STARTUP_MESSAGE_PREFIX = "The Dart Tooling Daemon is listening on ";
  private final Project myProject;
  private WebSocket myWebSocket;
  private AtomicInteger nextRequestId = new AtomicInteger();

  private final Map<String, ToolingDaemonConsumer> consumerMap = Maps.newHashMap();

  private final List<ToolingDaemonListener> listeners = new ArrayList<>();

  public DartToolingDaemonService(Project project) { myProject = project; }

  public void startService() throws ExecutionException {
    final DartSdk sdk = DartSdk.getDartSdk(myProject);
    if (sdk == null || !isDartSdkVersionSufficient(sdk)) {
      return;
    }
    final GeneralCommandLine result = new GeneralCommandLine().withWorkDirectory(sdk.getHomePath());
    result.setCharset(StandardCharsets.UTF_8);
    result.setExePath(FileUtil.toSystemDependentName("dart"));
    result.addParameter("tooling-daemon");

    final ColoredProcessHandler handler = new ColoredProcessHandler(result);
    handler.addProcessListener(new ProcessListener() {
      @Override
      public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
        final String text = event.getText().trim();
        if (text.startsWith(STARTUP_MESSAGE_PREFIX)) {
          final String address = text.split(STARTUP_MESSAGE_PREFIX)[1];
          try {
            myWebSocket = new WebSocket(new URI("ws://" + address + "/ws"));
            myWebSocket.setEventHandler(new WebSocketEventHandler() {
              @Override
              public void onClose() {

              }

              @Override
              public void onMessage(WebSocketMessage message) {
                JsonObject json;
                try {
                  json = (JsonObject)JsonParser.parseString(message.getText());
                } catch (Exception e) {
                  System.out.println("Parse message failed: " + message.getText() + e);
                  return;
                }
                String method = json.get("method").getAsString();
                if (method.equals("streamNotify")) {
                  JsonObject params = json.get("params").getAsJsonObject();
                  for (ToolingDaemonListener listener : new ArrayList<>(listeners)) {
                    listener.received(params.get("streamId").getAsString(), json);
                  }
                }

                String id = json.get("id").getAsString();
                ToolingDaemonConsumer consumer = consumerMap.remove(id);
                if (consumer != null) {
                  consumer.received(json);
                }
              }

              @Override
              public void onOpen() {
                // Fake request to make sure the tooling daemon works
                JsonObject params = new JsonObject();
                params.addProperty("streamId", "foo_stream");
                try {
                  sendRequest("streamListen", params, new ToolingDaemonConsumer() {
                    @Override
                    public void received(JsonObject response) {
                      System.out.println("received response from streamListen");
                      System.out.println(response);
                    }
                  });
                }
                catch (WebSocketException e) {
                  throw new RuntimeException(e);
                }
              }

              @Override
              public void onPing() {

              }

              @Override
              public void onPong() {

              }
            });
            myWebSocket.connect();
          }
          catch (WebSocketException e) {
            throw new RuntimeException(e);
          }
          catch (URISyntaxException e) {
            throw new RuntimeException(e);
          }

        }
      }
    });

    handler.startNotify();
  }

  public void sendRequest(String method, JsonObject params, ToolingDaemonConsumer consumer) throws WebSocketException {
    JsonObject request = new JsonObject();
    request.addProperty("jsonrpc", "2.0");
    request.addProperty("method", method);

    final String id = Integer.toString(nextRequestId.incrementAndGet());
    request.addProperty("id", id);
    request.add("params", params);

    consumerMap.put(id, consumer);
    myWebSocket.send(request.toString());
  }

  public void addToolingDaemonListener(ToolingDaemonListener listener) {
    listeners.add(listener);
  }

  public void removeToolingDaemonListener(ToolingDaemonListener listener) {
    listeners.remove(listener);
  }

  @NotNull
  public static DartToolingDaemonService getInstance(@NotNull final Project project) {
    return project.getService(DartToolingDaemonService.class);
  }

  @Override
  public void dispose() {

  }

  public static boolean isDartSdkVersionSufficient(@NotNull final DartSdk sdk) {
    return StringUtil.compareVersionNumbers(sdk.getVersion(), MIN_SDK_VERSION) >= 0;
  }
}
