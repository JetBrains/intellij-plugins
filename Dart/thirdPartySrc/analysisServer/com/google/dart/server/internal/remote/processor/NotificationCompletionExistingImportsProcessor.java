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
    Map<String, Map<String, Set<String>>> existingImports = buildUriNamesMap(paramsObject.get("imports").getAsJsonObject());
    getListener().computedExistingImports(file, existingImports);
  }

  private Map<String, Map<String, Set<String>>> buildUriNamesMap(JsonObject existingImports) {
    JsonObject elements = existingImports.get("elements").getAsJsonObject();  // ImportedElementSet
    List<String> strings = new ArrayList<>();
    elements.getAsJsonArray("strings").forEach(item -> strings.add(item.getAsString()));
    List<String> uris = new ArrayList<>();
    elements.getAsJsonArray("uris").forEach(item -> uris.add(strings.get(item.getAsInt())));
    List<String> names = new ArrayList<>();
    elements.getAsJsonArray("names").forEach(item -> names.add(strings.get(item.getAsInt())));
    // Loop through all of the import statements, building a map from imported library
    // onto a map from libraries exported from it onto declared names.
    Map<String, Map<String, Set<String>>> result = new HashMap<>();
    existingImports.get("imports").getAsJsonArray().forEach(item -> {
      JsonObject existingImport = item.getAsJsonObject();  // ExistingImport
      String importedLibraryUri = strings.get(existingImport.get("uri").getAsInt());
      Map<String, Set<String>> importedLibrary = new HashMap<>();
      result.put(importedLibraryUri, importedLibrary);
      // These "elements" are exports from the imported library.
      existingImport.get("elements").getAsJsonArray().forEach(element -> {
        int index = element.getAsInt();
        String declaredLibraryUri = uris.get(index);
        String declaredName = names.get(index);
        Set<String> declaredNames = importedLibrary.get(declaredLibraryUri);
        if (declaredNames == null) {
          declaredNames = new HashSet<>();
          importedLibrary.put(declaredLibraryUri, declaredNames);
        }

        declaredNames.add(declaredName);
      });
    });

    return result;
  }
}