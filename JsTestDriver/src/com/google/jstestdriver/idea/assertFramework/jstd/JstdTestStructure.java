package com.google.jstestdriver.idea.assertFramework.jstd;

import com.intellij.javascript.testFramework.util.JsPsiUtils;
import com.intellij.lang.javascript.psi.JSDefinitionExpression;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSFunctionExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JstdTestStructure {

  private final String myTestName;
  private final PsiElement myTestMethodNameDeclaration;
  private final JSDefinitionExpression myWholeLeftDefExpr;
  private final JSFunction myTestMethodBody;
  private final JSProperty myJsProperty;

  private JstdTestStructure(@NotNull String testName,
                            @NotNull PsiElement testMethodNameDeclaration,
                            @Nullable JSDefinitionExpression wholeLeftDefExpr,
                            @Nullable JSFunction testMethodBody,
                            @Nullable JSProperty jsProperty) {
    myTestName = testName;
    myTestMethodNameDeclaration = testMethodNameDeclaration;
    myWholeLeftDefExpr = wholeLeftDefExpr;
    myTestMethodBody = testMethodBody;
    myJsProperty = jsProperty;
  }

  @NotNull
  public String getName() {
    return myTestName;
  }

  @Nullable
  public JSProperty getJsProperty() {
    return myJsProperty;
  }

  @NotNull
  public PsiElement getTestMethodNameDeclaration() {
    return myTestMethodNameDeclaration;
  }

  @Nullable
  public JSDefinitionExpression getWholeLeftDefExpr() {
    return myWholeLeftDefExpr;
  }

  @Nullable
  public JSFunction getTestMethodBody() {
    return myTestMethodBody;
  }

  public boolean containsTextRange(@NotNull TextRange textRange) {
    if (myJsProperty != null) {
      TextRange enclosingTextRange = myJsProperty.getTextRange();
      return enclosingTextRange.contains(textRange);
    }
    TextRange nameTextRange = myTestMethodNameDeclaration.getTextRange();
    if (nameTextRange.contains(textRange)) {
      return true;
    }
    TextRange bodyTextRange = myTestMethodBody.getTextRange();
    return bodyTextRange.contains(textRange);
  }

  @Nullable
  public static JstdTestStructure newPropertyBasedTestStructure(@NotNull JSProperty jsProperty) {
    PsiElement testMethodNameDeclaration = JsPsiUtils.getPropertyNamePsiElement(jsProperty);
    if (testMethodNameDeclaration == null) {
      return null;
    }
    JSFunction testMethodBody = jsProperty.tryGetFunctionInitializer();
    String testName = StringUtil.stripQuotesAroundValue(testMethodNameDeclaration.getText());
    if (checkTestName(testName)) {
      return new JstdTestStructure(testName, testMethodNameDeclaration, null, testMethodBody, jsProperty);
    }
    return null;
  }

  @Nullable
  public static JstdTestStructure newPrototypeBasedTestStructure(@NotNull JSDefinitionExpression wholeLeftDefExpr,
                                                                 @NotNull LeafPsiElement testMethodDeclaration,
                                                                 @Nullable JSFunctionExpression testMethodBody) {
    String testName = testMethodDeclaration.getText();
    if (checkTestName(testName)) {
      return new JstdTestStructure(testName, testMethodDeclaration, wholeLeftDefExpr, testMethodBody, null);
    }
    return null;
  }

  private static boolean checkTestName(@Nullable String testName) {
    return testName != null && testName.startsWith("test");
  }

}
