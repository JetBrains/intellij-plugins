// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.google.dart.server.internal.remote.processor;

import com.google.common.collect.ImmutableMap;
import com.google.dart.server.AnalysisServerListener;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.*;

public class NotificationCompletionExistingImportsProcessor extends NotificationProcessor {
  public NotificationCompletionExistingImportsProcessor(AnalysisServerListener listener) { super(listener); }

  /**
   * Process the given {@link JsonObject} notification and notify {@link #listener}.
   */
  @Override
  public void process(JsonObject response) throws Exception {
    JsonObject paramsObject = response.get("params").getAsJsonObject();
    String file = paramsObject.get("file").getAsString();
    JsonObject existingImports = paramsObject.get("imports").getAsJsonObject();  // ExistingImports
    Map<String, Set<String>> uriToNames = buildUriNamesMap(existingImports);
    getListener().computedExistingImports(file, uriToNames);
  }

  private Map<String, Set<String>> buildUriNamesMap(JsonObject existingImports) {
    JsonObject elements = existingImports.get("elements").getAsJsonObject();  // ImportedElementSet
    List<String> strings = new ArrayList<>();
    elements.getAsJsonArray("strings").forEach(item -> strings.add(item.getAsString()));
    List<Integer> uris = new ArrayList<>();
    elements.getAsJsonArray("uris").forEach(item -> uris.add(item.getAsInt()));
    List<Integer> names = new ArrayList<>();
    elements.getAsJsonArray("names").forEach(item -> names.add(item.getAsInt()));
    Map<String, Set<String>> uriToNames = new HashMap<>();
    existingImports.get("imports").getAsJsonArray().forEach(item -> {
      JsonObject existingImport = item.getAsJsonObject();  // ExistingImport
      existingImport.get("elements").getAsJsonArray().forEach(element -> {
        int index = element.getAsInt();
        String uri = strings.get(uris.get(index));
        if (!uriToNames.containsKey(uri)) {
          uriToNames.put(uri, new HashSet<>());
        }

        String name = strings.get(names.get(index));
        uriToNames.get(uri).add(name);
      });
    });

    return uriToNames;
  }
}
