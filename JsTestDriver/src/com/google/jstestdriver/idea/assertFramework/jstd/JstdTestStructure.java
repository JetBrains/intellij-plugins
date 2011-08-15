package com.google.jstestdriver.idea.assertFramework.jstd;

import com.google.jstestdriver.idea.assertFramework.BaseTestStructure;
import com.google.jstestdriver.idea.util.CastUtils;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSFunctionExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JstdTestStructure extends BaseTestStructure {

  private final LeafPsiElement myTestMethodDeclaration;
  private final JSFunctionExpression myTestMethodBody;
  private final JSProperty myJsProperty;

  public JstdTestStructure(@NotNull String testName,
                           @NotNull LeafPsiElement testMethodDeclaration,
                           @Nullable JSFunctionExpression testMethodBody,
                           @Nullable JSProperty jsProperty) {
    super(testName);
    myTestMethodDeclaration = testMethodDeclaration;
    myTestMethodBody = testMethodBody;
    myJsProperty = jsProperty;
  }

  public JSProperty getJsProperty() {
    return myJsProperty;
  }

  public LeafPsiElement getTestMethodDeclaration() {
    return myTestMethodDeclaration;
  }

  public JSFunctionExpression getTestMethodBody() {
    return myTestMethodBody;
  }

  @Nullable
  public static JstdTestStructure newPropertyBasedStructure(@NotNull JSProperty jsProperty) {
    LeafPsiElement testMethodDeclaration = CastUtils.tryCast(jsProperty.getFirstChild(), LeafPsiElement.class);
    if (testMethodDeclaration == null) {
      return null;
    }
    JSFunctionExpression testMethodBody = CastUtils.tryCast(jsProperty.getValue(), JSFunctionExpression.class);
    String testName = StringUtil.stripQuotesAroundValue(testMethodDeclaration.getText());
    return new JstdTestStructure(testName, testMethodDeclaration, testMethodBody, jsProperty);
  }

  @NotNull
  public static JstdTestStructure newPrototypeBasedStructure(@NotNull LeafPsiElement testMethodDeclaration,
                                                             @Nullable JSFunctionExpression testMethodBody) {
    String testName = testMethodDeclaration.getText();
    return new JstdTestStructure(testName, testMethodDeclaration, testMethodBody, null);
  }

}
