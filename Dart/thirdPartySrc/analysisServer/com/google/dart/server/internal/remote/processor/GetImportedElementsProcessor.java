package com.google.dart.server.internal.remote.processor;

import com.google.dart.server.GetImportedElementsConsumer;
import com.google.gson.JsonObject;
import org.dartlang.analysis.server.protocol.ImportedElements;
import org.dartlang.analysis.server.protocol.RequestError;

import java.util.List;

public class GetImportedElementsProcessor extends ResultProcessor {
  private final GetImportedElementsConsumer consumer;

  public GetImportedElementsProcessor(GetImportedElementsConsumer consumer) {
    this.consumer = consumer;
  }

  public void process(JsonObject resultObject, RequestError requestError) {
    if (resultObject != null) {
      try {
        List<ImportedElements> elements = ImportedElements.fromJsonArray(resultObject.getAsJsonArray("elements"));

        // notify consumer
        consumer.computedImportedElements(elements);
      }
      catch (Exception exception) {
        // catch any exceptions in the formatting of this response
        requestError = generateRequestError(exception);
      }
    }
    if (requestError != null) {
      consumer.onError(requestError);
    }
  }
}
