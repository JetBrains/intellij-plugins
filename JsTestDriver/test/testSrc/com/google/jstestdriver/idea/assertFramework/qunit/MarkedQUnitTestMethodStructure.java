package com.google.jstestdriver.idea.assertFramework.qunit;

import com.google.jstestdriver.idea.JsTestDriverTestUtils;
import com.google.jstestdriver.idea.assertFramework.Annotation;
import com.google.jstestdriver.idea.assertFramework.CompoundId;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.util.ObjectUtils;
import junit.framework.Assert;

class MarkedQUnitTestMethodStructure {

  private static final String KEY_NAME = "name";

  private final CompoundId myId;
  private final String myName;
  private final int myStartPosition;
  private PsiElement myPsiElement;

  public MarkedQUnitTestMethodStructure(Annotation startAnnotation) {
    CompoundId id = startAnnotation.getCompoundId();
    if (id.getComponentCount() != 2) {
      throw new RuntimeException("Malformed test id: " + id + ", " + this);
    }
    myId = id;
    myName = startAnnotation.getRequiredValue(KEY_NAME);
    myStartPosition = startAnnotation.getTextRange().getEndOffset();
  }

  public CompoundId getId() {
    return myId;
  }

  public int getModuleId() {
    return myId.getFirstComponent();
  }

  public String getName() {
    return myName;
  }

  public void endEncountered(TextRange endAnnotationTextRange, JSFile jsFile) {
    if (myPsiElement != null) {
      throw new RuntimeException("End annotation is already encountered");
    }
    int endPosition = endAnnotationTextRange.getStartOffset();
    myPsiElement = JsTestDriverTestUtils.findExactPsiElement(jsFile, TextRange.create(myStartPosition, endPosition));
  }

  public void validateBuiltStructure() {
    JSCallExpression jsCallExpression = ObjectUtils.tryCast(myPsiElement, JSCallExpression.class);
    if (jsCallExpression == null) {
      Assert.fail("Unable to find underlying " + JSCallExpression.class + " for " + this + ", found: " + myPsiElement);
    }
  }

  public JSCallExpression getCallExpression() {
    return (JSCallExpression) myPsiElement;
  }

}
