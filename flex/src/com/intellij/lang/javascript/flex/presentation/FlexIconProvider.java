// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.presentation;

import com.intellij.ide.IconProvider;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.psi.JSNamedElement;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.XmlBackedJSClassFactory;
import com.intellij.lang.javascript.psi.impl.JSFileImpl;
import com.intellij.lang.javascript.psi.resolve.ActionScriptResolveUtil;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.ElementBase;
import com.intellij.psi.xml.XmlFile;
import com.intellij.ui.LayeredIcon;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

final class FlexIconProvider extends IconProvider {
  @Override
  public Icon getIcon(@NotNull PsiElement element, int flags) {
    Icon icon = null;

    if (element instanceof XmlFile) {
      if (JavaScriptSupportLoader.isFlexMxmFile((PsiFile)element)) {
        final JSClass jsClass = XmlBackedJSClassFactory.getXmlBackedClass((XmlFile)element);
        if (jsClass != null) {
          icon = jsClass.getIcon(flags);
        }
      }
    }
    else if (element instanceof JSFileImpl) {
      final JSNamedElement mainDeclaredElement = ActionScriptResolveUtil.findMainDeclaredElement((JSFileImpl)element);
      if (mainDeclaredElement != null) {
        int transformedFlags = ElementBase.transformFlags(element, flags);
        icon = mainDeclaredElement.getIcon(transformedFlags);
      }
    }

    if (icon != null) {
      final PsiFile psiFile = element.getContainingFile();
      final VirtualFile vFile = psiFile == null ? null : psiFile.getVirtualFile();
      CompilerManager compilerManager = CompilerManager.getInstance(element.getProject());
      if (vFile != null && compilerManager != null && compilerManager.isExcludedFromCompilation(vFile)) {
        icon = LayeredIcon.layeredIcon(new Icon[]{icon, PlatformIcons.EXCLUDED_FROM_COMPILE_ICON});
      }
    }

    return icon;
  }

}
