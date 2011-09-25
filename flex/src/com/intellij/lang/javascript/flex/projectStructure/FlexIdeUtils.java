package com.intellij.lang.javascript.flex.projectStructure;

import com.intellij.lang.javascript.flex.sdk.FlexSdkType;

/**
 * @author ksafonov
 */
public class FlexIdeUtils {
  public static FlexSdkType getSdkType() {
    return FlexSdkType.getInstance();
  }
}
