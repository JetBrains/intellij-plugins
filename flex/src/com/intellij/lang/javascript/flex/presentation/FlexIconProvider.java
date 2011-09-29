package com.intellij.lang.javascript.flex.presentation;

import com.intellij.ide.IconProvider;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.psi.JSNamedElement;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.impl.JSFileImpl;
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

/**
 * User: Maxim
 * Date: 16.05.2010
 * Time: 18:38:05
 */
public class FlexIconProvider extends IconProvider {

  @Override
  public Icon getIcon(@NotNull PsiElement element, int flags) {
    int transformedFlags = ElementBase.transformFlags(element, flags);

    Icon icon = null;

    if (element instanceof XmlFile) {
      if (JavaScriptSupportLoader.isFlexMxmFile((PsiFile)element)) {
        final JSClass jsClass = XmlBackedJSClassImpl.getXmlBackedClass((XmlFile)element);
        if (jsClass != null) {
          icon = jsClass.getIcon(flags);
        }
      }
    } else if (element instanceof JSFileImpl) {
      final JSNamedElement mainDeclaredElement = JSFileImpl.findMainDeclaredElement((JSFileImpl)element);
      if (mainDeclaredElement != null) {
        icon = mainDeclaredElement.getIcon(transformedFlags);
      }
    }

    if (icon != null) {
      final PsiFile psiFile = element.getContainingFile();
      final VirtualFile vFile = psiFile == null ? null : psiFile.getVirtualFile();
      CompilerManager compilerManager = CompilerManager.getInstance(element.getProject());
      if (vFile != null && compilerManager != null && compilerManager.isExcludedFromCompilation(vFile)) {
        icon = new LayeredIcon(icon, PlatformIcons.EXCLUDED_FROM_COMPILE_ICON);
      }
    }

    return icon;
  }

}
