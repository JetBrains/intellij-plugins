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

import com.google.dart.server.AnalysisServerListener;
import com.google.gson.JsonObject;

/**
 * Abstract processor class which holds the {@link AnalysisServerListener} for all processors.
 * 
 * @coverage dart.server.remote
 */
public abstract class NotificationProcessor extends JsonProcessor {
  private final AnalysisServerListener listener;

  public NotificationProcessor(AnalysisServerListener listener) {
    this.listener = listener;
  }

  /**
   * Process the given {@link JsonObject} notification and notify {@link #listener}.
   */
  public abstract void process(JsonObject response) throws Exception;

  protected AnalysisServerListener getListener() {
    return listener;
  }
}
