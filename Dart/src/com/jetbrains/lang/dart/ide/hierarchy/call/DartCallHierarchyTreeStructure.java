// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.hierarchy.call;

import com.intellij.find.findUsages.FindUsagesHandler;
import com.intellij.ide.hierarchy.HierarchyBrowserBaseEx;
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.ide.hierarchy.HierarchyTreeStructure;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ArrayUtilRt;
import com.jetbrains.lang.dart.ide.findUsages.DartServerFindUsagesHandler;
import com.jetbrains.lang.dart.ide.hierarchy.DartHierarchyUtil;
import com.jetbrains.lang.dart.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class DartCallHierarchyTreeStructure extends HierarchyTreeStructure {
  private final String myScopeType;

  public DartCallHierarchyTreeStructure(Project project, PsiElement element, String currentScopeType) {
    super(project, new DartCallHierarchyNodeDescriptor(null, element, true));
    myScopeType = currentScopeType;
  }

  protected static @NotNull FindUsagesHandler createFindUsageHandler(final @NotNull PsiElement element) {
    return new DartServerFindUsagesHandler(element);
  }

  public static void collectDeclarations(final @Nullable PsiElement element, final @NotNull List<? super PsiElement> results) {
    if (element != null) {
      Condition<PsiElement> isExecutable = object -> {
        if (object == null) return false;
        return DartHierarchyUtil.isExecutable(object);
      };
      PsiElement ref = PsiTreeUtil.findFirstParent(element, isExecutable);
      if (ref != null) {
        results.add(ref);
      }
    }
  }

  protected abstract @NotNull List<PsiElement> getChildren(@NotNull PsiElement element);

  @Override
  protected Object @NotNull [] buildChildren(@NotNull HierarchyNodeDescriptor descriptor) {
    final List<DartCallHierarchyNodeDescriptor> descriptors = new ArrayList<>();
    if (descriptor instanceof DartCallHierarchyNodeDescriptor dartDescriptor) {
      PsiElement element = dartDescriptor.getPsiElement();
      if (element == null) {
        return ArrayUtilRt.EMPTY_OBJECT_ARRAY;
      }
      boolean isCallable = DartHierarchyUtil.isExecutable(element);
      HierarchyNodeDescriptor nodeDescriptor = getBaseDescriptor();
      if (!(element instanceof DartComponent) || !isCallable || nodeDescriptor == null) {
        return ArrayUtilRt.EMPTY_OBJECT_ARRAY;
      }
      DartComponentName name = ((DartComponent)element).getComponentName();
      if (name == null) {
        return ArrayUtilRt.EMPTY_OBJECT_ARRAY;
      }

      final List<PsiElement> children = getChildren(name);
      final HashMap<PsiElement, DartCallHierarchyNodeDescriptor> callerToDescriptorMap = new HashMap<>();
      PsiElement baseClass = element instanceof DartMethodDeclaration ? PsiTreeUtil.getParentOfType(name, DartClass.class) : null;

      for (PsiElement caller : children) {
        if (isInScope(baseClass, caller, myScopeType)) {
          DartCallHierarchyNodeDescriptor callerDescriptor = callerToDescriptorMap.get(caller);
          if (callerDescriptor == null) {
            callerDescriptor = new DartCallHierarchyNodeDescriptor(descriptor, caller, false);
            callerToDescriptorMap.put(caller, callerDescriptor);
            descriptors.add(callerDescriptor);
          }
        }
      }
    }
    return ArrayUtil.toObjectArray(descriptors);
  }

  protected @NotNull GlobalSearchScope getScope() {
    if (HierarchyBrowserBaseEx.SCOPE_CLASS.equals(myScopeType)) {
      Object root = getRootElement();
      if (root instanceof DartCallHierarchyNodeDescriptor rootElement) {
        PsiElement element = rootElement.getPsiElement();
        DartFile file = PsiTreeUtil.getParentOfType(element, DartFile.class);
        if (file != null) {
          return GlobalSearchScope.fileScope(file);
        }
      }
      return GlobalSearchScope.projectScope(myProject);
    }
    else if (HierarchyBrowserBaseEx.SCOPE_PROJECT.equals(myScopeType)) {
      return GlobalSearchScope.projectScope(myProject);
    }
    else if (HierarchyBrowserBaseEx.SCOPE_TEST.equals(myScopeType)) {
      return GlobalSearchScope.projectScope(myProject); // We do not have a module to get its test scope.
    }
    else if (HierarchyBrowserBaseEx.SCOPE_ALL.equals(myScopeType)) {
      return GlobalSearchScope.allScope(myProject);
    }
    return GlobalSearchScope.projectScope(myProject);
  }
}
