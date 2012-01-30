package com.intellij.lang.javascript.flex.projectStructure;

import com.intellij.openapi.roots.libraries.LibraryProperties;
import com.intellij.util.xmlb.annotations.Attribute;
import org.jetbrains.annotations.Nullable;

/**
 * User: ksafonov
 */
public class FlexSdkProperties extends LibraryProperties<FlexSdkProperties> {
  @Nullable
  private String myId;

  @Nullable
  private String myHomePath;

  @Nullable
  private String myVersion;


  public FlexSdkProperties() {
    this(null);
  }

  public FlexSdkProperties(@Nullable String id) {
    myId = id;
  }

  @Override
  public FlexSdkProperties getState() {
    return this;
  }

  @Override
  public void loadState(FlexSdkProperties state) {
    myId = state.myId;
    myHomePath = state.myHomePath;
    myVersion = state.myVersion;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    FlexSdkProperties that = (FlexSdkProperties)o;

    if (myHomePath != null ? !myHomePath.equals(that.myHomePath) : that.myHomePath != null) return false;
    if (myId != null ? !myId.equals(that.myId) : that.myId != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = myId != null ? myId.hashCode() : 0;
    result = 31 * result + (myHomePath != null ? myHomePath.hashCode() : 0);
    return result;
  }

  @Attribute("id")
  @Nullable
  public String getId() {
    return myId;
  }

  public void setId(@Nullable String id) {
    myId = id;
  }

  @Attribute("home")
  @Nullable
  public String getHomePath() {
    return myHomePath;
  }

  public void setHomePath(@Nullable String homePath) {
    myHomePath = homePath;
  }

  @Attribute("version")
  @Nullable
  public String getVersion() {
    return myVersion;
  }

  public void setVersion(@Nullable String version) {
    myVersion = version;
  }

}
