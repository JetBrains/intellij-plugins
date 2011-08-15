package com.google.jstestdriver.idea.assertFramework.jstd;

import com.google.jstestdriver.idea.JsTestDriverTestUtils;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

class Annotation {
  final String myName;
  final TextRange myTextRange;
  final Map<String, String> myProperties;

  Annotation(@Nullable String name, int startPosition, int endPosition, String propertiesStr) {
    myName = name;
    myTextRange = TextRange.create(startPosition, endPosition);
    myProperties = JsTestDriverTestUtils.parseProperties(propertiesStr);
  }

  String getValue(String key) {
    return myProperties.get(key);
  }

  public String getName() {
    return myName;
  }

  @Override
  public String toString() {
    String props = myProperties.toString();
    return "/*{" + myName + " " + props.substring(1, props.length() - 1) + "}*/";
  }

}
