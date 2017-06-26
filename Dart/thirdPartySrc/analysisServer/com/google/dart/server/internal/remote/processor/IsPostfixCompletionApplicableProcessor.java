package com.google.dart.server.internal.remote.processor;

import com.google.dart.server.IsPostfixCompletionApplicableConsumer;
import com.google.gson.JsonObject;
import org.dartlang.analysis.server.protocol.RequestError;

public class IsPostfixCompletionApplicableProcessor {
  private final IsPostfixCompletionApplicableConsumer consumer;

  public IsPostfixCompletionApplicableProcessor(IsPostfixCompletionApplicableConsumer consumer) {
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
