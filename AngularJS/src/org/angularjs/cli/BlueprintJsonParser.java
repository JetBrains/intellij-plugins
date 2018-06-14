package org.angularjs.cli;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class BlueprintJsonParser {

  public static List<Blueprint> parse(String output) {
    Type listType = new TypeToken<ArrayList<Blueprint>>() {
    }.getType();
    return new GsonBuilder().create().fromJson(output, listType);
  }
}
