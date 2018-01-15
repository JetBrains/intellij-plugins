package com.intellij.javascript.flex.documentation;

import com.intellij.lang.actionscript.psi.impl.ActionScriptFunctionImpl;
import com.intellij.lang.actionscript.psi.impl.ActionScriptVariableImpl;
import com.intellij.lang.javascript.documentation.JSQuickNavigateBuilder;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSQualifiedName;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSPackageStatement;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.resolve.ActionScriptResolveUtil;
import com.intellij.lang.javascript.psi.types.JSTypeSubstitutor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import org.jetbrains.annotations.NotNull;

class FlexQuickNavigateBuilder extends JSQuickNavigateBuilder {

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

}
