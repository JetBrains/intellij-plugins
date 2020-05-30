/*
 * @author max
 */
package com.intellij.lang.javascript.generation;

import com.intellij.featureStatistics.ProductivityFeatureNames;
import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.validation.ActionScriptImplementedMethodProcessor;
import com.intellij.lang.javascript.validation.fixes.BaseCreateMembersFix;
import com.intellij.lang.javascript.validation.fixes.ImplementMethodsFix;
import com.intellij.psi.PsiElement;

import java.util.Collection;

/**
 * as only
 */
public class JavaScriptImplementMethodsHandlerForFlex extends BaseJSGenerateHandler {
  @Override
  protected void collectCandidates(final PsiElement clazz, final Collection<JSChooserElementNode> candidates) {
    for(JSFunction fun: ActionScriptImplementedMethodProcessor.collectFunctionsToImplement((JSClass)clazz)) {
      candidates.add(new JSNamedElementNode(fun));
    }
  }

  @Override
  protected String getTitleKey() {
    return "methods.to.implement.chooser.title";
  }

  @Override
  protected String getNoCandidatesMessage() {
    return JavaScriptBundle.message("no.methods.to.implement");
  }

  @Override
  protected BaseCreateMembersFix createFix(final PsiElement clazz) {
    return new ImplementMethodsFix((JSClass)clazz);
  }

  @Override
  protected boolean isValidForTarget(PsiElement jsClass) {
    return jsClass instanceof JSClass && !((JSClass)jsClass).isInterface() && !DialectDetector.isJavaScriptFamily(jsClass);
  }

  @Override
  protected String getProductivityFeatureId() {
    return ProductivityFeatureNames.CODEASSISTS_OVERRIDE_IMPLEMENT;
  }
}