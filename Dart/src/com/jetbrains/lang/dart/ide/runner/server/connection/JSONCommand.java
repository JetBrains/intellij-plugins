package com.jetbrains.lang.dart.ide.runner.server.connection;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.idea.LoggerFactory;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.io.socketConnection.AbstractRequest;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public class JSONCommand implements AbstractRequest {
  private static final Logger LOG = LoggerFactory.getInstance().getLoggerInstance(JSONCommand.class.getName());
  private final int id;
  private final JsonObject object;

  public JSONCommand(int id, JsonObject object) {
    this.id = id;
    this.object = object;
  }

  @Override
  public int getId() {
    return id;
  }

  public void sendCommand(Writer writer) throws IOException {
    final JsonObject jsonObject = new JsonObject();
    for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
      jsonObject.add(entry.getKey(), entry.getValue());
    }
    jsonObject.addProperty("id", id);
    String message = StringUtil.unescapeSlashes(jsonObject.toString());
    LOG.debug("write " + message);
    writer.write(message);
    writer.flush();
  }
}
