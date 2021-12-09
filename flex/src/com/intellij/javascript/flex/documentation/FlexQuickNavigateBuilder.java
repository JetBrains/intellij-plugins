package com.intellij.javascript.flex.documentation;

import com.intellij.lang.actionscript.psi.ActionScriptPsiImplUtil;
import com.intellij.lang.actionscript.psi.impl.ActionScriptFunctionImpl;
import com.intellij.lang.actionscript.psi.impl.ActionScriptVariableImpl;
import com.intellij.lang.javascript.documentation.JSQuickNavigateBuilder;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.*;
import com.intellij.lang.javascript.psi.resolve.ActionScriptResolveUtil;
import com.intellij.lang.javascript.psi.types.JSTypeSubstitutor;
import com.intellij.lang.javascript.refactoring.JSVisibilityUtil;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class FlexQuickNavigateBuilder extends JSQuickNavigateBuilder {

  @Override
  public @Nullable @NlsSafe String getQuickNavigateInfoForNavigationElement(@NotNull PsiElement element,
                                                                            @NotNull PsiElement originalElement,
                                                                            boolean jsDoc) {
    if (element instanceof JSNamespaceDeclaration) {
      return createQuickNavigateForNamespace((JSNamespaceDeclaration)element);
    }
    if (element instanceof JSAttributeNameValuePair) {
      return createQuickNavigateForAnnotationDerived(element, originalElement);
    }

    return super.getQuickNavigateInfoForNavigationElement(element, originalElement, jsDoc);
  }

  @Nullable
  @Override
  protected String getParentInfo(PsiElement parent,
                                 @NotNull PsiNamedElement element,
                                 @NotNull JSTypeSubstitutor substitutor) {
    if (parent instanceof JSClass) {
      return ((JSClass)parent).getQualifiedName();
    }
    else if (parent instanceof JSPackageStatement) {
      return ((JSPackageStatement)parent).getQualifiedName();
    }
    else if (parent instanceof JSFile) {
      if (parent.getContext() != null) {
        final String mxmlPackage = ActionScriptResolveUtil.findPackageForMxml(parent);
        if (mxmlPackage != null) {
          return mxmlPackage + (mxmlPackage.length() > 0 ? "." : "") + parent.getContext().getContainingFile().getName();
        }
      }
      else {

        if (element instanceof ActionScriptFunctionImpl && ((ActionScriptFunctionImpl)element).hasQualifiedName() ||
            element instanceof ActionScriptVariableImpl && ((ActionScriptVariableImpl)element).hasQualifiedName()) {
          final JSQualifiedName namespace = ((JSQualifiedNamedElement)element).getNamespace();
          assert namespace != null : "null namespace of element having qualified name";
          return namespace.getQualifiedName();
        }
      }
    }

    return null;
  }

  @Nullable
  private static String createQuickNavigateForNamespace(final JSNamespaceDeclaration ns) {
    final String qName = ns.getQualifiedName();
    if (qName == null) return null;
    StringBuilder result = new StringBuilder();
    String packageName = StringUtil.getPackageName(qName);
    if (packageName.length() > 0) result.append(packageName).append("\n");

    result.append("namespace");

    final String name = ns.getName();
    result.append(" ").append(name);

    String s = ns.getInitialValueString();
    if (s != null) {
      result.append(" = ").append(s);
    }
    return result.toString();
  }

  private String createQuickNavigateForAnnotationDerived(final PsiElement element, PsiElement originalElement) {
    final JSAttributeNameValuePair valuePair = (JSAttributeNameValuePair)element;
    final JSAttribute parent = (JSAttribute)valuePair.getParent();
    final StringBuilder builder = new StringBuilder();
    final JSClass clazz = PsiTreeUtil.getParentOfType(valuePair, JSClass.class);
    PsiElement realParent = clazz != null ? clazz : parent.getContainingFile();
    String info = getParentInfo(realParent, parent, JSTypeSubstitutor.EMPTY);
    if (!StringUtil.isEmpty(info)) {
      builder.append(info).append("\n");
    }
    builder.append(parent.getName()).append(" ").append(valuePair.getSimpleValue());
    return buildResult(ObjectKind.SIMPLE_DECLARATION, builder.toString(), element, originalElement);
  }

  @Override
  protected void appendAttrList(@NotNull JSAttributeListOwner owner, @NotNull StringBuilder result) {
    JSAttributeList attributeList = owner.getAttributeList();
    if (attributeList == null) return;
    
    appendModifierWithSpace(result, attributeList, JSAttributeList.ModifierType.OVERRIDE);

    String nsOrAccessModifier = ActionScriptPsiImplUtil.getNamespaceValue(attributeList);
    if (nsOrAccessModifier == null) {
      JSVisibilityUtil.PresentableAccessModifier modifier = JSVisibilityUtil.getPresentableAccessModifier(owner);
      if (modifier != null) nsOrAccessModifier = modifier.getText();
    }

    if (nsOrAccessModifier != null) {
      result.append(nsOrAccessModifier);
      result.append(" ");
    }

    appendPlainModifierList(attributeList, result);
  }

  @Override
  protected void appendPlainModifierList(@NotNull JSAttributeList attributeList, @NotNull StringBuilder result) {
    appendModifierWithSpace(result, attributeList, JSAttributeList.ModifierType.STATIC);
    appendModifierWithSpace(result, attributeList, JSAttributeList.ModifierType.FINAL);
    appendModifierWithSpace(result, attributeList, JSAttributeList.ModifierType.DYNAMIC);
    appendModifierWithSpace(result, attributeList, JSAttributeList.ModifierType.NATIVE);
  }

  @Override
  protected boolean isIncludeObjectInExtendsList() {
    return true;
  }

  @Nullable
  @Override
  protected  JSType getJSElementType(@NotNull JSElement element, @NotNull PsiElement originalElement) {
    return null;
  }

  @Override
  protected boolean shouldAppendFunctionKeyword(@NotNull JSFunctionItem function, @Nullable PsiElement parent) {
    return parent instanceof JSClass || super.shouldAppendFunctionKeyword(function, parent);
  }

  @Override
  protected JSType getVariableOrFieldType(@NotNull JSTypeDeclarationOwner variable) {
    return variable.getJSType();
  }
}
