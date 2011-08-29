package com.google.jstestdriver.idea.assertFramework.jasmine;

import com.google.common.collect.Lists;
import com.google.jstestdriver.idea.JsTestDriverTestUtils;
import com.google.jstestdriver.idea.assertFramework.Annotation;
import com.google.jstestdriver.idea.assertFramework.CompoundId;
import com.google.jstestdriver.idea.util.CastUtils;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import junit.framework.Assert;

import java.util.List;

class MarkedJasmineSuiteStructure extends MarkedJasmineSuiteStructureContainer {

  private static final String KEY_NAME = "name";

  private final CompoundId myId;
  private final String myName;
  private final int myStartPosition;
  private PsiElement myPsiElement;
  private final List<MarkedJasmineSpecStructure> myInnerSpecStructures = Lists.newArrayList();

  public MarkedJasmineSuiteStructure(Annotation startAnnotation) {
    myId = startAnnotation.getCompoundId();
    myName = startAnnotation.getRequiredValue(KEY_NAME);
    myStartPosition = startAnnotation.getTextRange().getEndOffset();
  }

  public CompoundId getId() {
    return myId;
  }

  public String getName() {
    return myName;
  }

  public void endAnnotationEncountered(TextRange endAnnotationTextRange, JSFile jsFile) {
    if (myPsiElement != null) {
      throw new RuntimeException("End annotation is already encountered");
    }
    int endPosition = endAnnotationTextRange.getStartOffset();
    myPsiElement = JsTestDriverTestUtils.findExactPsiElement(jsFile, TextRange.create(myStartPosition, endPosition));
  }

  public void validate() {
    JSCallExpression jsCallExpression = CastUtils.tryCast(myPsiElement, JSCallExpression.class);
    if (jsCallExpression == null) {
      Assert.fail("Unable to find underlying " + JSCallExpression.class + " for " + this);
    }
  }

  public PsiElement getPsiElement() {
    return myPsiElement;
  }

  public void addSpecStructure(MarkedJasmineSpecStructure specStructure) {
    myInnerSpecStructures.add(specStructure);
  }

  public List<MarkedJasmineSpecStructure> getInnerSpecStructures() {
    return myInnerSpecStructures;
  }
}
