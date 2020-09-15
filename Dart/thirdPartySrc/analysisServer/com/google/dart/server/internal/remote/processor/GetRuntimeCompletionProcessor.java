/*
 * Copyright (c) 2018, the Dart project authors.
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

import com.google.dart.server.GetRuntimeCompletionConsumer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.dartlang.analysis.server.protocol.*;

import java.util.List;

/**
 * Instances of {@code GetRuntimeSuggesionsConsumer} translate JSON result objects for a given
 * {@link GetRuntimeCompletionConsumer}.
 * 
 * @coverage dart.server.remote
 */
public class GetRuntimeCompletionProcessor extends ResultProcessor {
  private final GetRuntimeCompletionConsumer consumer;

  public GetRuntimeCompletionProcessor(GetRuntimeCompletionConsumer consumer) {
    this.consumer = consumer;
  }

  public void process(JsonObject resultObject, RequestError requestError) {
    if (resultObject != null) {
      try {
        List<CompletionSuggestion> suggestions = null;
        JsonElement suggestionsJson = resultObject.get("suggestions");
        if (suggestionsJson != null) {
          suggestions = CompletionSuggestion.fromJsonArray(suggestionsJson.getAsJsonArray());
        }

        List<RuntimeCompletionExpression> expressions = null;
        JsonElement expressionsJson = resultObject.get("expressions");
        if (expressionsJson != null) {
          expressions = RuntimeCompletionExpression.fromJsonArray(expressionsJson.getAsJsonArray());
        }

        consumer.computedResult(suggestions, expressions);
      } catch (Exception exception) {
        // catch any exceptions in the formatting of this response
        requestError = generateRequestError(exception);
      }
    }
    if (requestError != null) {
      consumer.onError(requestError);
    }
  }
}
