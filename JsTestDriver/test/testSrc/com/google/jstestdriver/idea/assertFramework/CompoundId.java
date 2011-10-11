package com.google.jstestdriver.idea.assertFramework;

import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.regex.Pattern;

public class CompoundId {

  @NotNull
  private final int[] myData;

  public CompoundId(@NotNull String text) {
    String[] s = text.split(Pattern.quote("_"));
    int[] data = new int[s.length];
    for (int i = 0; i < s.length; i++) {
      data[i] = Integer.parseInt(s[i]);
      if (data[i] < 0) {
        throw new RuntimeException("All components of CompoundId should be greater than 0, " + text);
      }
    }
    myData = data;
  }

  public CompoundId(@NotNull int[] data) {
    myData = Arrays.copyOf(data, data.length);
  }

  public CompoundId(int id) {
    myData = new int[] {id};
  }

  @NotNull
  public CompoundId getParentId() {
    if (!hasParent()) {
      throw new RuntimeException("No parent defined for " + this);
    }
    return new CompoundId(Arrays.copyOf(myData, myData.length - 1));
  }

  public boolean hasParent() {
    return myData.length > 0;
  }

  @Override
  public String toString() {
    return StringUtil.join(myData, "_");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CompoundId that = (CompoundId)o;

    return Arrays.equals(myData, that.myData);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(myData);
  }

  public int getComponentCount() {
    return myData.length;
  }

  public int getFirstComponent() {
    return myData[0];
  }
}
