/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetbrains.lang.dart.ide.hierarchy.type;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.ide.hierarchy.HierarchyTreeStructure;
import com.intellij.openapi.project.Project;
import com.intellij.util.ArrayUtil;
import com.jetbrains.lang.dart.psi.DartClass;
import org.dartlang.analysis.server.protocol.TypeHierarchyItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

  @NotNull
  @Override
  protected Object[] buildChildren(@NotNull HierarchyNodeDescriptor descriptor) {
    return ArrayUtil.EMPTY_OBJECT_ARRAY;
  }

  @NotNull
  private static HierarchyNodeDescriptor buildHierarchyElement(@NotNull final Project project, @NotNull final DartClass dartClass) {
    final List<TypeHierarchyItem> items = getTypeHierarchyItems(dartClass);
    final HierarchyNodeDescriptor superDescriptor = buildSuperClassHierarchy(project, items);
    final HierarchyNodeDescriptor baseDescriptor = new DartTypeHierarchyNodeDescriptor(project, superDescriptor, dartClass, true);
    if (superDescriptor != null) {
      superDescriptor.setCachedChildren(new HierarchyNodeDescriptor[]{baseDescriptor});
    }
    addSubClassHierarchy(Sets.<TypeHierarchyItem>newHashSet(), project, items, items.get(0), baseDescriptor);
    return baseDescriptor;
  }

  @Nullable
  private static HierarchyNodeDescriptor buildSuperClassHierarchy(@NotNull final Project project,
                                                                  @NotNull final List<TypeHierarchyItem> items) {
    HierarchyNodeDescriptor descriptor = null;
    final DartClass[] superClasses = createSuperClasses(project, items);
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

  @NotNull
  private static DartClass[] createSuperClasses(@NotNull final Project project, @NotNull final List<TypeHierarchyItem> items) {
    final Set<TypeHierarchyItem> seenItems = Sets.newHashSet();
    final List<DartClass> superClasses = Lists.newArrayList();
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
    return superClasses.toArray(new DartClass[superClasses.size()]);
  }
}
