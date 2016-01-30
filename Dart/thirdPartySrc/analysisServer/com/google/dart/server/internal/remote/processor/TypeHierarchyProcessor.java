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

import com.google.dart.server.GetTypeHierarchyConsumer;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.dartlang.analysis.server.protocol.RequestError;
import org.dartlang.analysis.server.protocol.TypeHierarchyItem;

/**
 * Instances of {@code TypeHierarchyResultProcessor} translate JSON result objects for a given
 * {@link GetTypeHierarchyConsumer}.
 * 
 * @coverage dart.server.remote
 */
public class TypeHierarchyProcessor extends ResultProcessor {

  private final GetTypeHierarchyConsumer consumer;

  public TypeHierarchyProcessor(GetTypeHierarchyConsumer consumer) {
    this.consumer = consumer;
  }

  public void process(JsonObject resultObject, RequestError requestError) {
    if (resultObject != null) {
      try {
        JsonArray typeHierarchyItemsArray = resultObject.get("hierarchyItems") != null
            ? resultObject.get("hierarchyItems").getAsJsonArray() : null;
        // construct type hierarchy item list and notify listener
        consumer.computedHierarchy(TypeHierarchyItem.fromJsonArray(typeHierarchyItemsArray));
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
