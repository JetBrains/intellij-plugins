// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.editor;

import com.intellij.codeInsight.navigation.actions.TypeDeclarationProvider;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.html.HtmlFileImpl;
import org.angular2.Angular2DecoratorUtil;
import org.angular2.codeInsight.metadata.AngularDirectiveMetadata;
import org.angularjs.codeInsight.DirectiveUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Angular2TypeDeclarationProvider implements TypeDeclarationProvider {

  @Nullable
  @Override
  public PsiElement[] getSymbolTypeDeclarations(@NotNull PsiElement symbol) {
    if (DirectiveUtil.isComponentDeclaration(symbol)) {
      JSClass cls = AngularDirectiveMetadata.create((JSImplicitElement)symbol).getDirectiveClass();
      if (cls instanceof TypeScriptClass) {
        HtmlFileImpl htmlFile = Angular2DecoratorUtil.findAngularComponentTemplate((TypeScriptClass)cls);
        if (htmlFile != null) {
          return new PsiElement[]{htmlFile};
        }
      }
    }
    return null;
  }

  @Nullable
  @Override
  public String getActionText(@NotNull DataContext context) {
    if (DirectiveUtil.isComponentDeclaration(context.getData(CommonDataKeys.PSI_ELEMENT))) {
      return "Component &Template";
    }
    return null;
  }
}
