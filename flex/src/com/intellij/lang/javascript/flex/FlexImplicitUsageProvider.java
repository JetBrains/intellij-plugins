package com.intellij.lang.javascript.flex;

import com.intellij.codeInsight.daemon.ImplicitUsageProvider;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.build.FlexCompilerSettingsEditor;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitSupport;
import com.intellij.lang.javascript.flex.run.FlexRuntimeConfigurationProducer;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSInheritanceUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;

/**
 * User: Maxim.Mossienko
 * Date: 03.09.2010
 * Time: 15:09:53
 */
public class FlexImplicitUsageProvider implements ImplicitUsageProvider, Condition<PsiElement> {
  @Override
  public boolean isImplicitUsage(PsiElement element) {
    if (element instanceof XmlAttribute &&
        ((XmlAttribute)element).isNamespaceDeclaration() &&
        JavaScriptSupportLoader.MXML_URI3.equals(((XmlAttribute)element).getValue())) {
      return true;
    }

    if (element instanceof JSClass) {
      JSClass clazz = (JSClass)element;
      Module moduleForPsiElement = ModuleUtil.findModuleForPsiElement(clazz);
      if (FlexRuntimeConfigurationProducer.isAcceptedMainClass(clazz, moduleForPsiElement, true)) return true;
      if (JSInheritanceUtil.isParentClass(clazz, FlexCompilerSettingsEditor.MODULE_BASE_CLASS_NAME)) return true;

      FlexUnitSupport flexUnitSupport = FlexUnitSupport.getSupport(moduleForPsiElement);
      if (flexUnitSupport != null && flexUnitSupport.isTestClass(clazz, true)) return true;
    } else if (element instanceof JSFunction) {
      if (isTestMethod((JSFunction)element)) return true;
    }
    return false;
  }

  private static boolean isTestMethod(JSFunction function) {
    Module moduleForPsiElement = ModuleUtil.findModuleForPsiElement(function);
    FlexUnitSupport flexUnitSupport = FlexUnitSupport.getSupport(moduleForPsiElement);
    if (flexUnitSupport != null && flexUnitSupport.isTestMethod(function)) return true;
    return false;
  }

  @Override
  public boolean isImplicitRead(PsiElement element) {
    return false;
  }

  @Override
  public boolean isImplicitWrite(PsiElement element) {
    return false;
  }

  @Override
  public boolean value(PsiElement psiNamedElement) {
    if (psiNamedElement instanceof JSFunction) {
      if (isTestMethod((JSFunction)psiNamedElement)) return true;
    }
    return false;
  }
}
