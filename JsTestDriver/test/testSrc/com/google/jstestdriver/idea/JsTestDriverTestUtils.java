package com.google.jstestdriver.idea;

import com.google.common.collect.Maps;
import com.intellij.openapi.application.PathManager;

import java.io.File;
import java.util.Map;
import java.util.regex.Pattern;

public class JsTestDriverTestUtils {

  private JsTestDriverTestUtils() {}

  public static File getTestDataDir() {
    return new File(PathManager.getHomePath(), "contrib/JsTestDriver/test/testData/");
  }

  public static Map<String, String> parseProperties(String propertiesStr) {
    Map<String, String> props = Maps.newLinkedHashMap();
    String[] keyValueStrings = propertiesStr.split(Pattern.quote(","));
    for (String keyValueStr : keyValueStrings) {
      String[] components = keyValueStr.split(Pattern.quote(":"), 2);
      if (components.length == 2) {
        props.put(components[0].trim(), components[1]);
      }
    }
    return props;
  }

}
