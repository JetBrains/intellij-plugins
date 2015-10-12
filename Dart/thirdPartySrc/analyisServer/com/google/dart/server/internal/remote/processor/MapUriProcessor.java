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

import com.google.dart.server.MapUriConsumer;
import com.google.gson.JsonObject;

import org.dartlang.analysis.server.protocol.RequestError;

/**
 * Instances of the class {@code MapUriProcessor} process the result of an {@code execution.mapUri}
 * request.
 */
public class MapUriProcessor extends ResultProcessor {
  /**
   * The consumer that will be notified when a result is processed.
   */
  private MapUriConsumer consumer;

  /**
   * Initialize a newly create result processor to process the result of an {@code execution.mapUri}
   * request.
   * 
   * @param consumer the consumer that will be notified when a result is processed
   */
  public MapUriProcessor(MapUriConsumer consumer) {
    this.consumer = consumer;
  }

  public void process(JsonObject resultObject, RequestError requestError) {
    if (resultObject != null) {
      try {
        String file = safelyGetAsString(resultObject, "file");
        String uri = safelyGetAsString(resultObject, "uri");
        consumer.computedFileOrUri(file, uri);
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
