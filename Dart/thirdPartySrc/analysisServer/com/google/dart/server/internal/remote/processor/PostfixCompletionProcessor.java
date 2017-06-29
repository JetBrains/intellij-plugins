package com.google.dart.server.internal.remote.processor;

import com.google.dart.server.GetPostfixCompletionConsumer;
import com.google.gson.JsonObject;
import org.dartlang.analysis.server.protocol.RequestError;
import org.dartlang.analysis.server.protocol.SourceChange;

public class PostfixCompletionProcessor extends ResultProcessor {
  private final GetPostfixCompletionConsumer consumer;

  public PostfixCompletionProcessor(GetPostfixCompletionConsumer consumer) {
    this.consumer = consumer;
  }

  public void process(JsonObject resultObject, RequestError requestError) {
    if (resultObject != null) {
      try {
        SourceChange sourceChange = SourceChange.fromJson(resultObject.get("change").getAsJsonObject());
        consumer.computedSourceChange(sourceChange);
      }
      catch (Exception exception) {
        // catch any exceptions in the formatting of this response
        requestError = generateRequestError(exception);
      }
    }
  }
}
