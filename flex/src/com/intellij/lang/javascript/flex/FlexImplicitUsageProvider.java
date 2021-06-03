package com.intellij.lang.javascript.flex;

import com.intellij.codeInsight.daemon.ImplicitUsageProvider;
import com.intellij.javascript.flex.FlexAnnotationNames;
import com.intellij.javascript.flex.mxml.FlexCommonTypeNames;
import com.intellij.javascript.flex.resolve.ActionScriptClassResolver;
import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitSupport;
import com.intellij.lang.javascript.flex.run.FlashRunConfigurationForm;
import com.intellij.lang.javascript.flex.run.FlashRunConfigurationProducer;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSParameter;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.ecmal4.JSAttribute;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSImportHandlingUtil;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class FlexImplicitUsageProvider implements ImplicitUsageProvider, Condition<PsiElement> {
  @Override
  public boolean isImplicitUsage(@NotNull PsiElement element) {
    if (element instanceof XmlAttribute &&
        ((XmlAttribute)element).isNamespaceDeclaration() &&
        JavaScriptSupportLoader.isLanguageNamespace(((XmlAttribute)element).getValue())) {
      return true;
    }

    if (!DialectDetector.isActionScript(element)) return false;

    if (element instanceof JSClass) {
      JSClass clazz = (JSClass)element;
      final Module module = ModuleUtilCore.findModuleForPsiElement(clazz);
      if (module == null || ModuleType.get(module) != FlexModuleType.getInstance()) return false;
      if (FlashRunConfigurationProducer.isAcceptedMainClass(clazz, module)) return true;
      if (ActionScriptClassResolver.isParentClass(clazz, FlashRunConfigurationForm.MODULE_BASE_CLASS_NAME)) return true;

      FlexUnitSupport flexUnitSupport = FlexUnitSupport.getSupport(module);
      if (flexUnitSupport != null && flexUnitSupport.isTestClass(clazz, true)) return true;
    }
    else if (element instanceof JSFunction) {
      if (isTestMethod((JSFunction)element)) return true;
      if (isAnnotatedByUnknownAttribute((JSAttributeListOwner)element)) return true;
    }
    else if (element instanceof JSVariable) {
      if (isAnnotatedByUnknownAttribute((JSAttributeListOwner)element)) return true;

      if (JSResolveUtil.findParent(element) instanceof JSClass) {
        final JSAttributeList varAttrList = ((JSVariable)element).getAttributeList();
        if (varAttrList != null && varAttrList.findAttributeByName(FlexAnnotationNames.EMBED) != null) {
          return true;
        }
      }
    }

    if (element instanceof JSParameter) {
      JSFunction function = PsiTreeUtil.getParentOfType(element, JSFunction.class);
      if (function != null) {
        final JSParameter[] params = function.getParameterVariables();

        if (params.length == 1 && element == params[0]) {
          JSType jsType = ((JSParameter)element).getJSType();
          String type = jsType == null ? null : jsType.getTypeText();
          if (type != null) type = JSImportHandlingUtil.resolveTypeName(type, element);

          if (type != null) {
            if (FlexCommonTypeNames.FLASH_EVENT_FQN.equals(type) ||
                FlexCommonTypeNames.STARLING_EVENT_FQN.equals(type)) {
              return true;
            }

            boolean b = JSResolveUtil.processHierarchy(type, element.getContainingFile(),
                                                       jsClass -> !FlexCommonTypeNames.FLASH_EVENT_FQN.equals(jsClass.getQualifiedName()) &&
                                                                                                         !FlexCommonTypeNames.STARLING_EVENT_FQN.equals(jsClass.getQualifiedName()), false);
            if (!b) return true;
          }
        }
      }
    }

    return false;
  }

  private static boolean isTestMethod(JSFunction function) {
    Module moduleForPsiElement = ModuleUtilCore.findModuleForPsiElement(function);
    FlexUnitSupport flexUnitSupport = FlexUnitSupport.getSupport(moduleForPsiElement);
    if (flexUnitSupport != null && flexUnitSupport.isTestMethod(function)) return true;
    return false;
  }

  @Override
  public boolean isImplicitRead(@NotNull PsiElement element) {
    return false;
  }

  @Override
  public boolean isImplicitWrite(@NotNull PsiElement element) {
    return false;
  }

  // for "Can't be static" extension
  @Override
  public boolean value(PsiElement psiNamedElement) {
    if (psiNamedElement instanceof JSFunction) {
      if (isTestMethod((JSFunction)psiNamedElement)) return true;
      if (isAnnotatedByUnknownAttribute((JSAttributeListOwner)psiNamedElement)) return true;
    }

    return false;
  }

  private static boolean isAnnotatedByUnknownAttribute(JSAttributeListOwner namedElement) {
    JSAttributeList attributeList = namedElement.getAttributeList();
    if (attributeList != null) {
      JSAttribute[] attributes = attributeList.getAttributes();
      for (JSAttribute a : attributes) {
        if ("Deprecated".equals(a.getName())) continue;
        return true;
      }
    }

    return false;
  }
}
