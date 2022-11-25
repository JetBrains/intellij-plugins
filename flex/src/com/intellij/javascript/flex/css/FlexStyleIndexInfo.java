package com.intellij.javascript.flex.css;

import com.intellij.lang.javascript.psi.JSCommonTypeNames;
import com.intellij.lang.javascript.psi.ecmal4.JSAttribute;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeNameValuePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * @author Eugene.Kudelevsky
 */
public class FlexStyleIndexInfo {
  private final String myClassOrFileName;
  private final String myAttributeName;
  private final String myInherit;
  private final String myType;
  private final String myArrayType;
  private final String myFormat;
  private final String myEnumeration;
  private final boolean myInClass;

  public FlexStyleIndexInfo(@NotNull String classOrFileName,
                            @NotNull String attributeName,
                            @NotNull String inherit,
                            @Nullable String type,
                            @Nullable String arrayType,
                            @Nullable String format,
                            @Nullable String enumeration,
                            boolean inClass) {
    myClassOrFileName = classOrFileName;
    myAttributeName = attributeName;
    myInherit = inherit;
    myType = type;
    myArrayType = arrayType;
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

  @Nullable
  public String getArrayType() {
    return myArrayType;
  }

  public boolean isInClass() {
    return myInClass;
  }

  @Nullable
  private static String getValue(JSAttribute attribute, String name) {
    JSAttributeNameValuePair pair = attribute.getValueByName(name);
    return pair != null ? pair.getSimpleValue() : null;
  }

  public static @NotNull FlexStyleIndexInfo create(@NotNull String className, @NotNull String name, @NotNull JSAttribute attribute, boolean inClass) {
    String inherit = getValue(attribute, "inherit");
    if (inherit == null) inherit = "no";
    String type = getValue(attribute, "type");
    String arrayType = JSCommonTypeNames.ARRAY_CLASS_NAME.equals(type) ? getValue(attribute, "arrayType") : null;
    String format = getValue(attribute, "format");
    String enumeration = getValue(attribute, "enumeration");
    return new FlexStyleIndexInfo(className, name, inherit, type, arrayType, format, enumeration, inClass);
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
    return myInClass == that.myInClass && 
           Objects.equals(myAttributeName, that.myAttributeName) &&
           Objects.equals(myClassOrFileName, that.myClassOrFileName) &&
           Objects.equals(myEnumeration, that.myEnumeration) &&
           Objects.equals(myFormat, that.myFormat) &&
           Objects.equals(myInherit, that.myInherit) && Objects.equals(myType, that.myType);
  }

  @Override
  public int hashCode() {
    int result = myClassOrFileName.hashCode();
    result = 31 * result + myAttributeName.hashCode();
    result = 31 * result + myInherit.hashCode();
    result = 31 * result + (myType != null ? myType.hashCode() : 0);
    result = 31 * result + (myFormat != null ? myFormat.hashCode() : 0);
    result = 31 * result + (myEnumeration != null ? myEnumeration.hashCode() : 0);
    result = 31 * result + (myInClass ? 1 : 0);
    return result;
  }
}
