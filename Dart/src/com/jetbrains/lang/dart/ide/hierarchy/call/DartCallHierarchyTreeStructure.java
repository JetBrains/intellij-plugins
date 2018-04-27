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
    super(project, new DartHierarchyNodeDescriptor(null, element, true));
    myScopeType = currentScopeType;
  }

  @NotNull
  protected static FindUsagesHandler createFindUsageHandler(@NotNull final PsiElement element) {
    return new DartServerFindUsagesHandler(element);
  }

  public static void collectDeclarations(@Nullable final PsiElement element, @NotNull final List<PsiElement> results) {
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

  @NotNull
  @Override
  protected Object[] buildChildren(@NotNull HierarchyNodeDescriptor descriptor) {
    final List<DartHierarchyNodeDescriptor> descriptors = new ArrayList<>();
    if (descriptor instanceof DartHierarchyNodeDescriptor) {
      final DartHierarchyNodeDescriptor dartDescriptor = (DartHierarchyNodeDescriptor)descriptor;
      PsiElement element = dartDescriptor.getPsiElement();
      if (element == null) {
        return ArrayUtil.EMPTY_OBJECT_ARRAY;
      }
      boolean isCallable = DartHierarchyUtil.isExecutable(element);
      HierarchyNodeDescriptor nodeDescriptor = getBaseDescriptor();
      if (!(element instanceof DartComponent) || !isCallable || nodeDescriptor == null) {
        return ArrayUtil.EMPTY_OBJECT_ARRAY;
      }
      DartComponentName name = ((DartComponent)element).getComponentName();
      if (name == null) {
        return ArrayUtil.EMPTY_OBJECT_ARRAY;
      }

      final List<PsiElement> children = getChildren(name);
      final HashMap<PsiElement, DartHierarchyNodeDescriptor> callerToDescriptorMap = new HashMap<>();
      PsiElement baseClass = element instanceof DartMethodDeclaration ? PsiTreeUtil.getParentOfType(name, DartClass.class) : null;

      for (PsiElement caller : children) {
        if (isInScope(baseClass, caller, myScopeType)) {
          DartHierarchyNodeDescriptor callerDescriptor = callerToDescriptorMap.get(caller);
          if (callerDescriptor == null) {
            callerDescriptor = new DartHierarchyNodeDescriptor(descriptor, caller, false);
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
    if (HierarchyBrowserBaseEx.SCOPE_CLASS.equals(myScopeType)) {
      Object root = getRootElement();
      if (root instanceof DartHierarchyNodeDescriptor) {
        DartHierarchyNodeDescriptor rootElement = (DartHierarchyNodeDescriptor)root;
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
