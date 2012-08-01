package com.google.jstestdriver.idea.assertFramework.qunit;

import com.google.common.collect.Lists;
import com.google.jstestdriver.idea.JsTestDriverTestUtils;
import com.google.jstestdriver.idea.assertFramework.Annotation;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.util.ObjectUtils;
import junit.framework.Assert;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

class MarkedQUnitModuleStructure {

  private static final String KEY_NAME = "name";

  private boolean myIsDefault;
  private final int myId;
  private final String myName;
  private PsiElement myPsiElement;
  private final int myStartPosition;
  private List<MarkedQUnitTestMethodStructure> myTestStructures = Lists.newArrayList();

  private MarkedQUnitModuleStructure(boolean isDefault, int id, String name, @Nullable Annotation startAnnotation) {
    myIsDefault = isDefault;
    myId = id;
    myName = name;
    if (startAnnotation != null) {
      myStartPosition = startAnnotation.getTextRange().getEndOffset();
    } else {
      myStartPosition = -1;
    }
  }

  public void endAnnotationEncountered(TextRange endAnnotationTextRange, JSFile jsFile) {
    if (myPsiElement != null) {
      throw new RuntimeException("End annotation is already encountered");
    }
    int endPosition = endAnnotationTextRange.getStartOffset();
    myPsiElement = JsTestDriverTestUtils.findExactPsiElement(jsFile, TextRange.create(myStartPosition, endPosition));
  }

  public int getId() {
    return myId;
  }

  public void validate() {
    if (!myIsDefault) {
      JSCallExpression jsCallExpression = ObjectUtils.tryCast(myPsiElement, JSCallExpression.class);
      if (jsCallExpression == null) {
        Assert.fail("Unable to find underlying " + JSCallExpression.class + " for " + this);
      }
    }
  }

  public void addTestStructure(MarkedQUnitTestMethodStructure markedQUnitTestStructure) {
    myTestStructures.add(markedQUnitTestStructure);
  }

  public String getName() {
    return myName;
  }

  public PsiElement getPsiElement() {
    return myPsiElement;
  }

  public List<MarkedQUnitTestMethodStructure> getTestStructures() {
    return myTestStructures;
  }

  @Override
  public String toString() {
    return "MarkedQUnitModuleStructure{myId=" + myId + ", myName='" + myName + "\'}";
  }

  public static MarkedQUnitModuleStructure newDefaultModule() {
    return new MarkedQUnitModuleStructure(true, 0, "Default Module", null);
  }

  public static MarkedQUnitModuleStructure newRegularModule(@NotNull Annotation startAnnotation) {
    int id = startAnnotation.getPositiveIntId();
    String name = startAnnotation.getRequiredValue(KEY_NAME);
    return new MarkedQUnitModuleStructure(false, id, name, startAnnotation);
  }

}
