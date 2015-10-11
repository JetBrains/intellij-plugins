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

import com.google.dart.server.GetFixesConsumer;
import com.google.gson.JsonObject;

import org.dartlang.analysis.server.protocol.AnalysisErrorFixes;
import org.dartlang.analysis.server.protocol.RequestError;

import java.util.List;

/**
 * Instances of {@code FixesProcessor} translate JSON result objects for a given
 * {@link GetFixesConsumer}.
 * 
 * @coverage dart.server.remote
 */
public class FixesProcessor extends ResultProcessor {

  private final GetFixesConsumer consumer;

  public FixesProcessor(GetFixesConsumer consumer) {
    this.consumer = consumer;
  }

  public void process(JsonObject resultObject, RequestError requestError) {
    if (resultObject != null) {
      try {
        List<AnalysisErrorFixes> errorFixesArray = AnalysisErrorFixes.fromJsonArray(resultObject.get(
            "fixes").getAsJsonArray());
        consumer.computedFixes(errorFixesArray);
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
