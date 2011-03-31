/*
 * @author max
 */
package com.intellij.lang.javascript.generation;

import com.intellij.featureStatistics.ProductivityFeatureNames;
import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSInheritanceUtil;
import com.intellij.lang.javascript.validation.fixes.BaseCreateMethodsFix;

import java.util.Collection;

public class JavaScriptOverrideMethodsHandler extends BaseJSGenerateHandler {
  protected String getTitleKey() {
    return "methods.to.override.chooser.title";
  }

  protected String getNoCandidatesMessage() {
    return JSBundle.message("no.methods.to.override");
  }

  protected BaseCreateMethodsFix createFix(final JSClass clazz) {
    return new OverrideMethodsFix(clazz);
  }

  protected void collectCandidates(final JSClass clazz, final Collection<JSNamedElementNode> candidates) {
    for(JSFunction function:JSInheritanceUtil.collectFunctionsToOverride(clazz)) {
      candidates.add(new JSNamedElementNode(function));
    }
  }

  @Override
  protected String getProductivityFeatureId() {
    return ProductivityFeatureNames.CODEASSISTS_OVERRIDE_IMPLEMENT;
  }
}