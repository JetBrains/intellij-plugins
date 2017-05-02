package com.intellij.coldFusion.patterns;

import com.intellij.coldFusion.model.psi.impl.CfmlTagImpl;
import com.intellij.patterns.PlatformPatterns;

/**
 * @author Sergey Karashevich
 */
public class CfmlPatterns extends PlatformPatterns {


  public static CfmlTagImplPattern.Capture<CfmlTagImpl> sqlCapture() {
    return CfmlPatternsUtil.INSTANCE.sqlCapture();
  }

}
