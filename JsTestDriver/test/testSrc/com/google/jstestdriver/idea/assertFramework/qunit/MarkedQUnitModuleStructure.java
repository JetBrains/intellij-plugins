package com.google.jstestdriver.idea.assertFramework.qunit;

import com.google.common.collect.Lists;
import com.google.jstestdriver.idea.JsTestDriverTestUtils;
import com.google.jstestdriver.idea.assertFramework.Annotation;
import com.google.jstestdriver.idea.util.CastUtils;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import junit.framework.Assert;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MarkedQUnitModuleStructure {

  private static final String KEY_ID = "id";
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

  public void endEncountered(TextRange endAnnotationTextRange, JSFile jsFile) {
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
      JSCallExpression jsCallExpression = CastUtils.tryCast(myPsiElement, JSCallExpression.class);
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

  static int getIdAndValidate(Annotation annotation) {
    String id = annotation.getValue(KEY_ID);
    String name = annotation.getValue(KEY_NAME);
    if (id == null || name == null) {
      throw new RuntimeException(KEY_ID + " and " + KEY_NAME + " should be specified, " + annotation);
    }
    return Integer.parseInt(id);
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
    return new MarkedQUnitModuleStructure(true, 0, "Default", null);
  }

  public static MarkedQUnitModuleStructure newRegularModule(@NotNull Annotation startAnnotation) {
    int id = getIdAndValidate(startAnnotation);
    return new MarkedQUnitModuleStructure(false, id, startAnnotation.getValue(KEY_NAME), startAnnotation);
  }

}
