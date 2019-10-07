// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.runner.server.webdev;

import com.google.gson.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Util methods for parsing JSON out of the webdev daemon protocol, typically of the form:
 * <code>[{"event":"event-name","params":{"some-key":"some-value"}}]</code>
 * <p/>
 * TODO(jwren) This class currently has two methods which could be refactored and cleaned up to share some code.
 */
public class DartDaemonParserUtil {

  /**
   * Given the json log:
   * <p>
   * [{"event":"app.debugPort","params":{"appId":"Dii/FZobJecU539Uw/cwpg==","port":36063,"wsUri":"ws://127.0.0.1:36063/a-code"}}]
   * <p>
   * return the {@link String} <code>ws://127.0.0.1:36063/a-code</code>
   */
  @Nullable
  public static String getWsUri(@NotNull final String text) throws Exception {
    if (text.isEmpty() || !text.startsWith("[{")) {
      return null;
    }
    final JsonObject obj;
    final JsonParser jp = new JsonParser();
    final JsonElement elem = jp.parse(text);
    obj = elem.getAsJsonArray().get(0).getAsJsonObject();

    final JsonPrimitive primEvent = obj.getAsJsonPrimitive("event");
    if (primEvent == null) {
      throw new Exception("Parse JSON from daemon did not have an \"event\" key: " + text);
    }

    final String eventName = primEvent.getAsString();
    if (eventName == null) {
      throw new Exception("Parse JSON from daemon did not have an \"event\" value: " + text);
    }

    final JsonObject params = obj.getAsJsonObject("params");
    if (params == null) {
      throw new Exception("Parse JSON from daemon did not have a \"params\" value: " + text);
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
  @Nullable
  public static String getLogMessage(@NotNull final String text) {
    if (text.isEmpty() || !text.startsWith("[{")) {
      return null;
    }
    final JsonObject obj;
    try {
      final JsonParser jp = new JsonParser();
      final JsonElement elem = jp.parse(text);
      obj = elem.getAsJsonArray().get(0).getAsJsonObject();
    }
    catch (JsonSyntaxException e) {
      return null;
    }

    final JsonPrimitive primEvent = obj.getAsJsonPrimitive("event");
    if (primEvent == null) {
      return null;
    }

    final String eventName = primEvent.getAsString();
    if (eventName == null || (!eventName.equals("daemon.log") && !eventName.equals("app.log"))) {
      return null;
    }

    final JsonObject params = obj.getAsJsonObject("params");
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
