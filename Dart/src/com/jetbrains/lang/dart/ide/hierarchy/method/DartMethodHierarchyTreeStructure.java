// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.hierarchy.method;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.intellij.ide.hierarchy.HierarchyBrowserManager;
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.ide.hierarchy.HierarchyTreeStructure;
import com.intellij.openapi.project.Project;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtilRt;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import org.dartlang.analysis.server.protocol.TypeHierarchyItem;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

import static com.jetbrains.lang.dart.ide.hierarchy.DartHierarchyUtil.findDartClass;
import static com.jetbrains.lang.dart.ide.hierarchy.DartHierarchyUtil.getTypeHierarchyItems;

public class DartMethodHierarchyTreeStructure extends HierarchyTreeStructure {
  private final SmartPsiElementPointer myMethod;

  public DartMethodHierarchyTreeStructure(Project project, DartComponent element) {
    super(project, null);
    DartClass baseClass = PsiTreeUtil.getParentOfType(element, DartClass.class);
    myBaseDescriptor = new DartMethodHierarchyNodeDescriptor(project, null, baseClass, true, this);
    setBaseElement(myBaseDescriptor);
    ((DartMethodHierarchyNodeDescriptor)myBaseDescriptor).setTreeStructure(this);
    myMethod = SmartPointerManager.getInstance(myProject).createSmartPsiElementPointer(element);
  }

  @NotNull
  @Override
  protected Object[] buildChildren(@NotNull HierarchyNodeDescriptor descript) {
    final DartMethodHierarchyNodeDescriptor descriptor = (DartMethodHierarchyNodeDescriptor)descript;
    final DartClass dartClass = descriptor.getType();
    if (dartClass == null) return ArrayUtilRt.EMPTY_OBJECT_ARRAY;

    final List<TypeHierarchyItem> items = getTypeHierarchyItems(dartClass);
    if (items.isEmpty()) return ArrayUtilRt.EMPTY_OBJECT_ARRAY;

    addAllVisibleSubclasses(Sets.newHashSet(), myProject, items, items.get(0), descriptor);
    return descriptor.getCachedChildren();
  }

  private void addAllVisibleSubclasses(@NotNull final Set<TypeHierarchyItem> stackItems,
                                       @NotNull final Project project,
                                       @NotNull final List<TypeHierarchyItem> items,
                                       @NotNull final TypeHierarchyItem item,
                                       @NotNull final DartMethodHierarchyNodeDescriptor descriptor) {
    if (!stackItems.add(item)) {
      descriptor.setCachedChildren(ArrayUtilRt.EMPTY_OBJECT_ARRAY);
      return;
    }
    HierarchyBrowserManager.State state = HierarchyBrowserManager.getInstance(myProject).getState();
    if (state == null) throw new NullPointerException();

    List<DartMethodHierarchyNodeDescriptor> subDescriptors = Lists.newArrayList();
    try {
      for (int index : item.getSubclasses()) {
        final TypeHierarchyItem subItem = items.get(index);
        final DartClass subclass = findDartClass(project, subItem);
        if (subclass != null) {
          final DartMethodHierarchyNodeDescriptor subDescriptor =
            new DartMethodHierarchyNodeDescriptor(project, descriptor, subclass, false, this);
          subDescriptors.add(subDescriptor);
          addAllVisibleSubclasses(stackItems, project, items, subItem, subDescriptor);
        }
      }
      DartClass dartClass = findDartClass(project, item);
      assert dartClass != null;
      String methodName = getBaseMethod().getName();
      if (methodName != null) {
        DartComponent method = dartClass.findMethodByName(methodName);
        if (method != null) {
          DartClass definingClass = PsiTreeUtil.getParentOfType(method, DartClass.class);
          if (definingClass == dartClass) {
            descriptor.myIsImplementor = true;
          }
          else {
            descriptor.myShouldImplement = method.isAbstract() && !dartClass.isAbstract();
          }
        }
      }
      for (DartMethodHierarchyNodeDescriptor subDescriptor : subDescriptors) {
        if (subDescriptor.myIsSuperclassOfImplementor || subDescriptor.myIsImplementor) {
          descriptor.myIsSuperclassOfImplementor = true;
          break;
        }
      }
      if (state.HIDE_CLASSES_WHERE_METHOD_NOT_IMPLEMENTED) {
        List<DartMethodHierarchyNodeDescriptor> toRemove = Lists.newArrayList();
        for (DartMethodHierarchyNodeDescriptor subDescriptor : subDescriptors) {
          if (!(subDescriptor.myIsSuperclassOfImplementor || subDescriptor.myIsImplementor)) {
            toRemove.add(subDescriptor);
          }
        }
        subDescriptors.removeAll(toRemove);
      }
    }
    finally {
      stackItems.remove(item);
    }
    descriptor.setCachedChildren(subDescriptors.toArray(new HierarchyNodeDescriptor[0]));
  }

  private DartComponent getBaseMethod() {
    return (DartComponent)myMethod.getElement();
  }
}
