package com.google.jstestdriver.idea.assertFramework.jstd;

import com.google.jstestdriver.idea.util.CastUtils;
import com.google.jstestdriver.idea.util.JsPsiUtils;
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
  private final JSFunctionExpression myTestMethodBody;
  private final JSProperty myJsProperty;

  private JstdTestStructure(@NotNull String testName,
                            @NotNull PsiElement testMethodNameDeclaration,
                            @Nullable JSFunctionExpression testMethodBody,
                            @Nullable JSProperty jsProperty) {
    myTestName = testName;
    myTestMethodNameDeclaration = testMethodNameDeclaration;
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
  public JSFunctionExpression getTestMethodBody() {
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
    JSFunctionExpression testMethodBody = CastUtils.tryCast(jsProperty.getValue(), JSFunctionExpression.class);
    String testName = StringUtil.stripQuotesAroundValue(testMethodNameDeclaration.getText());
    return new JstdTestStructure(testName, testMethodNameDeclaration, testMethodBody, jsProperty);
  }

  @NotNull
  public static JstdTestStructure newPrototypeBasedTestStructure(@NotNull LeafPsiElement testMethodDeclaration,
                                                                 @Nullable JSFunctionExpression testMethodBody) {
    String testName = testMethodDeclaration.getText();
    return new JstdTestStructure(testName, testMethodDeclaration, testMethodBody, null);
  }

}
