package com.google.jstestdriver.idea.assertFramework.jstd;

import com.google.common.collect.Maps;
import com.intellij.javascript.testFramework.Annotation;
import com.intellij.javascript.testFramework.JsTestCommonTestUtil;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSFunctionExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.regex.Pattern;

class MarkedTestStructure {

  private static final String KEY_ID = "id";
  private static final String KEY_NAME = "name";
  private static final String KEY_TYPE = "type";

  private final String myId;
  private final int myTestCaseId;
  private String myName;
  private final Map<String, PsiElement> myPsiElementByComponentNameMap = Maps.newHashMap();
  private final Map<String, Integer> myStartOffsetByComponentNameMap = Maps.newHashMap();

  MarkedTestStructure(Annotation annotation) {
    String id = getIdAndValidate(annotation);
    String[] idComponents = id.split(Pattern.quote("_"));
    if (idComponents.length != 2) {
      throw new RuntimeException("Malformed test id: " + id);
    }
    myId = id;
    myTestCaseId = Integer.parseInt(idComponents[0]);
  }

  void handleBeginAnnotation(Annotation annotation) {
    getIdAndValidate(annotation);
    String name = annotation.getValue(KEY_NAME);
    if (name != null) {
      if (myName != null) {
        throw new RuntimeException(KEY_NAME + " attribute should be specified one, TestId: " + myId);
      }
      myName = name;
    }
    String componentName = annotation.getValue(KEY_TYPE);
    if (myStartOffsetByComponentNameMap.containsKey(componentName)) {
      throw new RuntimeException("Start offset has been specified multiply times, myId: " + myId + ", component: " + componentName);
    }
    myStartOffsetByComponentNameMap.put(componentName, annotation.getTextRange().getEndOffset());
  }

  void handleEndAnnotation(Annotation annotation, JSFile jsFile) {
    String componentName = annotation.getValue(KEY_TYPE);
    Integer startOffset = myStartOffsetByComponentNameMap.get(componentName);
    if (startOffset == null) {
      throw new RuntimeException("Start offset has not been specified for id:" + myId + ", component: " + componentName + ", " + annotation);
    }
    TextRange textRange = TextRange.create(startOffset, annotation.getTextRange().getStartOffset());
    PsiElement psiElement = JsTestCommonTestUtil.findExactPsiElement(jsFile, textRange);
    if (myPsiElementByComponentNameMap.containsKey(componentName)) {
      throw new RuntimeException("Component is already there");
    }
    myPsiElementByComponentNameMap.put(componentName, psiElement);
  }

  @NotNull
  static String getIdAndValidate(Annotation annotation) {
    String id = annotation.getValue(KEY_ID);
    if (id == null) {
      throw new RuntimeException(KEY_ID + " attribute should be specified in " + annotation);
    }
    if (!"Test".equals(annotation.getName())) {
      throw new RuntimeException("Name is expected to be 'Test' in " + annotation);
    }
    if (annotation.getValue(KEY_TYPE) == null) {
      throw new RuntimeException("Test annotation " + annotation + " should have '" + KEY_TYPE + "' attribute!");
    }
    return id;
  }

  void validateBuiltTest() {
    if (myName == null) {
      throw new RuntimeException(KEY_NAME + " attribute should be specified for TestId:" + myId);
    }
    for (String componentName : myStartOffsetByComponentNameMap.keySet()) {
      if (!myPsiElementByComponentNameMap.containsKey(componentName)) {
        throw new RuntimeException("Some components does not have ending.");
      }
    }
  }

  public int getTestCaseId() {
    return myTestCaseId;
  }

  public String getId() {
    return myId;
  }

  public String getName() {
    return myName;
  }

  @Nullable
  public JSProperty getPropertyPsiElement() {
    PsiElement psiElement = myPsiElementByComponentNameMap.get("property");
    if (psiElement != null) {
      return (JSProperty) psiElement;
    }
    return null;
  }

  @Nullable
  public LeafPsiElement getDeclarationPsiElement() {
    PsiElement psiElement = myPsiElementByComponentNameMap.get("declaration");
    if (psiElement != null) {
      return (LeafPsiElement) psiElement;
    }
    return null;
  }

  @Nullable
  public JSFunctionExpression getBodyPsiElement() {
    PsiElement psiElement = myPsiElementByComponentNameMap.get("body");
    if (psiElement != null) {
      return (JSFunctionExpression) psiElement;
    }
    return null;
  }

}
