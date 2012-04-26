package com.intellij.lang.javascript.flex.flexunit;

import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.stubs.JSNameIndex;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopes;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.testIntegration.TestFinder;
import com.intellij.testIntegration.TestFinderHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class FlexUnitTestFinder implements TestFinder {

  @Nullable
  public JSClass findSourceElement(@NotNull final PsiElement element) {
    return findContextClass(element);
  }

  @Nullable
  static JSClass findContextClass(final @NotNull PsiElement element) {
    if (FlexUnitSupport.getModuleAndSupport(element) == null) {
      return null;
    }

    PsiFile psiFile = PsiTreeUtil.getParentOfType(element, PsiFile.class, false);
    if (psiFile instanceof JSFile) {
      psiFile = InjectedLanguageUtil.getTopLevelFile(psiFile);
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
    final String className = jsClass == null ? null : jsClass.getName();

    final Pair<Module, FlexUnitSupport> moduleAndSupport = FlexUnitSupport.getModuleAndSupport(element);
    final Module module = moduleAndSupport == null ? null : moduleAndSupport.first;
    final FlexUnitSupport flexUnitSupport = moduleAndSupport == null ? null : moduleAndSupport.second;

    if (className == null || module == null || flexUnitSupport == null) {
      return Collections.emptyList();
    }

    final Collection<String> allNames = StubIndex.getInstance().getAllKeys(JSNameIndex.KEY, element.getProject());
    final GlobalSearchScope scope = GlobalSearchScope.moduleWithDependentsScope(module);
    final List<Pair<? extends PsiNamedElement, Integer>> classesWithProximity = new ArrayList<Pair<? extends PsiNamedElement, Integer>>();

    for (final String possibleTestName : allNames) {
      if (possibleTestName.contains(className)) {
        for (final JSQualifiedNamedElement jsElement : JSResolveUtil.findElementsByName(possibleTestName, element.getProject(), scope)) {
          if (jsElement instanceof JSClass && flexUnitSupport.isTestClass((JSClass)jsElement, true)) {
            classesWithProximity.add(Pair.create(jsElement, TestFinderHelper.calcTestNameProximity(className, jsElement.getName())));
          }
        }
      }
    }

    return TestFinderHelper.getSortedElements(classesWithProximity, true);
  }

  @NotNull
  public Collection<PsiElement> findClassesForTest(@NotNull final PsiElement element) {
    final JSClass jsClass = findSourceElement(element);
    final String className = jsClass == null ? null : jsClass.getName();

    final Pair<Module, FlexUnitSupport> moduleAndSupport = FlexUnitSupport.getModuleAndSupport(element);
    final Module module = moduleAndSupport == null ? null : moduleAndSupport.first;
    final FlexUnitSupport flexUnitSupport = moduleAndSupport == null ? null : moduleAndSupport.second;

    if (className == null || module == null || flexUnitSupport == null) {
      return Collections.emptyList();
    }

    final GlobalSearchScope scope =
      GlobalSearchScope.moduleWithDependenciesScope(module).intersectWith(GlobalSearchScopes.projectProductionScope(module.getProject()));

    final List<Pair<? extends PsiNamedElement, Integer>> classesWithWeights = new ArrayList<Pair<? extends PsiNamedElement, Integer>>();
    for (Pair<String, Integer> nameWithWeight : TestFinderHelper.collectPossibleClassNamesWithWeights(className)) {
      for (final JSQualifiedNamedElement jsElement : JSResolveUtil.findElementsByName(nameWithWeight.first, module.getProject(), scope)) {
        if (jsElement instanceof JSClass && !((JSClass)jsElement).isInterface() && !flexUnitSupport.isTestClass((JSClass)jsElement, true)) {
          classesWithWeights.add(new Pair<JSQualifiedNamedElement, Integer>(jsElement, nameWithWeight.second));
        }
      }
    }

    return TestFinderHelper.getSortedElements(classesWithWeights, false);
  }

  public boolean isTest(@NotNull final PsiElement element) {
    final JSClass jsClass = findSourceElement(element);
    final Pair<Module, FlexUnitSupport> moduleAndSupport = FlexUnitSupport.getModuleAndSupport(element);
    return jsClass != null && moduleAndSupport != null && moduleAndSupport.second.isTestClass(jsClass, true);
  }
}
