package com.jetbrains.lang.dart.ide.hierarchy.type;

import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.ide.hierarchy.HierarchyTreeStructure;
import com.intellij.openapi.project.Project;
import com.intellij.util.ArrayUtil;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartType;
import com.jetbrains.lang.dart.util.DartClassResolveResult;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class DartSupertypesHierarchyTreeStructure extends HierarchyTreeStructure {

  public DartSupertypesHierarchyTreeStructure(final Project project, final DartClass dartClass) {
    super(project, new DartTypeHierarchyNodeDescriptor(project, null, dartClass, true));
  }

  @NotNull
  protected final Object[] buildChildren(@NotNull final HierarchyNodeDescriptor descriptor) {
    final DartClass dartClass = ((DartTypeHierarchyNodeDescriptor)descriptor).getDartClass();
    if (dartClass == null) {
      return ArrayUtil.EMPTY_OBJECT_ARRAY;
    }

    final List<DartClass> supers = new ArrayList<DartClass>();
    final DartType superClass = dartClass.getSuperClass();
    // looks like there's no sense in jumping to Object class
    if (superClass != null && !DartResolveUtil.OBJECT.equals(superClass.getName())) {
      final DartClassResolveResult dartClassResolveResult = DartResolveUtil.resolveClassByType(superClass);
      if (dartClassResolveResult.getDartClass() != null) {
        supers.add(dartClassResolveResult.getDartClass());
      }
    }

    List<DartClassResolveResult> implementsAndMixinsList =
      DartResolveUtil.resolveClassesByTypes(DartResolveUtil.getImplementsAndMixinsList(dartClass));
    for (DartClassResolveResult resolveResult : implementsAndMixinsList) {
      final DartClass resolveResultDartClass = resolveResult.getDartClass();
      if (resolveResultDartClass != null) {
        supers.add(resolveResultDartClass);
      }
    }

    final List<DartTypeHierarchyNodeDescriptor> descriptors = new ArrayList<DartTypeHierarchyNodeDescriptor>(supers.size());
    for (DartClass aSuper : supers) {
      descriptors.add(new DartTypeHierarchyNodeDescriptor(myProject, descriptor, aSuper, false));
    }
    return descriptors.toArray(new HierarchyNodeDescriptor[descriptors.size()]);
  }
}
