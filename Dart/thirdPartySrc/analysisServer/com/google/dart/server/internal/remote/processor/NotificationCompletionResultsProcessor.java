/*
 * Copyright (c) 2014, the Dart project authors.
 * 
 * Licensed under the Eclipse Public License v1.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.dart.server.internal.remote.processor;

import com.google.dart.server.AnalysisServerListener;
import com.google.dart.server.utilities.general.JsonUtilities;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.dartlang.analysis.server.protocol.CompletionSuggestion;
import org.dartlang.analysis.server.protocol.IncludedSuggestionSet;

import java.util.Collections;
import java.util.List;

/**
 * Processor for "completion.results" notification.
 * 
 * @coverage dart.server.remote
 */
public class NotificationCompletionResultsProcessor extends NotificationProcessor {

  public NotificationCompletionResultsProcessor(AnalysisServerListener listener) {
    super(listener);
  }

  /**
   * Process the given {@link JsonObject} notification and notify {@link #listener}.
   */
  @Override
  public void process(JsonObject response) throws Exception {
    JsonObject paramsObject = response.get("params").getAsJsonObject();
    String completionId = paramsObject.get("id").getAsString();
    JsonArray resultsArray = paramsObject.get("results").getAsJsonArray();

    final List<IncludedSuggestionSet> includedSuggestionSets;
    final JsonElement includedSuggestionSetsElement = paramsObject.get("includedSuggestionSets");
    if (includedSuggestionSetsElement != null) {
      final JsonArray includedSuggestionSetsArray = includedSuggestionSetsElement.getAsJsonArray();
      includedSuggestionSets = IncludedSuggestionSet.fromJsonArray(includedSuggestionSetsArray);
    } else {
      includedSuggestionSets = Collections.emptyList();
    }

    final List<String> includedSuggestionKinds;
    final JsonElement includedSuggestionKindsElement = paramsObject.get("includedSuggestionKinds");
    if (includedSuggestionKindsElement != null) {
      final JsonArray includedSuggestionKindsArray = includedSuggestionKindsElement.getAsJsonArray();
      includedSuggestionKinds = JsonUtilities.decodeStringList(includedSuggestionKindsArray);
    } else {
      includedSuggestionKinds = Collections.emptyList();
    }

    int replacementOffset = paramsObject.get("replacementOffset").getAsInt();
    int replacementLength = paramsObject.get("replacementLength").getAsInt();
    boolean isLast = paramsObject.get("isLast").getAsBoolean();
    // compute outline and notify listener
    getListener().computedCompletion(
        completionId,
        replacementOffset,
        replacementLength,
        CompletionSuggestion.fromJsonArray(resultsArray),
        includedSuggestionSets,
        includedSuggestionKinds,
        isLast);
  }
}
