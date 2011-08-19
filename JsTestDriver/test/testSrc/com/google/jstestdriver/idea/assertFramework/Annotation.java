package com.google.jstestdriver.idea.assertFramework;

import com.google.jstestdriver.idea.JsTestDriverTestUtils;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class Annotation {
  private final String myName;
  private final TextRange myTextRange;
  private final Map<String, String> myProperties;

  public Annotation(@Nullable String name, int startPosition, int endPosition, String propertiesStr) {
    myName = name;
    myTextRange = TextRange.create(startPosition, endPosition);
    myProperties = JsTestDriverTestUtils.parseProperties(propertiesStr);
  }

  public String getValue(String key) {
    return myProperties.get(key);
  }

  public String getName() {
    return myName;
  }

  public TextRange getTextRange() {
    return myTextRange;
  }

  @Override
  public String toString() {
    String props = myProperties.toString();
    return "/*{" + myName + " " + props.substring(1, props.length() - 1) + "}*/";
  }

}
