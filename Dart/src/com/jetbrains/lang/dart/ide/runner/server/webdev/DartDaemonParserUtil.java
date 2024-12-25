// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.runner.server.webdev;

import com.google.gson.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Util methods for parsing JSON out of the webdev daemon protocol, typically of the form:
 * <code>[{"event":"event-name","params":{"some-key":"some-value"}}]</code>
 */
public final class DartDaemonParserUtil {

  private static @Nullable JsonObject parseDaemonLog(final @NotNull String text) throws JsonSyntaxException {
    if (!text.startsWith("[{")) {
      return null;
    }
    final JsonParser jsonParser = new JsonParser();
    final JsonElement jsonElement = jsonParser.parse(text);
    if (jsonElement instanceof JsonArray && !jsonElement.getAsJsonArray().isEmpty()) {
      return jsonElement.getAsJsonArray().get(0).getAsJsonObject();
    }
    return null;
  }

  /**
   * Given the json log:
   * <p>
   * [{"event":"app.debugPort","params":{"appId":"Dii/FZobJecU539Uw/cwpg==","port":36063,"wsUri":"ws://127.0.0.1:36063/a-code"}}]
   * <p>
   * return the {@link String} <code>ws://127.0.0.1:36063/a-code</code>
   */
  public static @Nullable String getWsUri(final @NotNull String text) throws Exception {
    final JsonObject jsonObject = parseDaemonLog(text);
    if (jsonObject == null) {
      return null;
    }

    final JsonPrimitive primEvent = jsonObject.getAsJsonPrimitive("event");
    if (primEvent == null) {
      throw new Exception("Parse JSON from daemon did not have an \"event\" key: \"" + text + "\"");
    }

    final String eventName = primEvent.getAsString();
    if (eventName == null) {
      throw new Exception("Parse JSON from daemon did not have an \"event\" value: \"" + text + "\"");
    }

    final JsonObject params = jsonObject.getAsJsonObject("params");
    if (params == null) {
      throw new Exception("Parse JSON from daemon did not have a \"params\" value: \"" + text + "\"");
    }

    if (eventName.equals("app.debugPort")) {
      final JsonPrimitive primUri = params.getAsJsonPrimitive("wsUri");
      if (primUri != null) {
        return primUri.getAsString();
      }
    }
    return null;
  }

  /**
   * Given the json logs:
   * <code>
   * [{"event":"daemon.log","params":{"log":"some log text"}}]
   * <p>
   * or
   * <p>
   * [{"event":"app.log","params":{"appId":"foobar","log":"Counter is: 1\n"}}]
   * </code>
   * <p>
   * return the {@link String} <code>some log text</code> or <code>Counter is: 1</code>
   */
  public static @Nullable String getLogMessage(final @NotNull String text) {
    if (!text.startsWith("[{")) {
      return null;
    }
    final JsonObject jsonObject;
    try {
      jsonObject = parseDaemonLog(text);
    }
    catch (JsonSyntaxException e) {
      return null;
    }

    if (jsonObject == null) {
      return null;
    }

    final JsonPrimitive primEvent = jsonObject.getAsJsonPrimitive("event");
    if (primEvent == null) {
      return null;
    }

    final String eventName = primEvent.getAsString();
    if (eventName == null || (!eventName.equals("daemon.log") && !eventName.equals("app.log"))) {
      return null;
    }

    final JsonObject params = jsonObject.getAsJsonObject("params");
    if (params == null) {
      return null;
    }

    final JsonPrimitive primLog = params.getAsJsonPrimitive("log");
    if (primLog != null) {
      String primLogAsString = primLog.getAsString();
      if (primLogAsString != null && !primLogAsString.isEmpty()) {
        return primLogAsString.trim();
      }
    }
    return null;
  }
}
