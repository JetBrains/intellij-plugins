package com.intellij.lang.javascript.flex.flexunit;

import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.testIntegration.TestFinder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class FlexUnitTestFinder implements TestFinder {
  public JSClass findSourceElement(@NotNull final PsiElement element) {
    if (FlexUnitSupport.getModuleAndSupport(element) == null) {
      return null;
    }

    PsiFile psiFile = PsiTreeUtil.getParentOfType(element, PsiFile.class);
    if (psiFile instanceof JSFile) {
      final PsiElement context = psiFile.getContext();
      if (context != null) {
        psiFile = context.getContainingFile();
      }
    }

    if (psiFile instanceof JSFile) {
      return JSPsiImplUtils.findClass((JSFile)psiFile);
    }
    else if (psiFile instanceof XmlFile) {
      return XmlBackedJSClassImpl.getXmlBackedClass((XmlFile)psiFile);
    }

    return null;
  }

  @NotNull
  public Collection<PsiElement> findTestsForClass(@NotNull final PsiElement element) {
    final JSClass jsClass = findSourceElement(element);
    final Module module = ModuleUtil.findModuleForPsiElement(element);
    if (jsClass == null || module == null) {
      return Collections.emptyList();
    }

    final String[] testPossibleNames = {jsClass.getName() + "Test", "Test" + jsClass.getName()};

    final GlobalSearchScope scope = GlobalSearchScope.moduleTestsWithDependentsScope(module);
    final Collection<PsiElement> result = new ArrayList<PsiElement>();

    for (final String testName : testPossibleNames) {
      final Collection<JSQualifiedNamedElement> elements = JSResolveUtil.findElementsByName(testName, element.getProject(), scope);
      for (final JSQualifiedNamedElement jsQualifiedNamedElement : elements) {
        if (jsQualifiedNamedElement instanceof JSClass) {
          result.add(jsQualifiedNamedElement);
        }
      }
    }

    return result;
  }

  @NotNull
  public Collection<PsiElement> findClassesForTest(@NotNull final PsiElement element) {
    final JSClass jsClass = findSourceElement(element);
    final Module module = ModuleUtil.findModuleForPsiElement(element);
    if (jsClass == null || module == null) {
      return Collections.emptyList();
    }

    String possibleName = null;
    final String name = jsClass.getName();
    if (name != null && name.length() > "Test".length()) {
      possibleName = name.startsWith("Test")
                     ? name.substring("Test".length())
                     : name.endsWith("Test") ? name.substring(0, name.length() - "Test".length()) : null;
    }

    if (possibleName == null) {
      return Collections.emptyList();
    }

    final GlobalSearchScope scope =
      GlobalSearchScope.moduleWithDependenciesScope(module).intersectWith(GlobalSearchScope.projectProductionScope(module.getProject()));
    final Collection<PsiElement> result = new ArrayList<PsiElement>();

    final Collection<JSQualifiedNamedElement> elements = JSResolveUtil.findElementsByName(possibleName, element.getProject(), scope);
    for (final JSQualifiedNamedElement jsQualifiedNamedElement : elements) {
      if (jsQualifiedNamedElement instanceof JSClass) {
        result.add(jsQualifiedNamedElement);
      }
    }

    return result;
  }

  public boolean isTest(@NotNull final PsiElement element) {
    final VirtualFile virtualFile = element.getContainingFile().getVirtualFile();
    final Module module = virtualFile == null ? null : ModuleUtil.findModuleForFile(virtualFile, element.getProject());
    if (module == null || !ModuleRootManager.getInstance(module).getFileIndex().isInTestSourceContent(virtualFile)) {
      return false;
    }

    final JSClass jsClass = findSourceElement(element);
    final String name = jsClass == null ? null : jsClass.getName();

    return name != null && name.length() > "Test".length() && (name.startsWith("Test") || name.endsWith("Test"));
  }
}
