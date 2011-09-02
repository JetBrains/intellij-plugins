package com.intellij.lang.javascript.flex.projectStructure.options;

public class AirDesktopPackagingOptions extends AirPackagingOptions implements Cloneable {
  
  public boolean USE_GENERATED_DESCRIPTOR = true;
  public String CUSTOM_DESCRIPTOR_PATH = "";
  public String INSTALLER_FILE_NAME = "";

  protected AirDesktopPackagingOptions clone() {
    try {
      return (AirDesktopPackagingOptions)super.clone();
    }
    catch (CloneNotSupportedException e) {
      assert false;
      return null;
    }
  }

  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final AirDesktopPackagingOptions that = (AirDesktopPackagingOptions)o;

    if (USE_GENERATED_DESCRIPTOR != that.USE_GENERATED_DESCRIPTOR) return false;
    if (!CUSTOM_DESCRIPTOR_PATH.equals(that.CUSTOM_DESCRIPTOR_PATH)) return false;
    if (!INSTALLER_FILE_NAME.equals(that.INSTALLER_FILE_NAME)) return false;

    return true;
  }

  public int hashCode() {
    assert false;
    int result = (USE_GENERATED_DESCRIPTOR ? 1 : 0);
    result = 31 * result + CUSTOM_DESCRIPTOR_PATH.hashCode();
    result = 31 * result + INSTALLER_FILE_NAME.hashCode();
    return result;
  }
}
