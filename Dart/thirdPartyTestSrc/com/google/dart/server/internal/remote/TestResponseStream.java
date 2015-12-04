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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.LinkedList;

/**
 * A test implementation of {@link ResponseStream}.
 */
public class TestResponseStream implements ResponseStream {
  private final Object lock = new Object();
  private final LinkedList<JsonObject> responses = Lists.newLinkedList();
  private boolean lastRequestProcessed;
  private boolean done = false;

  public void done() {
    synchronized (lock) {
      done = true;
      lock.notifyAll();
    }
  }

  @Override
  public void lastRequestProcessed() {
    lastRequestProcessed = true;
  }

  /**
   * Puts the given response into the queue.
   */
  public void put(JsonObject response) throws Exception {
    synchronized (lock) {
      responses.addLast(response);
      lock.notifyAll();
    }
  }

  /**
   * Puts the given response into the queue.
   */
  public void put(String... lines) throws Exception {
    String json = Joiner.on('\n').join(lines);
    json = json.replace('\'', '"');
    JsonObject response = (JsonObject) new JsonParser().parse(json);
    put(response);
  }

  @Override
  public JsonObject take() {
    synchronized (lock) {
      while (!done) {
        if (!responses.isEmpty()) {
          lastRequestProcessed = false;
          return responses.removeFirst();
        }
        try {
          lock.wait();
        } catch (InterruptedException e) {
          continue;
        }
      }
    }
    return null;
  }

  public void waitForEmpty() {
    while (!isEmpty()) {
      Thread.yield();
    }
  }

  private boolean isEmpty() {
    synchronized (lock) {
      return lastRequestProcessed && responses.isEmpty();
    }
  }
}
