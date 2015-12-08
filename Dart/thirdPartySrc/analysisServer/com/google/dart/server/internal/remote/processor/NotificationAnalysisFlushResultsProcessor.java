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
import com.google.dart.server.utilities.general.JsonUtilities;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * Processor for "analysis.flushResults" notification.
 * 
 * @coverage dart.server.remote
 */
public class NotificationAnalysisFlushResultsProcessor extends NotificationProcessor {

  public NotificationAnalysisFlushResultsProcessor(AnalysisServerListener listener) {
    super(listener);
  }

  /**
   * Process the given {@link JsonObject} notification and notify {@link #listener}.
   */
  @Override
  public void process(JsonObject response) throws Exception {
    JsonObject paramsObject = response.get("params").getAsJsonObject();
    List<String> files = JsonUtilities.decodeStringList(paramsObject.get("files").getAsJsonArray());
    // notify listener
    getListener().flushedResults(files);
  }
}
