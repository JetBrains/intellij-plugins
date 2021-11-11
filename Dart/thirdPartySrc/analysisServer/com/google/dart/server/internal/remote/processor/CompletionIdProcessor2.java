/*
 * Copyright (c) 2021, the Dart project authors.
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

import com.google.dart.server.GetSuggestionsConsumer;
import com.google.dart.server.GetSuggestionsConsumer2;
import com.google.dart.server.utilities.general.JsonUtilities;
import com.google.gson.JsonObject;

import org.dartlang.analysis.server.protocol.CompletionSuggestion;
import org.dartlang.analysis.server.protocol.RequestError;

import java.util.List;

/**
 * Instances of {@code CompletionIdProcessor2} translate JSON result objects for a given
 * {@link GetSuggestionsConsumer2}.
 *
 * @coverage dart.server.remote
 */
public class CompletionIdProcessor2 extends ResultProcessor {

  private final GetSuggestionsConsumer2 consumer;

  public CompletionIdProcessor2(GetSuggestionsConsumer2 consumer) {
    this.consumer = consumer;
  }

  public void process(JsonObject resultObject, RequestError requestError) {
    if (resultObject != null) {
      int replacementOffset = resultObject.get("replacementOffset").getAsJsonPrimitive().getAsInt();
      int replacementLength = resultObject.get("replacementLength").getAsJsonPrimitive().getAsInt();
      List<CompletionSuggestion> suggestions = CompletionSuggestion.fromJsonArray(resultObject.get("suggestions").getAsJsonArray());
      List<String> libraryUrisToImport = JsonUtilities.decodeStringList(resultObject.get("libraryUrisToImport").getAsJsonArray());
      boolean isIncomplete = resultObject.get("isIncomplete").getAsJsonPrimitive().getAsBoolean();
      consumer.computedSuggestions(replacementOffset, replacementLength, suggestions, libraryUrisToImport, isIncomplete);
    }
    if (requestError != null) {
      consumer.onError(requestError);
    }
  }
}
