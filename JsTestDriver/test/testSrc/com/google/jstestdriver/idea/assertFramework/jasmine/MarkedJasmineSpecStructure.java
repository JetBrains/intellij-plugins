package com.google.jstestdriver.idea.assertFramework.jasmine;

import com.google.jstestdriver.idea.JsTestDriverTestUtils;
import com.google.jstestdriver.idea.assertFramework.Annotation;
import com.google.jstestdriver.idea.assertFramework.CompoundId;
import com.google.jstestdriver.idea.util.CastUtils;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import junit.framework.Assert;
import org.jetbrains.annotations.NotNull;

class MarkedJasmineSpecStructure {

  private static final String KEY_NAME = "name";

  private final CompoundId myId;
  private final String myName;
  private final int myStartPosition;
  private PsiElement myPsiElement;

  public MarkedJasmineSpecStructure(Annotation startAnnotation) {
    CompoundId id = startAnnotation.getCompoundId();
    if (id.getComponentCount() < 2) {
      throw new RuntimeException("Malformed spec id: " + id + ", " + startAnnotation);
    }
    myId = id;
    myName = startAnnotation.getRequiredValue(KEY_NAME);
    myStartPosition = startAnnotation.getTextRange().getEndOffset();
  }

  @NotNull
  public CompoundId getId() {
    return myId;
  }

  @NotNull
  public CompoundId getSuiteId() {
    return myId.getParentId();
  }

  public String getName() {
    return myName;
  }

  public PsiElement getPsiElement() {
    return myPsiElement;
  }

  public void endAnnotationEncountered(@NotNull TextRange endAnnotationTextRange, @NotNull JSFile jsFile) {
    if (myPsiElement != null) {
      throw new RuntimeException("End annotation is already encountered");
    }
    int endPosition = endAnnotationTextRange.getStartOffset();
    myPsiElement = JsTestDriverTestUtils.findExactPsiElement(jsFile, TextRange.create(myStartPosition, endPosition));
  }

  public void validate() {
    JSCallExpression jsCallExpression = CastUtils.tryCast(myPsiElement, JSCallExpression.class);
    if (jsCallExpression == null) {
      Assert.fail("Unable to find underlying " + JSCallExpression.class + " for " + this + ", found: " + myPsiElement);
    }
  }
}
