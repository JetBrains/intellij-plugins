/*
 * Copyright (c) 2024, the Dart project authors.
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

import com.google.dart.server.DartLspTextDocumentContentConsumer;
import com.google.gson.JsonObject;

import org.dartlang.analysis.server.protocol.RequestError;

/**
 * Instances of {@code DartLspTextDocumentContentProcessor} translate JSON result objects for a given
 * {@link LSPDartTextDocumentContentConsumer}.
 *
 * @coverage dart.server.remote
 */
public class DartLspTextDocumentContentProcessor extends ResultProcessor {

  private final DartLspTextDocumentContentConsumer consumer;

  public DartLspTextDocumentContentProcessor(DartLspTextDocumentContentConsumer consumer) {
    this.consumer = consumer;
  }

  public void process(JsonObject resultObject, RequestError requestError) {
    if (resultObject != null) {
      // Example: {"lspResponse":{"id":"1","jsonrpc":"2.0","result":{"content":"file contents"}}}
      JsonObject lspResponse = resultObject.getAsJsonObject("lspResponse");
      JsonObject innerResultObject = lspResponse.getAsJsonObject("result");
      final String contents = innerResultObject.get("content").getAsString();
      consumer.computedDocumentContents(contents);
    }
    if (requestError != null) {
      consumer.onError(requestError);
    }
  }
}