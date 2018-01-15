package com.intellij.javascript.flex.documentation;

import com.intellij.lang.actionscript.psi.impl.ActionScriptFunctionImpl;
import com.intellij.lang.actionscript.psi.impl.ActionScriptVariableImpl;
import com.intellij.lang.javascript.documentation.JSQuickNavigateBuilder;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSQualifiedName;
import com.intellij.lang.javascript.psi.ecmal4.*;
import com.intellij.lang.javascript.psi.resolve.ActionScriptResolveUtil;
import com.intellij.lang.javascript.psi.types.JSTypeSubstitutor;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class FlexQuickNavigateBuilder extends JSQuickNavigateBuilder {

  @Override
  protected String getQuickNavigateInfoForNavigationElement(PsiElement element, PsiElement originalElement) {
    if (element instanceof JSNamespaceDeclaration) {
      return createQuickNavigateForNamespace((JSNamespaceDeclaration)element);
    }
    if (element instanceof JSAttributeNameValuePair) {
      return createQuickNavigateForAnnotationDerived(element);
    }

    return super.getQuickNavigateInfoForNavigationElement(element, originalElement);
  }

  @Override
  protected void appendParentInfo(PsiElement parent,
                                  @NotNull StringBuilder builder,
                                  @NotNull PsiNamedElement element,
                                  @NotNull JSTypeSubstitutor substitutor) {
    if (parent instanceof JSClass) {
      builder.append(((JSClass)parent).getQualifiedName()).append("\n");
    }
    else if (parent instanceof JSPackageStatement) {
      builder.append(((JSPackageStatement)parent).getQualifiedName()).append("\n");
    }
    else if (parent instanceof JSFile) {
      if (parent.getContext() != null) {
        final String mxmlPackage = ActionScriptResolveUtil.findPackageForMxml(parent);
        if (mxmlPackage != null) {
          builder.append(mxmlPackage).append(mxmlPackage.length() > 0 ? "." : "").append(parent.getContext().getContainingFile().getName())
            .append("\n");
        }
      }
      else {
        boolean foundQualified = false;

        if (element instanceof ActionScriptFunctionImpl && ((ActionScriptFunctionImpl)element).hasQualifiedName() ||
            element instanceof ActionScriptVariableImpl && ((ActionScriptVariableImpl)element).hasQualifiedName()) {
          final JSQualifiedName namespace = ((JSQualifiedNamedElement)element).getNamespace();
          assert namespace != null : "null namespace of element having qualified name";
          builder.append(namespace.getQualifiedName()).append("\n");
          foundQualified = true;
        }
        if (!foundQualified) builder.append(parent.getContainingFile().getName()).append("\n");
      }
    }
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

  private String createQuickNavigateForAnnotationDerived(final PsiElement element) {
    final JSAttributeNameValuePair valuePair = (JSAttributeNameValuePair)element;
    final JSAttribute parent = (JSAttribute)valuePair.getParent();
    final StringBuilder builder = new StringBuilder();
    final JSClass clazz = PsiTreeUtil.getParentOfType(valuePair, JSClass.class);
    appendParentInfo(clazz != null ? clazz : parent.getContainingFile(), builder, parent, JSTypeSubstitutor.EMPTY);
    builder.append(parent.getName()).append(" ").append(valuePair.getSimpleValue());
    return builder.toString();
  }
}
