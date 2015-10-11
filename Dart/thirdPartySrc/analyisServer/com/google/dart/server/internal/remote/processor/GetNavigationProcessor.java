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

import com.google.dart.server.GetNavigationConsumer;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.dartlang.analysis.server.protocol.NavigationRegion;
import org.dartlang.analysis.server.protocol.NavigationTarget;
import org.dartlang.analysis.server.protocol.RequestError;

import java.util.List;

/**
 * Instances of {@code GetNavigationProcessor} translate JSON result objects for a given
 * {@link GetNavigationConsumer}.
 * 
 * @coverage dart.server.remote
 */
public class GetNavigationProcessor extends ResultProcessor {
  private final GetNavigationConsumer consumer;

  public GetNavigationProcessor(GetNavigationConsumer consumer) {
    this.consumer = consumer;
  }

  public void process(JsonObject resultObject, RequestError requestError) {
    if (resultObject != null) {
      try {
        // prepare targets
        String[] targetFiles = constructStringArray(resultObject.get("files").getAsJsonArray());
        List<NavigationTarget> targets = NavigationTarget.fromJsonArray(resultObject.get("targets").getAsJsonArray());
        for (NavigationTarget target : targets) {
          target.lookupFile(targetFiles);
        }
        // prepare regions
        JsonArray regionsArray = resultObject.get("regions").getAsJsonArray();
        List<NavigationRegion> regions = NavigationRegion.fromJsonArray(regionsArray);
        for (NavigationRegion region : regions) {
          region.lookupTargets(targets);
        }
        // notify consumer
        consumer.computedNavigation(regions);
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
