package com.google.jstestdriver.idea.assertFramework.jstd;

import com.google.common.collect.Lists;
import com.intellij.javascript.testFramework.Annotation;
import com.intellij.javascript.testFramework.JsTestCommonTestUtil;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;

import java.util.List;

class MarkedTestCaseStructure {
  static final String KEY_ID = "id";
  static final String KEY_NAME = "name";

  private final String myName;
  private final int myId;
  private final int myStartPosition;
  private PsiElement myPsiElement;
  private final List<MarkedTestStructure> myMarkedTestStructureList = Lists.newArrayList();

  MarkedTestCaseStructure(Annotation startAnnotation) {
    String id = startAnnotation.getValue(KEY_ID);
    String name = startAnnotation.getValue(KEY_NAME);
    myId = Integer.parseInt(id);
    myName = name;
    myStartPosition = startAnnotation.getTextRange().getEndOffset();
  }

  static int getIdAndValidate(Annotation annotation) {
    String id = annotation.getValue(KEY_ID);
    String name = annotation.getValue(KEY_NAME);
    if (id == null || name == null) {
      throw new RuntimeException(KEY_ID + " and " + KEY_NAME + " should be specified, " + annotation);
    }
    return Integer.parseInt(id);
  }

  void endEncountered(TextRange textRange, JSFile jsFile) {
    if (myPsiElement != null) {
      throw new RuntimeException("End annotation is already encountered");
    }
    int endPosition = textRange.getStartOffset();
    myPsiElement = JsTestCommonTestUtil.findExactPsiElement(jsFile, TextRange.create(myStartPosition, endPosition));
  }

  public void addTestStructureInfo(MarkedTestStructure markedTestStructure) {
    myMarkedTestStructureList.add(markedTestStructure);
  }

  public int getId() {
    return myId;
  }

  public String getName() {
    return myName;
  }

  public PsiElement getPsiElement() {
    return myPsiElement;
  }

  public List<MarkedTestStructure> getMarkedTestStructures() {
    return myMarkedTestStructureList;
  }

  @Override
  public String toString() {
    return "MarkedTestCaseStructure{name:" + myName + ", id:" + myId + "}";
  }
}
