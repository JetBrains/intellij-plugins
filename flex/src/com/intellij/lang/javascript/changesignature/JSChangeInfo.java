package com.intellij.lang.javascript.changesignature;

import com.intellij.lang.Language;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.refactoring.changeSignature.JSMethodDescriptor;
import com.intellij.lang.javascript.refactoring.changeSignature.JSParameterInfo;
import com.intellij.refactoring.changeSignature.ChangeInfo;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class JSChangeInfo implements ChangeInfo {
  private final JSFunction myMethod;
  private final String myNewName;
  private final JSAttributeList.AccessType myNewVisibility;
  private final String myNewReturnType;
  private final List<JSParameterInfo> myNewParameters;

  public JSChangeInfo(JSFunction method) {
    myMethod = method;
    myNewName = method.getName();
    myNewVisibility = method.getAttributeList().getAccessType();
    final JSType returnType = method.getReturnType();
    myNewReturnType = returnType == null ? null : returnType.getResolvedTypeText();
    myNewParameters = JSMethodDescriptor.getParameters(method);
  }

  @Override
  public JSParameterInfo @NotNull [] getNewParameters() {
    return myNewParameters.toArray(JSParameterInfo.EMPTY_ARRAY);
  }

  @Override
  public boolean isParameterSetOrOrderChanged() {
    return false;
  }

  @Override
  public JSFunction getMethod() {
    return myMethod;
  }

  @Override
  public boolean isReturnTypeChanged() {
    return false;
  }

  @Override
  public String getNewName() {
    return myNewName;
  }

  @Override
  public Language getLanguage() {
    return JavascriptLanguage.INSTANCE;
  }

  @Override
  public boolean isParameterTypesChanged() {
    return false;
  }

  @Override
  public boolean isParameterNamesChanged() {
    return false;
  }

  @Override
  public boolean isGenerateDelegate() {
    return false;
  }

  @Override
  public boolean isNameChanged() {
    return false;
  }

  public JSAttributeList.AccessType getNewVisibility() {
    return myNewVisibility;
  }
}
