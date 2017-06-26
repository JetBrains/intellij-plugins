package com.google.dart.server.internal.remote.processor;

import com.google.dart.server.ListPostfixCompletionTemplatesConsumer;
import com.google.dart.server.utilities.general.JsonUtilities;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import org.dartlang.analysis.server.protocol.PostfixCompletionTemplate;
import org.dartlang.analysis.server.protocol.RequestError;
import org.jetbrains.io.JsonUtil;

import java.lang.reflect.Type;

public class ListPostfixCompletionTemplatesProcessor extends ResultProcessor {
  private final ListPostfixCompletionTemplatesConsumer consumer;

  public ListPostfixCompletionTemplatesProcessor(ListPostfixCompletionTemplatesConsumer consumer) {
    this.consumer = consumer;
  }

  public void process(JsonObject resultObject, RequestError requestError) {
    if (resultObject != null) {
      try {
        String jsonString = resultObject.get("templates").getAsJsonPrimitive().getAsString();
        String[][] stringArray = new Gson().fromJson(jsonString, (Type)String[][].class);
        PostfixCompletionTemplate[] templates = PostfixCompletionTemplate.fromStringArray(stringArray);
        consumer.postfixCompletionTemplates(templates);
      }
      catch (Exception exception) {
        // catch any exceptions in the formatting of this response
        requestError = generateRequestError(exception);
      }
    }
  }
}
