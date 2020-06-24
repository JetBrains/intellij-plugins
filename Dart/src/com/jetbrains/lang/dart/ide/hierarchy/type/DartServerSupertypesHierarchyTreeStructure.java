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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.jetbrains.lang.dart.ide.hierarchy.DartHierarchyUtil.findDartClass;
import static com.jetbrains.lang.dart.ide.hierarchy.DartHierarchyUtil.getTypeHierarchyItems;

public final class DartServerSupertypesHierarchyTreeStructure extends HierarchyTreeStructure {

  public DartServerSupertypesHierarchyTreeStructure(final Project project, final DartClass dartClass) {
    super(project, new DartTypeHierarchyNodeDescriptor(project, null, dartClass, true));
  }

  @Override
  protected final Object @NotNull [] buildChildren(@NotNull final HierarchyNodeDescriptor descriptor) {
    final DartClass dartClass = ((DartTypeHierarchyNodeDescriptor)descriptor).getDartClass();
    if (dartClass == null || DartResolveUtil.OBJECT.equals(dartClass.getName())) {
      return ArrayUtilRt.EMPTY_OBJECT_ARRAY;
    }

    final List<TypeHierarchyItem> items = getTypeHierarchyItems(dartClass);
    if (items.isEmpty()) return ArrayUtilRt.EMPTY_OBJECT_ARRAY;

    addSuperClassHierarchy(new HashSet<TypeHierarchyItem>(), myProject, items, items.get(0), descriptor);

    return descriptor.getCachedChildren();
  }

  private static void addSuperClassHierarchy(@NotNull final Set<TypeHierarchyItem> stackItems,
                                             @NotNull final Project project,
                                             @NotNull final List<TypeHierarchyItem> items,
                                             @NotNull final TypeHierarchyItem item,
                                             @NotNull final HierarchyNodeDescriptor descriptor) {
    if (!stackItems.add(item)) {
      descriptor.setCachedChildren(ArrayUtilRt.EMPTY_OBJECT_ARRAY);
      return;
    }

    List<HierarchyNodeDescriptor> superDescriptors = new ArrayList<>();
    try {
      // superclass
      final Integer superIndex = item.getSuperclass();
      if (superIndex != null) {
        addSuperClassNode(stackItems, project, items, descriptor, superDescriptors, superIndex);
      }
      // mixins
      for (int index : item.getMixins()) {
        addSuperClassNode(stackItems, project, items, descriptor, superDescriptors, index);
      }
      // interfaces
      for (int index : item.getInterfaces()) {
        addSuperClassNode(stackItems, project, items, descriptor, superDescriptors, index);
      }
    }
    finally {
      stackItems.remove(item);
    }
    descriptor.setCachedChildren(superDescriptors.toArray(new HierarchyNodeDescriptor[0]));
  }

  private static void addSuperClassNode(@NotNull final Set<TypeHierarchyItem> stackItems,
                                        @NotNull final Project project,
                                        @NotNull final List<TypeHierarchyItem> items,
                                        @NotNull final HierarchyNodeDescriptor parentNode,
                                        @NotNull final List<HierarchyNodeDescriptor> descriptors,
                                        final int index) {
    final TypeHierarchyItem superItem = items.get(index);
    if (DartResolveUtil.OBJECT.equals(superItem.getClassElement().getName())) {
      return;
    }
    final DartClass superClass = findDartClass(project, superItem);
    final HierarchyNodeDescriptor superDescriptor = new DartTypeHierarchyNodeDescriptor(project, parentNode, superClass, false);
    descriptors.add(superDescriptor);
    addSuperClassHierarchy(stackItems, project, items, superItem, superDescriptor);
  }
}
