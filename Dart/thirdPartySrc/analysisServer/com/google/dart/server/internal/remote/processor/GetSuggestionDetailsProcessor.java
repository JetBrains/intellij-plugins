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

import com.google.dart.server.GetSuggestionDetailsConsumer;
import com.google.dart.server.GetSuggestionsConsumer;
import com.google.gson.JsonObject;

import org.dartlang.analysis.server.protocol.GetCompletionDetailsResult;
import org.dartlang.analysis.server.protocol.RequestError;
import org.dartlang.analysis.server.protocol.SourceChange;

/**
 * Instances of {@code CompletionIdProcessor} translate JSON result objects for a given
 * {@link GetSuggestionsConsumer}.
 * 
 * @coverage dart.server.remote
 */
public class GetSuggestionDetailsProcessor extends ResultProcessor {

  private final GetSuggestionDetailsConsumer consumer;

  public GetSuggestionDetailsProcessor(GetSuggestionDetailsConsumer consumer) {
    this.consumer = consumer;
  }

  public void process(JsonObject resultObject, RequestError requestError) {
    if (requestError != null) {
      consumer.onError(requestError);
      return;
    }

    try {
      String completion = resultObject.get("completion").getAsString();
      SourceChange change = SourceChange.fromJson(resultObject.get("change").getAsJsonObject());
      consumer.computedDetails(new GetCompletionDetailsResult(completion, change));
    } catch (Exception exception) {
      // Catch any exceptions in the formatting of this response.
      consumer.onError(generateRequestError(exception));
    }
  }
}
