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

import com.google.dart.server.GetAssistsConsumer;
import com.google.gson.JsonObject;

import org.dartlang.analysis.server.protocol.RequestError;
import org.dartlang.analysis.server.protocol.SourceChange;

import java.util.List;

/**
 * Instances of {@code AssistsProcessor} translate JSON result objects for a given
 * {@link GetAssistsConsumer}.
 * 
 * @coverage dart.server.remote
 */
public class AssistsProcessor extends ResultProcessor {

  private final GetAssistsConsumer consumer;

  public AssistsProcessor(GetAssistsConsumer consumer) {
    this.consumer = consumer;
  }

  public void process(JsonObject resultObject, RequestError requestError) {
    if (resultObject != null) {
      try {
        List<SourceChange> sourceChanges = SourceChange.fromJsonArray(resultObject.get("assists").getAsJsonArray());
        consumer.computedSourceChanges(sourceChanges);
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
