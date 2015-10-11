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
package com.google.dart.server.internal.remote.processor;

import com.google.common.reflect.TypeToken;
import com.google.dart.server.GetLibraryDependenciesConsumer;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.dartlang.analysis.server.protocol.RequestError;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Instances of {@code LibraryDependenciesProcessor} translate JSON result objects for a given
 * {@link GetLibraryDependenciesConsumer}.
 * 
 * @coverage dart.server.remote
 */
public class LibraryDependenciesProcessor extends ResultProcessor {

  private static final Map<String, Map<String, List<String>>> EMPTY_MAP = Collections.<String, Map<String, List<String>>> emptyMap();

  private static Map<String, Map<String, List<String>>> constructPackageMap(JsonElement jsonMap) {
    if (jsonMap == null) {
      return EMPTY_MAP;
    }
    return new Gson().fromJson(jsonMap, new TypeToken<Map<String, Map<String, List<String>>>>() {
    }.getType());
  }

  private final GetLibraryDependenciesConsumer consumer;

  public LibraryDependenciesProcessor(GetLibraryDependenciesConsumer consumer) {
    this.consumer = consumer;
  }

  public void process(JsonObject resultObject, RequestError requestError) {
    if (resultObject != null) {
      try {
        String[] libraries = constructStringArray(resultObject.get("libraries").getAsJsonArray());
        Map<String, Map<String, List<String>>> packageMap = constructPackageMap(resultObject.get("packageMap"));
        consumer.computedDependencies(libraries, packageMap);
      } catch (Exception exception) {
        // catch any exceptions in the formatting of this response
        requestError = generateRequestError(exception);
      }
    }
    if (requestError != null) {
      consumer.onError(requestError);
    }
  }

}
