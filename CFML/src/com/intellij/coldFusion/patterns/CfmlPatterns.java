package com.intellij.coldFusion.patterns;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.impl.source.tree.LeafPsiElement;

/**
 * @author Sergey Karashevich
 */
public class CfmlPatterns extends PlatformPatterns {

  public static LeafPsiElementPattern.Capture<LeafPsiElement> sqlCapture() {
    return CfmlPatternsUtil.INSTANCE.sqlCapture();
  }

}
