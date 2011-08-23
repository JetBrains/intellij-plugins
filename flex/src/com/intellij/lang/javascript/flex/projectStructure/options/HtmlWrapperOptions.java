package com.intellij.lang.javascript.flex.projectStructure.options;

public class HtmlWrapperOptions implements Cloneable {

  public enum WrapperType {FromSdk, FromFolder, NoWrapper}

  public WrapperType WRAPPER_TYPE = WrapperType.FromSdk;
  public boolean ENABLE_HISTORY = true;
  public boolean CHECK_PLAYER = true;
  public boolean EXPRESS_INSTALL = true;
  public String TEMPLATE_FOLDER_PATH = "";

  protected HtmlWrapperOptions clone() {
    try {
      return (HtmlWrapperOptions)super.clone();
    }
    catch (CloneNotSupportedException e) {
      assert false;
      return null;
    }
  }

  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final HtmlWrapperOptions that = (HtmlWrapperOptions)o;

    if (ENABLE_HISTORY != that.ENABLE_HISTORY) return false;
    if (CHECK_PLAYER != that.CHECK_PLAYER) return false;
    if (EXPRESS_INSTALL != that.EXPRESS_INSTALL) return false;
    if (!TEMPLATE_FOLDER_PATH.equals(that.TEMPLATE_FOLDER_PATH)) return false;
    if (WRAPPER_TYPE != that.WRAPPER_TYPE) return false;

    return true;
  }

  public int hashCode() {
    assert false;

    int result = WRAPPER_TYPE.hashCode();
    result = 31 * result + (ENABLE_HISTORY ? 1 : 0);
    result = 31 * result + (CHECK_PLAYER ? 1 : 0);
    result = 31 * result + (EXPRESS_INSTALL ? 1 : 0);
    result = 31 * result + TEMPLATE_FOLDER_PATH.hashCode();
    return result;
  }
}
