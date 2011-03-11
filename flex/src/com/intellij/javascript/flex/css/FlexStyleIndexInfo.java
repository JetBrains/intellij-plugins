package com.intellij.javascript.flex.css;

import com.intellij.lang.javascript.psi.ecmal4.JSAttribute;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeNameValuePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Eugene.Kudelevsky
 */
public class FlexStyleIndexInfo {
  private final String myClassOrFileName;
  private final String myAttributeName;
  private final String myInherit;
  private final String myType;
  private final String myFormat;
  private final String myEnumeration;
  private final boolean myInClass;

  public FlexStyleIndexInfo(@NotNull String classOrFileName,
                            @NotNull String attributeName,
                            @NotNull String inherit,
                            @Nullable String type,
                            @Nullable String format,
                            @Nullable String enumeration,
                            boolean inClass) {
    myClassOrFileName = classOrFileName;
    myAttributeName = attributeName;
    myInherit = inherit;
    myType = type;
    myFormat = format;
    myEnumeration = enumeration;
    myInClass = inClass;
  }

  @NotNull
  public String getInherit() {
    return myInherit;
  }

  @Nullable
  public String getType() {
    return myType;
  }

  public boolean isInClass() {
    return myInClass;
  }

  @Nullable
  private static String getValue(JSAttribute attribute, String name) {
    JSAttributeNameValuePair pair = attribute.getValueByName(name);
    return pair != null ? pair.getSimpleValue() : null;
  }

  @Nullable
  public static FlexStyleIndexInfo create(@NotNull String className, @NotNull String name, @NotNull JSAttribute attribute, boolean inClass) {
    String inherit = getValue(attribute, "inherit");
    if (inherit == null) inherit = "no";
    String type = getValue(attribute, "type");
    String format = getValue(attribute, "format");
    String enumeration = getValue(attribute, "enumeration");
    return new FlexStyleIndexInfo(className, name, inherit, type, format, enumeration, inClass);
  }

  public String getEnumeration() {
    return myEnumeration;
  }

  @Nullable
  public String getFormat() {
    return myFormat;
  }

  @NotNull
  public String getClassOrFileName() {
    return myClassOrFileName;
  }

  @NotNull
  public String getAttributeName() {
    return myAttributeName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    FlexStyleIndexInfo that = (FlexStyleIndexInfo)o;

    if (myInClass != that.myInClass) return false;
    if (myAttributeName != null ? !myAttributeName.equals(that.myAttributeName) : that.myAttributeName != null) return false;
    if (myClassOrFileName != null ? !myClassOrFileName.equals(that.myClassOrFileName) : that.myClassOrFileName != null) return false;
    if (myEnumeration != null ? !myEnumeration.equals(that.myEnumeration) : that.myEnumeration != null) return false;
    if (myFormat != null ? !myFormat.equals(that.myFormat) : that.myFormat != null) return false;
    if (myInherit != null ? !myInherit.equals(that.myInherit) : that.myInherit != null) return false;
    if (myType != null ? !myType.equals(that.myType) : that.myType != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = myClassOrFileName != null ? myClassOrFileName.hashCode() : 0;
    result = 31 * result + (myAttributeName != null ? myAttributeName.hashCode() : 0);
    result = 31 * result + (myInherit != null ? myInherit.hashCode() : 0);
    result = 31 * result + (myType != null ? myType.hashCode() : 0);
    result = 31 * result + (myFormat != null ? myFormat.hashCode() : 0);
    result = 31 * result + (myEnumeration != null ? myEnumeration.hashCode() : 0);
    result = 31 * result + (myInClass ? 1 : 0);
    return result;
  }
}
