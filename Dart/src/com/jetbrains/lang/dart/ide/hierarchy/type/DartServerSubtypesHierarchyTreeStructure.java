// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.hierarchy.type;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.ide.hierarchy.HierarchyTreeStructure;
import com.intellij.openapi.project.Project;
import com.intellij.util.ArrayUtilRt;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.dartlang.analysis.server.protocol.TypeHierarchyItem;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

import static com.jetbrains.lang.dart.ide.hierarchy.DartHierarchyUtil.findDartClass;
import static com.jetbrains.lang.dart.ide.hierarchy.DartHierarchyUtil.getTypeHierarchyItems;

public class DartServerSubtypesHierarchyTreeStructure extends HierarchyTreeStructure {
  private final String myCurrentScopeType;

  public DartServerSubtypesHierarchyTreeStructure(final Project project, final DartClass dartClass, final String currentScopeType) {
    super(project, new DartTypeHierarchyNodeDescriptor(project, null, dartClass, true));
    myCurrentScopeType = currentScopeType;
  }

  @Override
  @NotNull
  protected final Object[] buildChildren(@NotNull final HierarchyNodeDescriptor descriptor) {
    final DartClass dartClass = ((DartTypeHierarchyNodeDescriptor)descriptor).getDartClass();
    if (dartClass == null) return ArrayUtilRt.EMPTY_OBJECT_ARRAY;

    if (DartResolveUtil.OBJECT.equals(dartClass.getName())) {
      return new Object[]{DartBundle.message("dart.hierarchy.object")};
    }

    final List<TypeHierarchyItem> items = getTypeHierarchyItems(dartClass);
    if (items.isEmpty()) return ArrayUtilRt.EMPTY_OBJECT_ARRAY;

    addSubClassHierarchy(Sets.newHashSet(), myProject, items, items.get(0), descriptor);
    return descriptor.getCachedChildren();
  }

  public static void addSubClassHierarchy(@NotNull final Set<? super TypeHierarchyItem> stackItems,
                                          @NotNull final Project project,
                                          @NotNull final List<? extends TypeHierarchyItem> items,
                                          @NotNull final TypeHierarchyItem item,
                                          @NotNull final HierarchyNodeDescriptor descriptor) {
    if (!stackItems.add(item)) {
      descriptor.setCachedChildren(ArrayUtilRt.EMPTY_OBJECT_ARRAY);
      return;
    }
    List<HierarchyNodeDescriptor> subDescriptors = Lists.newArrayList();
    try {
      for (int index : item.getSubclasses()) {
        final TypeHierarchyItem subItem = items.get(index);
        final DartClass subClass = findDartClass(project, subItem);
        if (subClass != null) {
          final HierarchyNodeDescriptor subDescriptor = new DartTypeHierarchyNodeDescriptor(project, descriptor, subClass, false);
          subDescriptors.add(subDescriptor);
          addSubClassHierarchy(stackItems, project, items, subItem, subDescriptor);
        }
      }
    }
    finally {
      stackItems.remove(item);
    }
    descriptor.setCachedChildren(subDescriptors.toArray(new HierarchyNodeDescriptor[0]));
  }
}
