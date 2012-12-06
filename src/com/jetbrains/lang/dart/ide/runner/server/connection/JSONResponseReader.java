package com.jetbrains.lang.dart.ide.runner.server.connection;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.ObjectUtils;
import com.intellij.util.io.socketConnection.ResponseReader;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;

public class JSONResponseReader implements ResponseReader<JsonResponse> {
  private static final Logger LOG = Logger.getInstance("#com.jetbrains.lang.dart.ide.runner.debugger.connection.JSONResponseReader");
  private final BufferedReader myReader;
  private final StringBuilder myResponseBuilder = new StringBuilder();

  public JSONResponseReader(BufferedReader reader) {
    myReader = reader;
  }

  @Override
  public JsonResponse readResponse() throws IOException, InterruptedException {
    int c;
    while ((c = myReader.read()) != -1) {
      myResponseBuilder.append((char)c);
      if (c == '}') {
        @NonNls String responseString = myResponseBuilder.toString();
        if (LOG.isDebugEnabled() && !responseString.startsWith("-1 logMessage ")) {
          LOG.debug("response check: " + responseString);
        }
        final JsonObject JsonObject = parseResponse(responseString);
        if (JsonObject != null) {
          LOG.debug("read  " + responseString);
          myResponseBuilder.setLength(0);
          if (JsonObject.get("id") != null) {
            return new JsonResponseToRequest(JsonObject);
          }
          return new JsonResponse(JsonObject);
        }
      }
    }
    return null;
  }

  @Nullable
  private static JsonObject parseResponse(String jsonContent) {
    final JsonElement jsonElement;
    try {
      final JsonParser jsonParser = new JsonParser();
      jsonElement = jsonParser.parse(jsonContent);
    }
    catch (Exception e) {
      return null;
    }
    final JsonObject rootObj = ObjectUtils.tryCast(jsonElement, JsonObject.class);
    return rootObj == null ? null : rootObj;
  }
}
