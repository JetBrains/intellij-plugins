/*
 * Copyright (c) 2015, the Dart project authors.
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

import com.google.dart.server.internal.remote.utilities.RequestUtilities;
import com.google.gson.JsonObject;

import java.util.LinkedList;

/**
 * A {@link RequestSink} that enqueues all but "get version" requests and can be later converted
 * into a "passthrough" or an "error" {@link RequestSink}.
 *
 * @coverage dart.server.remote
 */
public class BlockingRequestSink implements RequestSink {
  /**
   * The base {@link RequestSink}
   */
  private final RequestSink base;

  /**
   * A queue of requests.
   */
  private final LinkedList<JsonObject> queue = new LinkedList<>();

  public BlockingRequestSink(RequestSink base) {
    this.base = base;
  }

  @Override
  public void add(JsonObject request) {
    synchronized (queue) {
      if (RequestUtilities.isVersionRequest(request)) {
        base.add(request);
      } else {
        queue.add(request);
      }
    }
  }

  @Override
  public void close() {
    base.close();
  }

  /**
   * Responds with an error to all the currently queued requests and return a {@link RequestSink} to
   * do the same for all the future requests.
   *
   * @param errorResponseSink the sink to send error responses to, not {@code null}
   */
  public RequestSink toErrorSink(ResponseSink errorResponseSink, String errorResponseCode,
      String errorResponseMessage) {
    ErrorRequestSink errorRequestSink = new ErrorRequestSink(
        errorResponseSink,
        errorResponseCode,
        errorResponseMessage);
    synchronized (queue) {
      for (JsonObject request : queue) {
        errorRequestSink.add(request);
      }
    }
    return errorRequestSink;
  }

  /**
   * Returns the passthrough {@link RequestSink}.
   */
  public RequestSink toPassthroughSink() {
    synchronized (queue) {
      for (JsonObject request : queue) {
        base.add(request);
      }
    }
    return base;
  }
}
