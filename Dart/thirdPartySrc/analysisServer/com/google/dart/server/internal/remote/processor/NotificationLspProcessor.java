package com.google.dart.server.internal.remote.processor;

import com.google.dart.server.AnalysisServerListener;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

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
    // {
    //   "event"::"lsp.notification",
    //   "params":: {
    //     "lspNotification":: {
    //       "jsonrpc"::"2.0",
    //       "method"::"dart/textDocumentContentDidChange",
    //       "params":: {
    //         "uri"::"dart-macro+file::///some/path/lib/test.dart"
    //       }
    //     }
    //   }
    // }
    JsonObject paramsObject = response.getAsJsonObject("params");
    if (paramsObject == null) {
      return;
    }

    JsonObject lspNotificationObject = paramsObject.getAsJsonObject("lspNotification");
    if (lspNotificationObject == null) {
      return;
    }

    JsonElement methodElement = lspNotificationObject.get("method");
    if (methodElement instanceof JsonPrimitive && methodElement.getAsString().equals("dart/textDocumentContentDidChange")) {
      JsonObject innerParamsObject = lspNotificationObject.getAsJsonObject("params");
      if (innerParamsObject != null) {
        JsonElement uriElement = innerParamsObject.get("uri");
        if (uriElement instanceof JsonPrimitive) {
          getListener().lspTextDocumentContentDidChange(uriElement.getAsString());
        }
      }
    }
  }
}
