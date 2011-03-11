package com.intellij.lang.javascript.changesignature;

import com.intellij.lang.Language;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.refactoring.changeSignature.JSMethodDescriptor;
import com.intellij.lang.javascript.refactoring.changeSignature.JSParameterInfo;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.changeSignature.ChangeInfo;
import com.intellij.refactoring.changeSignature.ParameterInfo;
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
    myNewReturnType = method.getReturnType().getResolvedTypeText();
    myNewParameters = JSMethodDescriptor.getParameters(method);
  }

  @NotNull
  @Override
  public JSParameterInfo[] getNewParameters() {
    return myNewParameters.toArray(new JSParameterInfo[myNewParameters.size()]);
  }

  @Override
  public boolean isParameterSetOrOrderChanged() {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public JSFunction getMethod() {
    return myMethod;
  }

  @Override
  public boolean isReturnTypeChanged() {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
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
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public boolean isParameterNamesChanged() {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public boolean isGenerateDelegate() {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public boolean isNameChanged() {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public JSAttributeList.AccessType getNewVisibility() {
    return myNewVisibility;
  }
}
