package com.jetbrains.lang.dart.ide.runner.server.connection;

import com.google.gson.JsonObject;
import com.intellij.util.io.socketConnection.AbstractResponse;

public class JsonResponse implements AbstractResponse {
  private JsonObject myJsonObject;

  public JsonResponse(JsonObject object) {
    myJsonObject = object;
  }

  public JsonObject getJsonObject() {
    return myJsonObject;
  }
}
