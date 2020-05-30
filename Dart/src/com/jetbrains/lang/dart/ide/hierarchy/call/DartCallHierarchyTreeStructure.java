// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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

  @NotNull
  protected static FindUsagesHandler createFindUsageHandler(@NotNull final PsiElement element) {
    return new DartServerFindUsagesHandler(element);
  }

  public static void collectDeclarations(@Nullable final PsiElement element, @NotNull final List<? super PsiElement> results) {
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

  @NotNull
  protected abstract List<PsiElement> getChildren(@NotNull PsiElement element);

  @Override
  protected Object @NotNull [] buildChildren(@NotNull HierarchyNodeDescriptor descriptor) {
    final List<DartCallHierarchyNodeDescriptor> descriptors = new ArrayList<>();
    if (descriptor instanceof DartCallHierarchyNodeDescriptor) {
      final DartCallHierarchyNodeDescriptor dartDescriptor = (DartCallHierarchyNodeDescriptor)descriptor;
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

  @NotNull
  protected GlobalSearchScope getScope() {
    if (HierarchyBrowserBaseEx.getScopeClass().equals(myScopeType)) {
      Object root = getRootElement();
      if (root instanceof DartCallHierarchyNodeDescriptor) {
        DartCallHierarchyNodeDescriptor rootElement = (DartCallHierarchyNodeDescriptor)root;
        PsiElement element = rootElement.getPsiElement();
        DartFile file = PsiTreeUtil.getParentOfType(element, DartFile.class);
        if (file != null) {
          return GlobalSearchScope.fileScope(file);
        }
      }
      return GlobalSearchScope.projectScope(myProject);
    }
    else if (HierarchyBrowserBaseEx.getScopeProject().equals(myScopeType)) {
      return GlobalSearchScope.projectScope(myProject);
    }
    else if (HierarchyBrowserBaseEx.getScopeTest().equals(myScopeType)) {
      return GlobalSearchScope.projectScope(myProject); // We do not have a module to get its test scope.
    }
    else if (HierarchyBrowserBaseEx.getScopeAll().equals(myScopeType)) {
      return GlobalSearchScope.allScope(myProject);
    }
    return GlobalSearchScope.projectScope(myProject);
  }
}
