package com.google.dart.server.internal.remote.processor;

import com.google.dart.server.EditIsPostfixCompletionApplicableConsumer;
import com.google.gson.JsonObject;
import org.dartlang.analysis.server.protocol.RequestError;

public class EditIsPostfixCompletionApplicableProcessor {
  private final EditIsPostfixCompletionApplicableConsumer consumer;

  public EditIsPostfixCompletionApplicableProcessor(EditIsPostfixCompletionApplicableConsumer consumer) {
    this.consumer = consumer;
  }

  public void process(JsonObject resultObject, RequestError requestError) {
    if (resultObject != null) {
      try {
        boolean value = resultObject.get("value").getAsJsonPrimitive().getAsBoolean();
        consumer.isPostfixCompletionApplicable(value);
      }
      catch (Exception exception) {
        // ignore any exceptions in the formatting of this response
        consumer.isPostfixCompletionApplicable(false);
      }
    }
  }
}
