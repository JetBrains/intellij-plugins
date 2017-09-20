package com.google.dart.server.internal.remote.processor;

import com.google.dart.server.AnalysisServerListener;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.dartlang.analysis.server.protocol.ClosingLabel;

/**
 * Processor for "analysis.closingLabels" notification.
 */
public class NotificationAnalysisClosingLabelsProcessor extends NotificationProcessor {
  public NotificationAnalysisClosingLabelsProcessor(AnalysisServerListener listener) {
    super(listener);
  }

  @Override
  public void process(JsonObject response) throws Exception {
    JsonObject paramsObject = response.get("params").getAsJsonObject();
    String file = paramsObject.get("file").getAsString();
    JsonArray labelsJsonArray = paramsObject.get("labels").getAsJsonArray();
    getListener().computedClosingLabels(file, ClosingLabel.fromJsonArray(labelsJsonArray));
  }
}
