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

import com.google.dart.server.AnalysisServerListener;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Processor for "lsp.notification" notification.
 * 
 * @coverage dart.server.remote
 */
public class NotificationLspProcessor extends NotificationProcessor {

  public NotificationLspProcessor(AnalysisServerListener listener) {
    super(listener);
  }

  @Override
  public void process(JsonObject response) throws Exception {
    //  Example:
    // {"event"::"lsp.notification","params"::
    //   {"lspNotification"::
    //     {"jsonrpc"::"2.0","method"::"dart/textDocumentContentDidChange","params"::
    //       {"uri"::"dart-macro+file::///some/path/lib/test.dart"}}}}
    JsonObject paramsObject = response.getAsJsonObject("params");
    if(paramsObject == null) {
      return;
    }

    JsonObject lspNotificationObject = paramsObject.getAsJsonObject("lspNotification");
    if(lspNotificationObject == null) {
      return;
    }

    JsonElement methodElement = lspNotificationObject.get("method");
    if (methodElement != null && methodElement.getAsString().equals("dart/textDocumentContentDidChange")) {
      JsonObject innerParamsObject = lspNotificationObject.getAsJsonObject("params");
      if (innerParamsObject != null && innerParamsObject.get("uri").getAsString() != null) {
        getListener().lspTextDocumentContentDidChange(innerParamsObject.get("uri").getAsString());
      }
    }
  }
}
