package com.google.jstestdriver.idea.assertFramework;

import com.google.jstestdriver.idea.JsTestDriverTestUtils;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Annotation {

  private static final Pattern PARSE_PATTERN = Pattern.compile("/\\*(\\w+) (.+?)\\*/");

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

  public CompoundId getCompoundId() {
    String idStr = getStringId();
    return new CompoundId(idStr);
  }

  public int getPositiveIntId() {
    String idStr = getStringId();
    int id = Integer.parseInt(idStr);
    if (id <= 0) {
      throw new RuntimeException("id should be greater than 0, " + this);
    }
    return id;
  }

  @NotNull
  public String getStringId() {
    return getRequiredValue("id");
  }

  @NotNull
  public String getRequiredValue(String key) {
    String value = getValue(key);
    if (value == null) {
      throw new RuntimeException("Attribute '" + key + "' should be specified, " + this);
    }
    return value;
  }

  @Override
  public String toString() {
    String props = myProperties.toString();
    return "/*" + myName + " " + props.substring(1, props.length() - 1) + "*/";
  }

  public static Annotation fromMatcher(@NotNull Matcher extMatcher) {
    Matcher matcher = PARSE_PATTERN.matcher(extMatcher.group());
    if (matcher.find()) {
      String name = matcher.group(1);
      String properties = matcher.group(2);
      return new Annotation(name, extMatcher.start(), extMatcher.end(), properties);
    }
    throw new RuntimeException("Can't match '" + extMatcher.group() + "' against " + PARSE_PATTERN.pattern());
  }

}
