package com.intellij.javascript.bower;

import com.intellij.util.containers.ComparatorUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BowerPackageBasicInfo {
  private final String myName;
  private final String myDescription;

  public BowerPackageBasicInfo(@NotNull String name, @Nullable String description) {
    myName = name;
    myDescription = description;
  }

  public @NotNull String getName() {
    return myName;
  }

  public @Nullable String getDescription() {
    return myDescription;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BowerPackageBasicInfo info = (BowerPackageBasicInfo)o;
    return myName.equals(info.myName) && ComparatorUtil.equalsNullable(myDescription, info.myDescription);
  }

  @Override
  public int hashCode() {
    int result = myName.hashCode();
    result = 31 * result + (myDescription != null ? myDescription.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "name=" + myName + ", description=" + myDescription;
  }
}
