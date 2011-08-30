package com.intellij.lang.javascript.flex.projectStructure;

import com.intellij.lang.javascript.flex.sdk.FlexSdkType;
import com.intellij.openapi.projectRoots.SdkType;

/**
 * @author ksafonov
 */
public class FlexIdeUtils {
  public static boolean isFlatUi() {
    return "true".equals(System.getProperty("flexide.ui.in.tabs"));
  }

  public static boolean isNewUI() {
    return "true".equals(System.getProperty("flexide.new.ui"));
  }

  public static SdkType getSdkType() {
    return FlexSdkType.getInstance();
  }
}
