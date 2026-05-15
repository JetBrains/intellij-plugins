package com.intellij.lang.javascript.linter.jshint.version;

import com.intellij.openapi.util.NlsSafe;
import org.jetbrains.annotations.NotNull;

/**
 * @author Sergey Simonchik
 */
public class JSHintVersionDescriptor {

  private final String myVersion;
  private final String myUrl;

  public JSHintVersionDescriptor(@NotNull String version, @NotNull String url) {
    myVersion = version;
    myUrl = url;
  }

  public @NotNull @NlsSafe String getVersion() {
    return myVersion;
  }

  public @NotNull @NlsSafe String getUrl() {
    return myUrl;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    JSHintVersionDescriptor that = (JSHintVersionDescriptor)o;

    return  myUrl.equals(that.myUrl) && myVersion.equals(that.myVersion);
  }

  @Override
  public int hashCode() {
    int result = myVersion.hashCode();
    result = 31 * result + myUrl.hashCode();
    return result;
  }

  public boolean isBundled() {
    return JSHintVersionUtil.isBundledVersion(myVersion);
  }

  @Override
  public String toString() {
    return "(version:" + myVersion + ", url:" + myUrl + ")";
  }
}
