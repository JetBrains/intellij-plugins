// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.model.info;

import com.intellij.util.ArrayUtil;
import com.intellij.util.ArrayUtilRt;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

/**
 * @author vnikolaenko
 */
public class CfmlAttributeDescription implements Comparable<CfmlAttributeDescription> {
  private final Pattern myNamePattern;
  // private String myName;
  private final int myType;
  private final boolean myRequired;
  private String myDescription;
  private String myCompletionExample = null;
  private String[] myValues = null;

  public CfmlAttributeDescription(String name, int type, boolean required, String description) {
    myNamePattern = Pattern.compile(name);
    myType = type;
    myRequired = required;
    myDescription = description;
  }

  public CfmlAttributeDescription(String name, int type, boolean required, String description, String completionExample) {
    this(name, type, required, description);
    myCompletionExample = completionExample;
  }

  public void addValue(String value) {
    if (myValues == null) {
      myValues = ArrayUtilRt.EMPTY_STRING_ARRAY;
    }
    myValues = ArrayUtil.append(myValues, value);
  }

  public void setDescription(String description) {
    myDescription = description;
  }

  public String @Nullable [] getValues() {
    return myValues;
  }

  public String getName() {
    return myNamePattern.matcher(myNamePattern.pattern()).matches() ? myNamePattern.pattern() : myCompletionExample;
  }

  public String getDescription() {
    return myDescription;
  }

  public boolean acceptName(String name) {
    return myNamePattern.matcher(name).matches();
  }

  public int getType() {
    return myType;
  }

  public boolean isRequired() {
    return myRequired;
  }

  @Override
  public int compareTo(CfmlAttributeDescription o) {
    return myNamePattern.pattern().compareTo(o.myNamePattern.pattern());
  }

  @Override
  public String toString() {
    return "<div>" +
           myNamePattern.pattern() +
           "</div>" +
           "<div>" +
           getDescription() +
           "</div>" +
           "<div>" +
           getType() +
           "</div>" +
           "<div>" +
           isRequired() +
           "</div>";
  }
}
