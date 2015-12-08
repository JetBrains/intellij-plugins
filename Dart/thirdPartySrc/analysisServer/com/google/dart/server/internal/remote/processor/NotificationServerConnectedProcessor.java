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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Processor for "server.connected" notification.
 * 
 * @coverage dart.server.remote
 */
public class NotificationServerConnectedProcessor extends NotificationProcessor {

  public NotificationServerConnectedProcessor(AnalysisServerListener listener) {
    super(listener);
  }

  @Override
  public void process(JsonObject response) throws Exception {
    getListener().serverConnected(getVersion(response));
  }

  private String getVersion(JsonObject response) {
    JsonElement paramsElement = response.get("params");
    if (paramsElement != null) {
      JsonElement versionElement = paramsElement.getAsJsonObject().get("version");
      if (versionElement != null) {
        return versionElement.getAsString();
      }
    }
    return null;
  }
}
