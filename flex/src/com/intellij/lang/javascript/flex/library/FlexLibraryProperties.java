package com.intellij.lang.javascript.flex.library;

import com.intellij.openapi.roots.libraries.LibraryProperties;
import com.intellij.openapi.util.Comparing;
import com.intellij.util.xmlb.annotations.Attribute;
import org.jetbrains.annotations.Nullable;

/**
* User: ksafonov
*/
public class FlexLibraryProperties extends LibraryProperties<FlexLibraryProperties> {
  @Nullable
  private String myId;

  public FlexLibraryProperties() {
    this(null);
  }

  public FlexLibraryProperties(@Nullable String id) {
    myId = id;
  }

  @Override
  public FlexLibraryProperties getState() {
    return this;
  }

  @Override
  public void loadState(FlexLibraryProperties state) {
    myId = state.myId;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof FlexLibraryProperties && Comparing.equal(((FlexLibraryProperties)obj).myId, myId);
  }

  @Override
  public int hashCode() {
    return myId != null ? myId.hashCode() : 0;
  }

  @Attribute("id")
  @Nullable
  public String getId() {
    return myId;
  }

  public void setId(@Nullable String id) {
    myId = id;
  }
}
