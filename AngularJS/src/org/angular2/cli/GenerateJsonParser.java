// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.cli;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public final class GenerateJsonParser {
  public static List<GenerateCommand> parse(String output) {
    Type listType = new TypeToken<ArrayList<GenerateCommand>>() {
    }.getType();
    return new Gson().fromJson(output, listType);
  }
}
