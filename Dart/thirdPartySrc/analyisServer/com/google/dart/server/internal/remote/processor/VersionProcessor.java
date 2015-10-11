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

import com.google.dart.server.GetVersionConsumer;
import com.google.gson.JsonObject;

import org.dartlang.analysis.server.protocol.RequestError;

/**
 * Instances of {@code VersionProcessor} translate JSON result objects for a given
 * {@link GetVersionConsumer}.
 * 
 * @coverage dart.server.remote
 */
public class VersionProcessor extends ResultProcessor {

  private final GetVersionConsumer consumer;

  public VersionProcessor(GetVersionConsumer consumer) {
    this.consumer = consumer;
  }

  public void process(JsonObject resultObject, RequestError requestError) {
    if (resultObject != null) {
      consumer.computedVersion(resultObject.get("version").getAsString());
    }
    if (requestError != null) {
      consumer.onError(requestError);
    }
  }
}
