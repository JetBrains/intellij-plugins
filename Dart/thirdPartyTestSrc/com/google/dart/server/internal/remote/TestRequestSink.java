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

package com.google.dart.server.internal.remote;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * A test implementation of {@link RequestSink}.
 */
public class TestRequestSink implements RequestSink {
  private final List<JsonObject> requests = Lists.newArrayList();
  private boolean isClosed = false;

  @Override
  public void add(JsonObject request) {
    requests.add(request);
  }

  @Override
  public void close() {
    isClosed = true;
  }

  /**
   * Returns recorded requests.
   */
  public List<JsonObject> getRequests() {
    return requests;
  }

  public boolean isClosed() {
    return isClosed;
  }
}
