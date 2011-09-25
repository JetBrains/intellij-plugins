package com.intellij.lang.javascript.flex.projectStructure;

import com.intellij.lang.javascript.flex.sdk.FlexSdkType;

/**
 * @author ksafonov
 */
public class FlexIdeUtils {
  public static boolean isNewUI() {
    return "true".equals(System.getProperty("flexide.new.ui"));
  }

  public static FlexSdkType getSdkType() {
    return FlexSdkType.getInstance();
  }
}
