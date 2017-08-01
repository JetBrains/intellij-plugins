package com.google.dart.server.internal.remote.processor;

import com.google.dart.server.ImportElementsConsumer;
import com.google.gson.JsonObject;
import org.dartlang.analysis.server.protocol.RequestError;
import org.dartlang.analysis.server.protocol.SourceEdit;

import java.util.List;

public class ImportElementsProcessor extends ResultProcessor {
  private final ImportElementsConsumer consumer;

  public ImportElementsProcessor(ImportElementsConsumer consumer) {
    this.consumer = consumer;
  }

  public void process(JsonObject resultObject, RequestError requestError) {
    if (resultObject != null) {
      try {
        List<SourceEdit> edits = SourceEdit.fromJsonArray(resultObject.getAsJsonArray("edits"));

        // notify consumer
        consumer.computedImportedElements(edits);
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
