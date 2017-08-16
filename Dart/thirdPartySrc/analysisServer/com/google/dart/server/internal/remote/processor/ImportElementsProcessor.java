package com.google.dart.server.internal.remote.processor;

import com.google.dart.server.ImportElementsConsumer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.dartlang.analysis.server.protocol.RequestError;
import org.dartlang.analysis.server.protocol.SourceFileEdit;

public class ImportElementsProcessor extends ResultProcessor {
  private final ImportElementsConsumer consumer;

  public ImportElementsProcessor(ImportElementsConsumer consumer) {
    this.consumer = consumer;
  }

  public void process(JsonObject resultObject, RequestError requestError) {
    if (resultObject != null) {
      try {
        final JsonElement element = resultObject.get("edit");
        JsonObject editObject = element == null ? null : element.getAsJsonObject();
        SourceFileEdit fileEdit = editObject == null ? null : SourceFileEdit.fromJson(editObject);
        consumer.computedImportedElements(fileEdit);
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
