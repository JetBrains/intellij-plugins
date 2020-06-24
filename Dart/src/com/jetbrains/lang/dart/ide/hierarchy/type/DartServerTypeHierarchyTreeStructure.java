// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.hierarchy.type;

import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.ide.hierarchy.HierarchyTreeStructure;
import com.intellij.openapi.project.Project;
import com.intellij.util.ArrayUtilRt;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.dartlang.analysis.server.protocol.TypeHierarchyItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.jetbrains.lang.dart.ide.hierarchy.DartHierarchyUtil.findDartClass;
import static com.jetbrains.lang.dart.ide.hierarchy.DartHierarchyUtil.getTypeHierarchyItems;
import static com.jetbrains.lang.dart.ide.hierarchy.type.DartServerSubtypesHierarchyTreeStructure.addSubClassHierarchy;

public final class DartServerTypeHierarchyTreeStructure extends HierarchyTreeStructure {
  private final String myCurrentScopeType;

  public DartServerTypeHierarchyTreeStructure(final Project project, final DartClass dartClass, String currentScopeType) {
    super(project, buildHierarchyElement(project, dartClass));
    myCurrentScopeType = currentScopeType;
    //super(project, buildHierarchyElement(project, dartClass), currentScopeType);
    setBaseElement(myBaseDescriptor); //to set myRoot
  }

  @Override
  protected Object @NotNull [] buildChildren(@NotNull HierarchyNodeDescriptor descriptor) {
    return ArrayUtilRt.EMPTY_OBJECT_ARRAY;
  }

  @NotNull
  private static HierarchyNodeDescriptor buildHierarchyElement(@NotNull final Project project, @NotNull final DartClass dartClass) {
    if (DartResolveUtil.OBJECT.equals(dartClass.getName())) {
      return new DartTypeHierarchyNodeDescriptor(project, null, dartClass, true);
    }

    final List<TypeHierarchyItem> items = getTypeHierarchyItems(dartClass);
    final HierarchyNodeDescriptor superDescriptor = buildSuperClassHierarchy(project, items);
    final HierarchyNodeDescriptor baseDescriptor = new DartTypeHierarchyNodeDescriptor(project, superDescriptor, dartClass, true);
    if (superDescriptor != null) {
      superDescriptor.setCachedChildren(new HierarchyNodeDescriptor[]{baseDescriptor});
    }
    if (!items.isEmpty()) {
      addSubClassHierarchy(new HashSet<TypeHierarchyItem>(), project, items, items.get(0), baseDescriptor);
    }

    return baseDescriptor;
  }

  @Nullable
  private static HierarchyNodeDescriptor buildSuperClassHierarchy(@NotNull final Project project,
                                                                  @NotNull final List<? extends TypeHierarchyItem> items) {
    HierarchyNodeDescriptor descriptor = null;
    final DartClass[] superClasses = filterSuperClasses(project, items);
    for (int i = superClasses.length - 1; i >= 0; i--) {
      final DartClass superClass = superClasses[i];
      final HierarchyNodeDescriptor newDescriptor = new DartTypeHierarchyNodeDescriptor(project, descriptor, superClass, false);
      if (descriptor != null) {
        descriptor.setCachedChildren(new HierarchyNodeDescriptor[]{newDescriptor});
      }
      descriptor = newDescriptor;
    }
    return descriptor;
  }

  public static DartClass @NotNull [] filterSuperClasses(@NotNull final Project project, @NotNull final List<? extends TypeHierarchyItem> items) {
    if (items.isEmpty()) return new DartClass[]{};

    final Set<TypeHierarchyItem> seenItems = new HashSet<TypeHierarchyItem>();
    final List<DartClass> superClasses = new ArrayList<>();
    Integer superIndex = items.get(0).getSuperclass();
    while (superIndex != null) {
      TypeHierarchyItem superItem = items.get(superIndex);
      if (!seenItems.add(superItem)) {
        break;
      }
      final DartClass superClass = findDartClass(project, superItem);
      if (superClass != null) {
        superClasses.add(superClass);
      }
      superIndex = superItem.getSuperclass();
    }
    return superClasses.toArray(new DartClass[0]);
  }
}
