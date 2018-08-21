package org.angularjs.cli;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SchematicsJsonParser {

  public static List<Schematic> parse(String output) {
    Type listType = new TypeToken<ArrayList<Schematic>>() {
    }.getType();
    return new GsonBuilder().create().fromJson(output, listType);
  }
}
