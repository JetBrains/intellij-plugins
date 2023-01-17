package org.angularjs.index;

import com.intellij.util.containers.ComparatorUtil;
import org.jetbrains.annotations.Nullable;

public class AngularNamedItemDefinition {
  private final @Nullable String myName;
  private final long myStartOffset;

  public AngularNamedItemDefinition(@Nullable String name, long startOffset) {
    myName = name;
    myStartOffset = startOffset;
  }

  public @Nullable String getName() {
    return myName;
  }

  public long getStartOffset() {
    return myStartOffset;
  }

  @Override
  public int hashCode() {
    int hashcode = myName != null ? myName.hashCode() : 0;
    return hashcode * 31 + (int)myStartOffset;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (!(obj instanceof AngularNamedItemDefinition)) return false;

    return myStartOffset == ((AngularNamedItemDefinition)obj).myStartOffset &&
           ComparatorUtil.equalsNullable(myName, ((AngularNamedItemDefinition)obj).myName);
  }
}
