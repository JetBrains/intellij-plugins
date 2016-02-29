package org.angularjs.index;

import org.jetbrains.annotations.Nullable;

/**
 * @author Irina.Chernushina on 2/11/2016.
 */
public class AngularViewDefinition {
  @Nullable private final String myName;
  private final long myStartOffset;

  public AngularViewDefinition(@Nullable String name, long startOffset) {
    myName = name;
    myStartOffset = startOffset;
  }

  @Nullable
  public String getName() {
    return myName;
  }

  public long getStartOffset() {
    return myStartOffset;
  }
}
