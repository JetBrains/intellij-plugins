/*
 * Copyright (c) 2019, the Dart project authors.
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

import com.google.dart.server.ListPostfixCompletionTemplatesConsumer;
import com.google.dart.server.utilities.general.JsonUtilities;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import org.dartlang.analysis.server.protocol.PostfixCompletionTemplate;
import org.dartlang.analysis.server.protocol.RequestError;
import org.jetbrains.io.JsonUtil;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class ListPostfixCompletionTemplatesProcessor extends ResultProcessor {
  private final ListPostfixCompletionTemplatesConsumer consumer;

  public ListPostfixCompletionTemplatesProcessor(ListPostfixCompletionTemplatesConsumer consumer) {
    this.consumer = consumer;
  }

  public void process(JsonObject resultObject, RequestError requestError) {
    if (resultObject != null) {
      try {
        JsonArray items = resultObject.get("templates").getAsJsonArray();
        ArrayList<PostfixCompletionTemplate> templates = new ArrayList<>();
        items.forEach(item -> {
          JsonObject temp = item.getAsJsonObject();
          templates.add(PostfixCompletionTemplate.fromJson(temp));
        });
        consumer.postfixCompletionTemplates(templates.toArray(new PostfixCompletionTemplate[items.size()]));
      }
      catch (Exception exception) {
        // catch any exceptions in the formatting of this response
        requestError = generateRequestError(exception);
      }
    }
    if (requestError != null) {
      consumer.onError(requestError);
    }
  }
}
