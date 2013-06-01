package com.jetbrains.lang.dart.ide.runner.server.connection;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.util.io.socketConnection.ResponseToRequest;

public class JsonResponseToRequest extends JsonResponse implements ResponseToRequest {
  public JsonResponseToRequest(JsonObject object) {
    super(object);
  }

  @Override
  public int getRequestId() {
    final JsonElement idElement = getJsonObject().get("id");
    return idElement == null ? -1 : idElement.getAsInt();
  }
}
